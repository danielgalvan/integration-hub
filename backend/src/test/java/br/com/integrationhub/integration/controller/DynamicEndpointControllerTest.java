package br.com.integrationhub.integration.controller;

import br.com.integrationhub.exception.GlobalExceptionHandler;
import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.service.DynamicEndpointService;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        DynamicEndpointController.class,
        GlobalExceptionHandler.class
})
class DynamicEndpointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegrationService integrationService;

    @MockitoBean
    private EndpointService endpointService;

    @MockitoBean
    private DynamicEndpointService dynamicEndpointService;

    @Test
    void deveExecutarEndpointDinamico() throws Exception {

        Integration integration = createIntegration();
        Endpoint endpoint = createEndpoint();

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                any(Map.class)
        )).thenReturn(
                List.of(
                        Map.of(
                                "ID", 1,
                                "STATUS", "ABERTO"
                        )
                )
        );

        mockMvc.perform(
                        get("/api/pedidos/buscar")
                                .param("id", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ID").value(1))
                .andExpect(jsonPath("$[0].STATUS").value("ABERTO"));
    }

    @Test
    void deveRetornarNotFoundQuandoIntegrationNaoExistir()
            throws Exception {

        when(integrationService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/inexistente/buscar")
                                .param("id", "1")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundQuandoEndpointNaoExistir()
            throws Exception {

        Integration integration = createIntegration();

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/inexistente",
                "GET"
        )).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/pedidos/inexistente")
                                .param("id", "1")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarBadRequestParaParametroObrigatorioAusente()
            throws Exception {

        Integration integration = createIntegration();
        Endpoint endpoint = createEndpoint();

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                any(Map.class)
        )).thenThrow(
                new IllegalArgumentException(
                        "Parâmetro obrigatório não informado: id"
                )
        );

        mockMvc.perform(
                        get("/api/pedidos/buscar")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Parâmetro obrigatório não informado: id"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/pedidos/buscar")
                );
    }

    @Test
    void deveRetornarBadRequestParaParametroNumberInvalido()
            throws Exception {

        Integration integration = createIntegration();
        Endpoint endpoint = createEndpoint();

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                any(Map.class)
        )).thenThrow(
                new IllegalArgumentException(
                        "Parâmetro id deve ser numérico"
                )
        );

        mockMvc.perform(
                        get("/api/pedidos/buscar")
                                .param("id", "abc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Parâmetro id deve ser numérico")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/pedidos/buscar")
                );
    }

    @Test
    void deveRetornarErroControladoQuandoOcorrerErroDeBanco()
            throws Exception {

        Integration integration = createIntegration();
        Endpoint endpoint = createEndpoint();

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                any(Map.class)
        )).thenThrow(
                new DataAccessResourceFailureException(
                        "Erro ao acessar banco de dados"
                )
        );

        mockMvc.perform(
                        get("/api/pedidos/buscar")
                                .param("id", "1")
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.error")
                                .value("Internal Server Error")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Erro ao executar o endpoint configurado"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/pedidos/buscar")
                );
    }

    private Integration createIntegration() {

        Integration integration = new Integration();

        integration.setId(8L);
        integration.setName("Pedidos");
        integration.setBasePath("/api/pedidos");
        integration.setActive("S");

        return integration;
    }

    private Endpoint createEndpoint() {

        Endpoint endpoint = new Endpoint();

        endpoint.setId(1L);
        endpoint.setIntegrationId(8L);
        endpoint.setName("Buscar pedido");
        endpoint.setPath("/buscar");
        endpoint.setMethod("GET");
        endpoint.setSqlText(
                "select id from pedido where id = :id"
        );
        endpoint.setActive("S");

        return endpoint;
    }
}