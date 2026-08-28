package br.com.integrationhub.config;

import br.com.integrationhub.auth.AuthController;
import br.com.integrationhub.auth.AuthService;
import br.com.integrationhub.auth.LoginResponse;
import br.com.integrationhub.controller.HealthController;
import br.com.integrationhub.integration.controller.EndpointController;
import br.com.integrationhub.integration.controller.IntegrationController;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import br.com.integrationhub.security.JwtService;
import br.com.integrationhub.service.DatabaseHealthService;
import br.com.integrationhub.user.controller.UserController;
import br.com.integrationhub.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        IntegrationController.class,
        EndpointController.class,
        HealthController.class,
        AuthController.class,
        UserController.class
})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegrationService integrationService;

    @MockitoBean
    private EndpointService endpointService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private DatabaseHealthService databaseHealthService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @Test
    void devePermitirHealthSemToken() throws Exception {

        mockMvc.perform(
                        get("/api/health")
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void devePermitirLoginSemToken() throws Exception {

        when(authService.login(any()))
                .thenReturn(
                        new LoginResponse(
                                "token",
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
                                          "password": "senha",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void deveBloquearTrocaDeSenhaSemToken()
            throws Exception {

        mockMvc.perform(
                        put("/api/auth/password")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void devePermitirAdministradorTrocarSenha()
            throws Exception {

        mockToken(
                "token-admin",
                "admin",
                "A"
        );

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void devePermitirCriadorTrocarSenha()
            throws Exception {

        mockToken(
                "token-criador",
                "criador",
                "C"
        );

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void devePermitirConsumidorTrocarSenha()
            throws Exception {

        mockToken(
                "token-consumidor",
                "consumidor",
                "U"
        );

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void deveBloquearUsuariosSemToken()
            throws Exception {

        mockMvc.perform(
                        get("/api/users")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void devePermitirAdministradorConsultarUsuarios()
            throws Exception {

        mockToken(
                "token-admin",
                "admin",
                "A"
        );

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void deveBloquearCriadorAoConsultarUsuarios()
            throws Exception {

        mockToken(
                "token-criador",
                "criador",
                "C"
        );

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void deveBloquearConsumidorAoConsultarUsuarios()
            throws Exception {

        mockToken(
                "token-consumidor",
                "consumidor",
                "U"
        );

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void deveBloquearIntegrationsSemToken() throws Exception {

        mockMvc.perform(
                        get("/api/integrations")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void deveBloquearEndpointsSemToken() throws Exception {

        mockMvc.perform(
                        get("/api/endpoints")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void devePermitirAdministradorConsultarIntegrations()
            throws Exception {

        mockToken(
                "token-admin",
                "admin",
                "A"
        );

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void devePermitirCriadorConsultarIntegrations()
            throws Exception {

        mockToken(
                "token-criador",
                "criador",
                "C"
        );

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void devePermitirConsumidorConsultarIntegrations()
            throws Exception {

        mockToken(
                "token-consumidor",
                "consumidor",
                "U"
        );

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void devePermitirAdministradorConsultarEndpoints()
            throws Exception {

        mockToken(
                "token-admin",
                "admin",
                "A"
        );

        mockMvc.perform(
                        get("/api/endpoints")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void devePermitirCriadorConsultarEndpoints()
            throws Exception {

        mockToken(
                "token-criador",
                "criador",
                "C"
        );

        mockMvc.perform(
                        get("/api/endpoints")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void devePermitirConsumidorConsultarEndpoints()
            throws Exception {

        mockToken(
                "token-consumidor",
                "consumidor",
                "U"
        );

        mockMvc.perform(
                        get("/api/endpoints")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void devePermitirAdministradorExcluirIntegration()
            throws Exception {

        mockToken(
                "token-admin",
                "admin",
                "A"
        );

        mockMvc.perform(
                        delete("/api/integrations/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void devePermitirCriadorExcluirIntegration()
            throws Exception {

        mockToken(
                "token-criador",
                "criador",
                "C"
        );

        mockMvc.perform(
                        delete("/api/integrations/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void deveBloquearConsumidorAoExcluirIntegration()
            throws Exception {

        mockToken(
                "token-consumidor",
                "consumidor",
                "U"
        );

        mockMvc.perform(
                        delete("/api/integrations/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void devePermitirAdministradorExcluirEndpoint()
            throws Exception {

        mockToken(
                "token-admin",
                "admin",
                "A"
        );

        mockMvc.perform(
                        delete("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void devePermitirCriadorExcluirEndpoint()
            throws Exception {

        mockToken(
                "token-criador",
                "criador",
                "C"
        );

        mockMvc.perform(
                        delete("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void deveBloquearConsumidorAoExcluirEndpoint()
            throws Exception {

        mockToken(
                "token-consumidor",
                "consumidor",
                "U"
        );

        mockMvc.perform(
                        delete("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void deveBloquearTokenInvalido() throws Exception {

        when(
                jwtService.isTokenValid(
                        "token-invalido"
                )
        ).thenReturn(false);

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-invalido"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    private void mockToken(
            String token,
            String username,
            String role
    ) {

        when(
                jwtService.isTokenValid(token)
        ).thenReturn(true);

        when(
                jwtService.getUsername(token)
        ).thenReturn(username);

        when(
                jwtService.getRole(token)
        ).thenReturn(role);
    }
}
