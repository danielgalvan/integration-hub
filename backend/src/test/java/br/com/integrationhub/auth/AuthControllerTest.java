package br.com.integrationhub.auth;

import br.com.integrationhub.config.SecurityConfig;
import br.com.integrationhub.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deveRealizarLoginSemTrocaObrigatoriaDeSenha()
            throws Exception {

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(
                        new LoginResponse(
                                "token-jwt",
                                "Bearer",
                                3600,
                                false
                        )
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "admin",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .value("token-jwt")
                )
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expiresIn")
                                .value(3600)
                )
                .andExpect(
                        jsonPath("$.passwordChangeRequired")
                                .value(false)
                );
    }

    @Test
    void deveInformarTrocaObrigatoriaDeSenha()
            throws Exception {

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(
                        new LoginResponse(
                                "token-jwt",
                                "Bearer",
                                3600,
                                true
                        )
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "admin",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.passwordChangeRequired")
                                .value(true)
                );
    }

    @Test
    void deveRejeitarLoginSemUsername()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "password": "admin",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void deveRejeitarLoginSemPassword()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void deveRejeitarLoginSemEnvironment()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "admin"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Ambiente é obrigatório")
                );
    }

    @Test
    void deveRejeitarLoginComEnvironmentEmBranco()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "admin",
                                          "environment": "   "
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Ambiente é obrigatório")
                );
    }

    @Test
    void deveRejeitarLoginComUsernameEmBranco()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "   ",
                                          "password": "admin",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Usuário é obrigatório")
                );
    }

    @Test
    void deveRejeitarLoginComPasswordEmBranco()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "   ",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Senha é obrigatória")
                );
    }

    @Test
    void deveRetornarUnauthorizedParaCredenciaisInvalidas()
            throws Exception {

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(
                        new ResponseStatusException(
                                UNAUTHORIZED,
                                "Usuário ou senha inválidos"
                        )
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "errada",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void deveAlterarSenha()
            throws Exception {

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("admin");

        when(jwtService.getRole("token-valido"))
                .thenReturn("A");

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-valido"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "novaSenha123"
                                        }
                                        """)
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(authService)
                .changePassword(
                        "admin",
                        "novaSenha123"
                );
    }

    @Test
    void deveRejeitarTrocaDeSenhaSemToken()
            throws Exception {

        mockMvc.perform(
                        put("/api/auth/password")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "novaSenha123"
                                        }
                                        """)
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void deveRejeitarNovaSenhaEmBranco()
            throws Exception {

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("admin");

        when(jwtService.getRole("token-valido"))
                .thenReturn("A");

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-valido"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "   "
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void deveRejeitarNovaSenhaComMenosDeSeisCaracteres()
            throws Exception {

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("admin");

        when(jwtService.getRole("token-valido"))
                .thenReturn("A");

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-valido"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "12345"
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }
}
