package com.aryan.aiassistant.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 bytes long.");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long organizationId, String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 24 * 60 * 60 * 1000L);

        return Jwts.builder()
                .subject(email)
                .claim("organizationId", organizationId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
}