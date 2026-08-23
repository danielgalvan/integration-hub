package br.com.integrationhub.config;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicOpenApiCustomizerTest {

    private IntegrationService integrationService;
    private EndpointService endpointService;
    private DynamicOpenApiCustomizer customizer;

    @BeforeEach
    void setUp() {

        integrationService = mock(IntegrationService.class);
        endpointService = mock(EndpointService.class);

        customizer = new DynamicOpenApiCustomizer(
                integrationService,
                endpointService
        );
    }

    @Test
    void deveRemoverPathDinamicoGenerico() {

        OpenAPI openApi = createOpenApi();

        openApi.getPaths().addPathItem(
                "/api/**",
                new PathItem()
        );

        when(integrationService.findAll())
                .thenReturn(List.of());

        customizer.customise(openApi);

        assertFalse(
                openApi.getPaths().containsKey("/api/**")
        );
    }

    @Test
    void deveAdicionarEndpointDinamicoGet() {

        Integration integration = createIntegration(
                8L,
                "Pedidos",
                "/api/pedidos",
                "S"
        );

        Endpoint endpoint = createEndpoint(
                4L,
                8L,
                "Listar pedidos por status",
                "/listar",
                "GET",
                "S",
                List.of(
                        new EndpointParameter(
                                "status",
                                "VARCHAR2",
                                true
                        )
                )
        );

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationId(8L))
                .thenReturn(List.of(endpoint));

        OpenAPI openApi = createOpenApi();

        customizer.customise(openApi);

        PathItem pathItem =
                openApi.getPaths().get(
                        "/api/pedidos/listar"
                );

        assertNotNull(pathItem);
        assertNotNull(pathItem.getGet());

        assertEquals(
                "Listar pedidos por status",
                pathItem.getGet().getSummary()
        );

        assertTrue(
                pathItem.getGet()
                        .getTags()
                        .contains("Pedidos")
        );
    }

    @Test
    void deveCriarOperationIdUnico() {

        Integration integration = createIntegration(
                8L,
                "Pedidos",
                "/api/pedidos",
                "S"
        );

        Endpoint endpoint = createEndpoint(
                5L,
                8L,
                "Listar itens",
                "/itens",
                "GET",
                "S",
                List.of()
        );

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationId(8L))
                .thenReturn(List.of(endpoint));

        OpenAPI openApi = createOpenApi();

        customizer.customise(openApi);

        assertEquals(
                "dynamicGet_8_5",
                openApi.getPaths()
                        .get("/api/pedidos/itens")
                        .getGet()
                        .getOperationId()
        );
    }

    @Test
    void deveDocumentarParametroVarchar2() {

        Parameter parameter =
                getSingleParameter(
                        new EndpointParameter(
                                "status",
                                "VARCHAR2",
                                true
                        )
                );

        assertEquals(
                "status",
                parameter.getName()
        );

        assertEquals(
                "query",
                parameter.getIn()
        );

        assertTrue(parameter.getRequired());

        assertEquals(
                "string",
                parameter.getSchema().getType()
        );
    }

    @Test
    void deveDocumentarParametroNumber() {

        Parameter parameter =
                getSingleParameter(
                        new EndpointParameter(
                                "pedido_id",
                                "NUMBER",
                                true
                        )
                );

        assertEquals(
                "number",
                parameter.getSchema().getType()
        );
    }

    @Test
    void deveDocumentarParametroDate() {

        Parameter parameter =
                getSingleParameter(
                        new EndpointParameter(
                                "data",
                                "DATE",
                                true
                        )
                );

        assertEquals(
                "string",
                parameter.getSchema().getType()
        );

        assertEquals(
                "date",
                parameter.getSchema().getFormat()
        );
    }

    @Test
    void deveDocumentarParametroTimestamp() {

        Parameter parameter =
                getSingleParameter(
                        new EndpointParameter(
                                "dataHora",
                                "TIMESTAMP",
                                true
                        )
                );

        assertEquals(
                "string",
                parameter.getSchema().getType()
        );

        assertEquals(
                "date-time",
                parameter.getSchema().getFormat()
        );
    }

    @Test
    void deveDocumentarParametroOpcional() {

        Parameter parameter =
                getSingleParameter(
                        new EndpointParameter(
                                "status",
                                "VARCHAR2",
                                false
                        )
                );

        assertFalse(parameter.getRequired());
    }

    @Test
    void deveIgnorarIntegrationInativa() {

        Integration integration = createIntegration(
                8L,
                "Pedidos",
                "/api/pedidos",
                "N"
        );

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        OpenAPI openApi = createOpenApi();

        customizer.customise(openApi);

        assertFalse(
                openApi.getPaths()
                        .containsKey("/api/pedidos/listar")
        );
    }

    @Test
    void deveIgnorarEndpointInativo() {

        Integration integration = createIntegration(
                8L,
                "Pedidos",
                "/api/pedidos",
                "S"
        );

        Endpoint endpoint = createEndpoint(
                4L,
                8L,
                "Listar pedidos",
                "/listar",
                "GET",
                "N",
                List.of()
        );

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationId(8L))
                .thenReturn(List.of(endpoint));

        OpenAPI openApi = createOpenApi();

        customizer.customise(openApi);

        assertFalse(
                openApi.getPaths()
                        .containsKey("/api/pedidos/listar")
        );
    }

    @Test
    void deveIgnorarMetodoDiferenteDeGet() {

        Integration integration = createIntegration(
                8L,
                "Pedidos",
                "/api/pedidos",
                "S"
        );

        Endpoint endpoint = createEndpoint(
                4L,
                8L,
                "Criar pedido",
                "/criar",
                "POST",
                "S",
                List.of()
        );

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationId(8L))
                .thenReturn(List.of(endpoint));

        OpenAPI openApi = createOpenApi();

        customizer.customise(openApi);

        assertFalse(
                openApi.getPaths()
                        .containsKey("/api/pedidos/criar")
        );
    }

    @Test
    void deveCriarRespostasPadrao() {

        Integration integration = createIntegration(
                8L,
                "Pedidos",
                "/api/pedidos",
                "S"
        );

        Endpoint endpoint = createEndpoint(
                4L,
                8L,
                "Listar pedidos",
                "/listar",
                "GET",
                "S",
                List.of()
        );

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationId(8L))
                .thenReturn(List.of(endpoint));

        OpenAPI openApi = createOpenApi();

        customizer.customise(openApi);

        var responses =
                openApi.getPaths()
                        .get("/api/pedidos/listar")
                        .getGet()
                        .getResponses();

        assertTrue(responses.containsKey("200"));
        assertTrue(responses.containsKey("400"));
        assertTrue(responses.containsKey("404"));
        assertTrue(responses.containsKey("500"));
    }

    private Parameter getSingleParameter(
            EndpointParameter endpointParameter) {

        Integration integration = createIntegration(
                8L,
                "Pedidos",
                "/api/pedidos",
                "S"
        );

        Endpoint endpoint = createEndpoint(
                4L,
                8L,
                "Endpoint teste",
                "/teste",
                "GET",
                "S",
                List.of(endpointParameter)
        );

        when(integrationService.findAll())
                .thenReturn(List.of(integration));

        when(endpointService.findByIntegrationId(8L))
                .thenReturn(List.of(endpoint));

        OpenAPI openApi = createOpenApi();

        customizer.customise(openApi);

        return openApi.getPaths()
                .get("/api/pedidos/teste")
                .getGet()
                .getParameters()
                .getFirst();
    }

    private OpenAPI createOpenApi() {

        OpenAPI openApi = new OpenAPI();
        openApi.setPaths(new Paths());

        return openApi;
    }

    private Integration createIntegration(
            Long id,
            String name,
            String basePath,
            String active) {

        Integration integration = new Integration();

        integration.setId(id);
        integration.setName(name);
        integration.setBasePath(basePath);
        integration.setActive(active);

        return integration;
    }

    private Endpoint createEndpoint(
            Long id,
            Long integrationId,
            String name,
            String path,
            String method,
            String active,
            List<EndpointParameter> parameters) {

        Endpoint endpoint = new Endpoint();

        endpoint.setId(id);
        endpoint.setIntegrationId(integrationId);
        endpoint.setName(name);
        endpoint.setDescription(name);
        endpoint.setPath(path);
        endpoint.setMethod(method);
        endpoint.setActive(active);
        endpoint.setParameters(parameters);

        return endpoint;
    }
}