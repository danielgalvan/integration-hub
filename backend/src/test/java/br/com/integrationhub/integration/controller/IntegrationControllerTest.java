
package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.model.ApiKeyResponse;
import br.com.integrationhub.integration.service.IntegrationService;
import br.com.integrationhub.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntegrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegrationService integrationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deveListarBuscarECriarIntegracoes() throws Exception {

        Integration integration = createIntegration();
        integration.setLegacyApiKeyActive("N");

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(integrationService.findById(1L))
                .thenReturn(Optional.of(integration));

        when(integrationService.save(any(Integration.class)))
                .thenReturn(integration);

        mockMvc.perform(get("/api/integrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].basePath")
                        .value("/api/usuarios"))
                .andExpect(jsonPath("$[0].active")
                        .value("S"))
                .andExpect(jsonPath("$[0].legacyApiKeyActive")
                        .value("N"));

        mockMvc.perform(get("/api/integrations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Usuarios"))
                .andExpect(jsonPath("$.active")
                        .value("S"))
                .andExpect(jsonPath("$.legacyApiKeyActive")
                        .value("N"));

        mockMvc.perform(
                        post("/api/integrations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Usuarios",
                                          "description": "Consulta",
                                          "basePath": "/api/usuarios",
                                          "active": "S"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.active")
                        .value("S"))
                .andExpect(jsonPath("$.legacyApiKeyActive")
                        .value("N"));
    }

    @Test
    void deveRetornarNotFoundParaIntegracaoInexistente()
            throws Exception {

        when(integrationService.findById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/integrations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarIntegracaoSemNome()
            throws Exception {

        mockMvc.perform(
                        post("/api/integrations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"basePath\":\"/api/usuarios\"}"
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveGerarApiKeyDaIntegracao() throws Exception {

        when(integrationService.generateApiKey(1L))
                .thenReturn(new ApiKeyResponse("ihub_chave_teste"));

        mockMvc.perform(post("/api/integrations/1/api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey")
                        .value("ihub_chave_teste"));

        verify(integrationService).generateApiKey(1L);
    }

    @Test
    void deveAtivarApiKeyLegada() throws Exception {

        Integration integration = createIntegration();
        integration.setLegacyApiKeyActive("S");

        when(integrationService.updateLegacyApiKeyActive(1L, "S"))
                .thenReturn(integration);

        mockMvc.perform(
                        patch("/api/integrations/1/legacy-api-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "legacyApiKeyActive": "S"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.legacyApiKeyActive")
                        .value("S"));

        verify(integrationService)
                .updateLegacyApiKeyActive(1L, "S");
    }

    @Test
    void deveDesativarApiKeyLegada() throws Exception {

        Integration integration = createIntegration();
        integration.setLegacyApiKeyActive("N");

        when(integrationService.updateLegacyApiKeyActive(1L, "N"))
                .thenReturn(integration);

        mockMvc.perform(
                        patch("/api/integrations/1/legacy-api-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "legacyApiKeyActive": "N"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.legacyApiKeyActive")
                        .value("N"));

        verify(integrationService)
                .updateLegacyApiKeyActive(1L, "N");
    }

    @Test
    void deveEncaminharValorInvalidoAoService() throws Exception {

        Integration integration = createIntegration();
        integration.setLegacyApiKeyActive("X");

        when(integrationService.updateLegacyApiKeyActive(1L, "X"))
                .thenReturn(integration);

        mockMvc.perform(
                        patch("/api/integrations/1/legacy-api-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "legacyApiKeyActive": "X"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        verify(integrationService)
                .updateLegacyApiKeyActive(1L, "X");
    }

    private Integration createIntegration() {

        Integration integration = new Integration();

        integration.setId(1L);
        integration.setName("Usuarios");
        integration.setDescription("Consulta");
        integration.setBasePath("/api/usuarios");
        integration.setActive("S");
        integration.setCreatedBy("SYSTEM");

        return integration;
    }
}
