package br.com.integrationhub.config;

import br.com.integrationhub.integration.controller.EndpointController;
import br.com.integrationhub.integration.controller.IntegrationController;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import br.com.integrationhub.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        IntegrationController.class,
        EndpointController.class
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

    @Test
    void deveBloquearIntegrationsSemToken() throws Exception {

        mockMvc.perform(
                        get("/api/integrations")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBloquearEndpointsSemToken() throws Exception {

        mockMvc.perform(
                        get("/api/endpoints")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirIntegrationsComTokenValido() throws Exception {

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("admin");

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-valido"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirEndpointsComTokenValido() throws Exception {

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("admin");

        mockMvc.perform(
                        get("/api/endpoints")
                                .header(
                                        "Authorization",
                                        "Bearer token-valido"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearTokenInvalido() throws Exception {

        when(jwtService.isTokenValid("token-invalido"))
                .thenReturn(false);

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-invalido"
                                )
                )
                .andExpect(status().isUnauthorized());
    }
}