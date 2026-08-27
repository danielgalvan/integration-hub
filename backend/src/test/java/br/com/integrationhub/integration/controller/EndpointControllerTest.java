package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EndpointController.class)
@AutoConfigureMockMvc(addFilters = false)
class EndpointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EndpointService endpointService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deveListarEndpoints() throws Exception {

        Endpoint endpoint = createEndpoint();

        when(endpointService.findAll())
                .thenReturn(List.of(endpoint));

        mockMvc.perform(get("/api/endpoints"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Buscar usuario")
                )
                .andExpect(
                        jsonPath("$[0].active")
                                .value("S")
                )
                .andExpect(
                        jsonPath("$[0].parameters[0].name")
                                .value("id")
                );

        verify(endpointService).findAll();
    }

    @Test
    void deveBuscarEndpointPorId() throws Exception {

        Endpoint endpoint = createEndpoint();

        when(endpointService.findById(1L))
                .thenReturn(Optional.of(endpoint));

        mockMvc.perform(get("/api/endpoints/1"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.integrationId")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.sqlText")
                                .value("select 1")
                );

        verify(endpointService).findById(1L);
    }

    @Test
    void deveRetornarNotFoundParaEndpointInexistente()
            throws Exception {

        when(endpointService.findById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/endpoints/99"))
                .andExpect(status().isNotFound());

        verify(endpointService).findById(99L);
    }

    @Test
    void deveListarEndpointsPorIntegracao() throws Exception {

        Endpoint endpoint = createEndpoint();

        when(endpointService.findByIntegrationId(2L))
                .thenReturn(List.of(endpoint));

        mockMvc.perform(
                        get("/api/endpoints/integration/2")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].integrationId")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Buscar usuario")
                );

        verify(endpointService)
                .findByIntegrationId(2L);
    }

    @Test
    void deveCriarEndpoint() throws Exception {

        Endpoint endpoint = createEndpoint();

        when(endpointService.save(any(Endpoint.class)))
                .thenReturn(endpoint);

        mockMvc.perform(
                        post("/api/endpoints")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
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
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.active")
                                .value("S")
                );

        verify(endpointService)
                .save(any(Endpoint.class));
    }

    @Test
    void deveAtualizarEndpoint() throws Exception {

        Endpoint endpoint = createEndpoint();

        endpoint.setName("Buscar usuario atualizado");
        endpoint.setPath("/usuario-atualizado");
        endpoint.setActive("N");
        endpoint.setUpdatedBy("SYSTEM");

        when(
                endpointService.update(
                        org.mockito.ArgumentMatchers.eq(1L),
                        any(Endpoint.class)
                )
        ).thenReturn(endpoint);

        mockMvc.perform(
                        put("/api/endpoints/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "integrationId": 2,
                                          "name": "Buscar usuario atualizado",
                                          "description": "Consulta atualizada",
                                          "path": "/usuario-atualizado",
                                          "method": "GET",
                                          "sqlText": "select 1",
                                          "parameters": [
                                            {
                                              "name": "id",
                                              "type": "NUMBER",
                                              "required": true
                                            }
                                          ],
                                          "active": "N"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Buscar usuario atualizado")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/usuario-atualizado")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value("N")
                );

        verify(endpointService)
                .update(
                        org.mockito.ArgumentMatchers.eq(1L),
                        any(Endpoint.class)
                );
    }

    @Test
    void deveRetornarNotFoundAoAtualizarEndpointInexistente()
            throws Exception {

        when(
                endpointService.update(
                        org.mockito.ArgumentMatchers.eq(99L),
                        any(Endpoint.class)
                )
        ).thenThrow(
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Endpoint não encontrado"
                )
        );

        mockMvc.perform(
                        put("/api/endpoints/99")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "integrationId": 2,
                                          "name": "Endpoint inexistente",
                                          "description": "Teste",
                                          "path": "/inexistente",
                                          "method": "GET",
                                          "sqlText": "select 1",
                                          "parameters": [],
                                          "active": "S"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound());

        verify(endpointService)
                .update(
                        org.mockito.ArgumentMatchers.eq(99L),
                        any(Endpoint.class)
                );
    }

    @Test
    void deveExcluirEndpoint() throws Exception {

        mockMvc.perform(
                        delete("/api/endpoints/1")
                )
                .andExpect(status().isNoContent());

        verify(endpointService).delete(1L);
    }

    @Test
    void deveRetornarNotFoundAoExcluirEndpointInexistente()
            throws Exception {

        doThrow(
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Endpoint não encontrado"
                )
        ).when(endpointService).delete(99L);

        mockMvc.perform(
                        delete("/api/endpoints/99")
                )
                .andExpect(status().isNotFound());

        verify(endpointService).delete(99L);
    }

    @Test
    void deveRejeitarEndpointComDadosInvalidos() throws Exception {

        mockMvc.perform(post("/api/endpoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"name\":\"Endpoint\","
                                        + "\"path\":\"sem-barra\","
                                        + "\"method\":\"POST\","
                                        + "\"sqlText\":\"\"}"
                        ))
                .andExpect(status().isBadRequest());

        verify(endpointService, never())
                .save(any(Endpoint.class));
    }

    private Endpoint createEndpoint() {

        EndpointParameter parameter =
                new EndpointParameter(
                        "id",
                        "NUMBER",
                        true
                );

        Endpoint endpoint = new Endpoint();

        endpoint.setId(1L);
        endpoint.setIntegrationId(2L);
        endpoint.setName("Buscar usuario");
        endpoint.setDescription(
                "Consulta usuario pelo identificador"
        );
        endpoint.setPath("/usuario");
        endpoint.setMethod("GET");
        endpoint.setSqlText("select 1");
        endpoint.setParameters(
                List.of(parameter)
        );
        endpoint.setActive("S");
        endpoint.setCreatedBy("SYSTEM");

        return endpoint;
    }
}