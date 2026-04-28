package com.poshan.backend.security;

import com.poshan.backend.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JwtService {

    private final String secret;
    private final int accessTokenTtlDays;
    private SecretKey signingKey;

    public JwtService(
        @Value("${app.auth.jwt.secret}") String secret,
        @Value("${app.auth.jwt.access-token-ttl-days:7}") int accessTokenTtlDays
    ) {
        this.secret = secret;
        this.accessTokenTtlDays = accessTokenTtlDays;
    }

    @PostConstruct
    public void initialize() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes long.");
        }

        signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public LocalDateTime calculateAccessTokenExpiry() {
        return LocalDateTime.now().plusDays(accessTokenTtlDays);
    }

    public String generateAccessToken(Long id, String email, Role role, LocalDateTime expiresAt) {
        Date issuedAt = new Date();
        Date expiration = Date.from(expiresAt.toInstant(ZoneOffset.UTC));

        return Jwts.builder()
            .subject(email)
            .issuedAt(issuedAt)
            .expiration(expiration)
            .claim("uid", id)
            .claim("role", role.name())
            .signWith(signingKey)
            .compact();
    }

    public AuthPrincipal parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            Long id = claims.get("uid", Long.class);
            String email = claims.getSubject();
            String roleValue = claims.get("role", String.class);

            if (id == null || email == null || roleValue == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            return new AuthPrincipal(id, Role.valueOf(roleValue), email);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}
