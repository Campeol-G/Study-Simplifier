package com.studys.identity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String gerar(UUID userId, String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(properties.getJwtExpirationMs())))
                .signWith(key())
                .compact();
    }

    public UUID validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }
    }
}
