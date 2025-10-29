package com.officefood.healthy_food_api.config;

import com.officefood.healthy_food_api.service.JwtService;
import com.officefood.healthy_food_api.service.TokenBlacklistService;
import com.officefood.healthy_food_api.service.impl.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserServiceImpl userService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**",
            "/error", "/api/auth/**", "/", "/health", "/actuator/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService, UserServiceImpl userService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String method = req.getMethod();

        // Log để debug
        System.out.println("JwtAuthFilter: Processing " + method + " " + path);

        // Skip authentication for OPTIONS requests and public paths
        if ("OPTIONS".equalsIgnoreCase(method) || isPublicPath(path)) {
            System.out.println("JwtAuthFilter: Public path, skipping auth for " + path);
            chain.doFilter(req, res);
            return;
        }

        // Get Authorization header
        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Check if token is blacklisted
                if (!tokenBlacklistService.isBlacklisted(token)) {
                    String username = jwtService.extractUsername(token);
                    var userDetails = userService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(token, userDetails)) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("JwtAuthFilter: Authentication successful for user: " + username);
                    }
                }
            } catch (Exception e) {
                System.out.println("JwtAuthFilter: Token validation failed: " + e.getMessage());
                // Continue without authentication
            }
        }

        chain.doFilter(req, res);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
