package com.MCF.backend.service;

import com.MCF.backend.security.JwtSigningKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtTokenService(
            @Value("${app.auth.jwt.secret}") String secret,
            @Value("${app.auth.jwt.access-ttl-ms:86400000}") long accessTtlMs,
            @Value("${app.auth.jwt.refresh-ttl-ms:2592000000}") long refreshTtlMs
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.auth.jwt.secret must be set");
        }
        this.signingKey = JwtSigningKey.fromSecret(secret);
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    public String createAccessToken(long userId, String username, String role) {
        return buildToken(String.valueOf(userId), username, role, "access", accessTtlMs);
    }

    public String createRefreshToken(long userId, String username, String role) {
        return buildToken(String.valueOf(userId), username, role, "refresh", refreshTtlMs);
    }

    private String buildToken(String subject, String username, String role, String typ, long ttlMs) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .claim("username", username)
                .claim("role", role == null || role.isBlank() ? "user" : role)
                .claim("typ", typ)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ttlMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAndValidate(String token, String expectedTyp) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String typ = claims.get("typ", String.class);
        if (typ == null || !typ.equals(expectedTyp)) {
            throw new IllegalArgumentException("Invalid token type");
        }
        return claims;
    }
}
