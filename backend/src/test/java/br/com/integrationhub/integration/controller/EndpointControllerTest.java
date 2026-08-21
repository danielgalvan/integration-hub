package br.com.integrationhub.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.service.EndpointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EndpointController.class)
class EndpointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EndpointService endpointService;

    @Test
    void deveListarBuscarECriarEndpoints() throws Exception {
        var endpoint = new Endpoint(1L, 2L, "Buscar usuario", null, "/usuario", "GET", "select 1", List.of("id"), true);
        when(endpointService.findAll()).thenReturn(List.of(endpoint));
        when(endpointService.findById(1L)).thenReturn(Optional.of(endpoint));
        when(endpointService.save(any(Endpoint.class))).thenReturn(endpoint);

        mockMvc.perform(get("/api/endpoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Buscar usuario"));

        mockMvc.perform(get("/api/endpoints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integrationId").value(2));

        mockMvc.perform(post("/api/endpoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"integrationId\":2,\"name\":\"Buscar usuario\",\"path\":\"/usuario\",\"method\":\"GET\",\"sql\":\"select 1\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornarNotFoundParaEndpointInexistente() throws Exception {
        when(endpointService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/endpoints/99"))
                .andExpect(status().isNotFound());
    }
}
