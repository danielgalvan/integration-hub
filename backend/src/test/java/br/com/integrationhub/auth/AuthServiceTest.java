package br.com.integrationhub.auth;

import br.com.integrationhub.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {

        authService = new AuthService(
                jwtService,
                passwordEncoder,
                "admin",
                "$2a$10$hash",
                60
        );
    }

    @Test
    void deveRealizarLoginComCredenciaisValidas() {

        when(
                passwordEncoder.matches(
                        "admin",
                        "$2a$10$hash"
                )
        ).thenReturn(true);

        when(jwtService.generateToken("admin"))
                .thenReturn("token-jwt");

        LoginResponse response =
                authService.login(
                        new LoginRequest(
                                "admin",
                                "admin"
                        )
                );

        assertEquals(
                "token-jwt",
                response.token()
        );

        assertEquals(
                "Bearer",
                response.tokenType()
        );

        assertEquals(
                3600,
                response.expiresIn()
        );

        verify(passwordEncoder)
                .matches(
                        "admin",
                        "$2a$10$hash"
                );

        verify(jwtService)
                .generateToken("admin");
    }

    @Test
    void deveRejeitarUsuarioInvalido() {

        when(
                passwordEncoder.matches(
                        "admin",
                        "$2a$10$hash"
                )
        ).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(
                                new LoginRequest(
                                        "outro",
                                        "admin"
                                )
                        )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }

    @Test
    void deveRejeitarSenhaInvalida() {

        when(
                passwordEncoder.matches(
                        "senha-errada",
                        "$2a$10$hash"
                )
        ).thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(
                                new LoginRequest(
                                        "admin",
                                        "senha-errada"
                                )
                        )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }

    @Test
    void deveRetornarExpiresInEmSegundos() {

        when(
                passwordEncoder.matches(
                        "admin",
                        "$2a$10$hash"
                )
        ).thenReturn(true);

        when(jwtService.generateToken("admin"))
                .thenReturn("token-jwt");

        LoginResponse response =
                authService.login(
                        new LoginRequest(
                                "admin",
                                "admin"
                        )
                );

        assertEquals(
                60 * 60,
                response.expiresIn()
        );
    }
}
