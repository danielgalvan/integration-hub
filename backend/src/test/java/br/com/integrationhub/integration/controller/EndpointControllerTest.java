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
import br.com.integrationhub.integration.model.EndpointParameter;
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
        EndpointParameter parameter = new EndpointParameter(
                "id",
                "NUMBER",
                true
        );

        Endpoint endpoint = new Endpoint();
        endpoint.setId(1L);
        endpoint.setIntegrationId(2L);
        endpoint.setName("Buscar usuario");
        endpoint.setDescription("Consulta usuario pelo identificador");
        endpoint.setPath("/usuario");
        endpoint.setMethod("GET");
        endpoint.setSqlText("select 1");
        endpoint.setParameters(List.of(parameter));
        endpoint.setActive("S");
        endpoint.setCreatedBy("SYSTEM");

        when(endpointService.findAll()).thenReturn(List.of(endpoint));
        when(endpointService.findById(1L)).thenReturn(Optional.of(endpoint));
        when(endpointService.save(any(Endpoint.class))).thenReturn(endpoint);

        mockMvc.perform(get("/api/endpoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Buscar usuario"))
                .andExpect(jsonPath("$[0].active").value("S"))
                .andExpect(jsonPath("$[0].parameters[0].name").value("id"));

        mockMvc.perform(get("/api/endpoints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integrationId").value(2))
                .andExpect(jsonPath("$.sqlText").value("select 1"));

        mockMvc.perform(post("/api/endpoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "integrationId": 2,
                                  "name": "Buscar usuario",
                                  "description": "Consulta usuario pelo identificador",
                                  "path": "/usuario",
                                  "method": "GET",
                                  "sqlText": "select 1",
                                  "parameters": [
                                    {
                                      "name": "id",
                                      "type": "NUMBER",
                                      "required": true
                                    }
                                  ],
                                  "active": "S"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value("S"));
    }

    @Test
    void deveRetornarNotFoundParaEndpointInexistente() throws Exception {
        when(endpointService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/endpoints/99"))
                .andExpect(status().isNotFound());
    }
}