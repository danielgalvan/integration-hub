package br.com.integrationhub.auth;

import br.com.integrationhub.security.JwtService;
import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    private AuthService authService;

    @BeforeEach
    void setUp() {

        authService = new AuthService(
                jwtService,
                userService,
                60
        );
    }

    @Test
    void deveRealizarLoginComCredenciaisValidas() {

        User user = createUser(
                "admin",
                "A",
                "A",
                "N"
        );

        when(userService.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(
                userService.passwordMatches(
                        "admin",
                        user
                )
        ).thenReturn(true);

        when(
                jwtService.generateToken(
                        "admin",
                        "dev",
                        "A"
                )
        ).thenReturn("token-jwt");

        LoginResponse response =
                authService.login(
                        new LoginRequest(
                                "admin",
                                "admin",
                                "dev"
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

        assertFalse(
                response.passwordChangeRequired()
        );

        verify(userService)
                .findByUsername("admin");

        verify(userService)
                .passwordMatches(
                        "admin",
                        user
                );

        verify(jwtService)
                .generateToken(
                        "admin",
                        "dev",
                        "A"
                );
    }

    @Test
    void deveInformarTrocaObrigatoriaDeSenha() {

        User user = createUser(
                "admin",
                "A",
                "A",
                "S"
        );

        when(userService.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(
                userService.passwordMatches(
                        "admin",
                        user
                )
        ).thenReturn(true);

        when(
                jwtService.generateToken(
                        "admin",
                        "dev",
                        "A"
                )
        ).thenReturn("token-jwt");

        LoginResponse response =
                authService.login(
                        new LoginRequest(
                                "admin",
                                "admin",
                                "dev"
                        )
                );

        assertTrue(
                response.passwordChangeRequired()
        );
    }

    @Test
    void deveRealizarLoginComPerfilCriador() {

        User user = createUser(
                "criador",
                "A",
                "C",
                "N"
        );

        when(userService.findByUsername("criador"))
                .thenReturn(Optional.of(user));

        when(
                userService.passwordMatches(
                        "senha",
                        user
                )
        ).thenReturn(true);

        when(
                jwtService.generateToken(
                        "criador",
                        "homolog",
                        "C"
                )
        ).thenReturn("token-jwt");

        LoginResponse response =
                authService.login(
                        new LoginRequest(
                                "criador",
                                "senha",
                                "homolog"
                        )
                );

        assertEquals(
                "token-jwt",
                response.token()
        );

        verify(jwtService)
                .generateToken(
                        "criador",
                        "homolog",
                        "C"
                );
    }

    @Test
    void deveRejeitarUsuarioInexistente() {

        when(userService.findByUsername("outro"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(
                                new LoginRequest(
                                        "outro",
                                        "admin",
                                        "dev"
                                )
                        )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }

    @Test
    void deveRejeitarUsuarioInativo() {

        User user = createUser(
                "admin",
                "I",
                "A",
                "N"
        );

        when(userService.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(
                                new LoginRequest(
                                        "admin",
                                        "admin",
                                        "dev"
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

        User user = createUser(
                "admin",
                "A",
                "A",
                "N"
        );

        when(userService.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(
                userService.passwordMatches(
                        "senha-errada",
                        user
                )
        ).thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(
                                new LoginRequest(
                                        "admin",
                                        "senha-errada",
                                        "dev"
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

        User user = createUser(
                "admin",
                "A",
                "A",
                "N"
        );

        when(userService.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(
                userService.passwordMatches(
                        "admin",
                        user
                )
        ).thenReturn(true);

        when(
                jwtService.generateToken(
                        "admin",
                        "dev",
                        "A"
                )
        ).thenReturn("token-jwt");

        LoginResponse response =
                authService.login(
                        new LoginRequest(
                                "admin",
                                "admin",
                                "dev"
                        )
                );

        assertEquals(
                60 * 60,
                response.expiresIn()
        );
    }

    @Test
    void deveAlterarSenhaDoUsuarioAutenticado() {

        User user = createUser(
                "admin",
                "A",
                "A",
                "S"
        );

        when(userService.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        authService.changePassword(
                "admin",
                "novaSenha"
        );

        verify(userService).changePassword(
                1L,
                "novaSenha"
        );
    }

    @Test
    void deveRetornarNotFoundAoAlterarSenhaDeUsuarioInexistente() {

        when(userService.findByUsername("inexistente"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.changePassword(
                        "inexistente",
                        "novaSenha"
                )
        );

        assertEquals(
                404,
                exception.getStatusCode().value()
        );
    }

    @Test
    void deveRetornarDadosDoUsuarioAutenticado() {

        User user = createUser(
                "criador",
                "A",
                "C",
                "N"
        );

        when(userService.findByUsername("criador"))
                .thenReturn(Optional.of(user));

        AuthenticatedUserResponse response =
                authService.getAuthenticatedUser("criador");

        assertEquals(1L, response.id());
        assertEquals("criador", response.username());
        assertEquals("Usuário Teste", response.name());
        assertEquals("teste@email.com", response.email());
        assertEquals("C", response.role());
    }

    @Test
    void deveRetornarNotFoundAoBuscarUsuarioAutenticadoInexistente() {

        when(userService.findByUsername("inexistente"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.getAuthenticatedUser("inexistente")
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    private User createUser(
            String username,
            String status,
            String type,
            String passwordChangeRequired
    ) {

        return new User(
                1L,
                username,
                "Usuário Teste",
                "teste@email.com",
                "$2a$10$hash",
                status,
                type,
                passwordChangeRequired,
                LocalDateTime.now(),
                null
        );
    }
}
