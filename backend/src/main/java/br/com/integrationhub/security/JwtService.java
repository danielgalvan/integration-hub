package br.com.integrationhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMinutes;

    public JwtService(
            @Value("${integration-hub.security.jwt.secret}")
            String secret,

            @Value("${integration-hub.security.jwt.expiration-minutes}")
            long expirationMinutes
    ) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(
            String username,
            String environment,
            String role
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("environment", environment)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        expirationMinutes,
                                        ChronoUnit.MINUTES
                                )
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsername(String token) {
        return getClaims(token)
                .getSubject();
    }

    public String getEnvironment(String token) {
        return getClaims(token)
                .get(
                        "environment",
                        String.class
                );
    }

    public String getRole(String token) {
        return getClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes =
                Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
