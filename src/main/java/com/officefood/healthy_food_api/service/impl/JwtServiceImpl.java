package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtServiceImpl(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshTokenExpirationMs // 7 days default
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username != null && username.equals(userDetails.getUsername());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("type", "access")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTokenExpirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken, String username) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(refreshToken).getBody();

            String tokenUsername = claims.getSubject();
            String tokenType = claims.get("type", String.class);

            return tokenUsername != null &&
                    tokenUsername.equals(username) &&
                    "refresh".equals(tokenType) &&
                    claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getAccessTokenExpiration() {
        return accessTokenExpirationMs / 1000; // Convert to seconds
    }

    @Override
    public long getRefreshTokenExpiration() {
        return refreshTokenExpirationMs / 1000; // Convert to seconds
    }
}
