package br.com.integrationhub.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString(
                    "01234567890123456789012345678901"
                            .getBytes(StandardCharsets.UTF_8)
            );

    @Test
    void deveGerarTokenValidoComUsername() {

        JwtService jwtService = new JwtService(SECRET, 60);

        String token = jwtService.generateToken("admin");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("admin", jwtService.getUsername(token));
    }

    @Test
    void deveRejeitarTokenAdulterado() {

        JwtService jwtService = new JwtService(SECRET, 60);
        String token = jwtService.generateToken("admin");

        assertFalse(jwtService.isTokenValid(token + "alterado"));
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {

        JwtService issuer = new JwtService(SECRET, 60);
        JwtService validator = new JwtService(
                Base64.getEncoder().encodeToString(
                        "abcdefghijklmnopqrstuvwxyzABCDEF"
                                .getBytes(StandardCharsets.UTF_8)
                ),
                60
        );

        assertFalse(
                validator.isTokenValid(
                        issuer.generateToken("admin")
                )
        );
    }

    @Test
    void deveRejeitarTokenExpirado() {

        JwtService jwtService = new JwtService(SECRET, -1);

        assertFalse(
                jwtService.isTokenValid(
                        jwtService.generateToken("admin")
                )
        );
    }
}
