package br.com.integrationhub.integration.controller;

import br.com.integrationhub.exception.GlobalExceptionHandler;
import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.service.DynamicEndpointService;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        DynamicEndpointController.class,
        IntegrationController.class,
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

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/buscar"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                ArgumentMatchers.<Map<String, String>>any()
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
    void deveRepassarParametrosDaRequisicaoParaService()
            throws Exception {

        Integration integration = createIntegration();
        Endpoint endpoint = createEndpoint();

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/buscar"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                ArgumentMatchers.<Map<String, String>>any()
        )).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/pedidos/buscar")
                                .param("id", "10")
                                .param("status", "ABERTO")
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Map<String, String>> parametersCaptor =
                createStringMapCaptor();

        verify(dynamicEndpointService).executeGet(
                eq(endpoint),
                parametersCaptor.capture()
        );

        Map<String, String> parameters =
                parametersCaptor.getValue();

        assertEquals(
                "10",
                parameters.get("id")
        );

        assertEquals(
                "ABERTO",
                parameters.get("status")
        );
    }

    @Test
    void deveRetornarNotFoundQuandoIntegrationNaoExistir()
            throws Exception {

        when(integrationService.findBestMatchByRequestPath(
                "/api/inexistente/buscar"
        )).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/inexistente/buscar")
                                .param("id", "1")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Integração não encontrada")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/inexistente/buscar")
                );
    }

    @Test
    void deveRetornarNotFoundQuandoEndpointNaoExistir()
            throws Exception {

        Integration integration = createIntegration();

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/inexistente"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/inexistente",
                "GET"
        )).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/pedidos/inexistente")
                                .param("id", "1")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Endpoint não encontrado")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/pedidos/inexistente")
                );
    }

    @Test
    void deveRetornarBadRequestParaParametroObrigatorioAusente()
            throws Exception {

        Integration integration = createIntegration();
        Endpoint endpoint = createEndpoint();

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/buscar"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                ArgumentMatchers.<Map<String, String>>any()
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

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/buscar"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                ArgumentMatchers.<Map<String, String>>any()
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

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/buscar"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                ArgumentMatchers.<Map<String, String>>any()
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

    @Test
    void deveUsarRequestPathCompletoParaResolverIntegration()
            throws Exception {

        Integration integration = createIntegration();
        Endpoint endpoint = createEndpoint();

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/buscar"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                8L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                ArgumentMatchers.<Map<String, String>>any()
        )).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/pedidos/buscar")
                                .param("id", "1")
                )
                .andExpect(status().isOk());

        verify(integrationService)
                .findBestMatchByRequestPath(
                        "/api/pedidos/buscar"
                );
    }

    @Test
    void deveCalcularEndpointPathComBaseNaIntegrationResolvida()
            throws Exception {

        Integration integration = new Integration();

        integration.setId(20L);
        integration.setName("Pedidos especiais");
        integration.setBasePath("/api/pedidos/especiais");
        integration.setActive("S");

        Endpoint endpoint = createEndpoint();
        endpoint.setIntegrationId(20L);

        when(integrationService.findBestMatchByRequestPath(
                "/api/pedidos/especiais/buscar"
        )).thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                20L,
                "/buscar",
                "GET"
        )).thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(
                eq(endpoint),
                ArgumentMatchers.<Map<String, String>>any()
        )).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/pedidos/especiais/buscar")
                                .param("id", "1")
                )
                .andExpect(status().isOk());

        verify(endpointService)
                .findByIntegrationIdAndPathAndMethod(
                        20L,
                        "/buscar",
                        "GET"
                );
    }

    @Test
    void deveManterRotaAdministrativaForaDoControllerDinamico()
            throws Exception {

        when(integrationService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/integrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(integrationService).findAll();
    }

    @Test
    void deveRejeitarMetodoDiferenteDeGetNaRotaDinamica()
            throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.post(
                                        "/api/pedidos/buscar"
                                )
                )
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Método HTTP não suportado")
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

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, String>> createStringMapCaptor() {

        return ArgumentCaptor.forClass(
                (Class<Map<String, String>>) (Class<?>) Map.class
        );
    }
}
