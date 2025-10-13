package com.officefood.healthy_food_api.config;

import com.officefood.healthy_food_api.service.JwtService;
import com.officefood.healthy_food_api.service.TokenBlacklistService;

import com.officefood.healthy_food_api.service.UserDetailsServiceImpl;
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
    private final UserDetailsServiceImpl userDetailsService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**",
            "/error", "/api/auth/**", "/", "/health", "/actuator/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService, UserDetailsServiceImpl uds) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(req.getMethod()) || isPublicPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (!tokenBlacklistService.isBlacklisted(token)) {
                try {
                    String username = jwtService.extractUsername(token);
                    var userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, userDetails)) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (Exception ignored) { }
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
