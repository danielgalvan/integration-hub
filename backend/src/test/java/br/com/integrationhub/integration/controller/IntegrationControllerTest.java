package br.com.integrationhub.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.service.IntegrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IntegrationController.class)
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegrationService integrationService;

    @Test
    void deveListarBuscarECriarIntegracoes() throws Exception {
        var integration = new Integration(1L, "Usuarios", "Consulta", "/usuarios", true);
        when(integrationService.findAll()).thenReturn(List.of(integration));
        when(integrationService.findById(1L)).thenReturn(Optional.of(integration));
        when(integrationService.save(any(Integration.class))).thenReturn(integration);

        mockMvc.perform(get("/api/integrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].basePath").value("/usuarios"));

        mockMvc.perform(get("/api/integrations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Usuarios"));

        mockMvc.perform(post("/api/integrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Usuarios\",\"basePath\":\"/usuarios\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornarNotFoundParaIntegracaoInexistente() throws Exception {
        when(integrationService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/integrations/99"))
                .andExpect(status().isNotFound());
    }
}
