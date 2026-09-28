package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.model.IntegrationCredential;
import br.com.integrationhub.integration.service.DynamicEndpointService;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationCredentialService;
import br.com.integrationhub.integration.service.IntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamicEndpointControllerTest {

    private IntegrationService integrationService;
    private EndpointService endpointService;
    private DynamicEndpointService dynamicEndpointService;
    private IntegrationCredentialService integrationCredentialService;
    private HttpServletRequest request;

    private DynamicEndpointController controller;

    @BeforeEach
    void setUp() {
        integrationService =
                mock(IntegrationService.class);

        endpointService =
                mock(EndpointService.class);

        dynamicEndpointService =
                mock(DynamicEndpointService.class);

        integrationCredentialService =
                mock(IntegrationCredentialService.class);

        request =
                mock(HttpServletRequest.class);

        controller =
                new DynamicEndpointController(
                        integrationService,
                        endpointService,
                        dynamicEndpointService,
                        integrationCredentialService
                );
    }

    @Test
    void shouldExecuteGetWithoutAuthentication() {
        Integration integration =
                createIntegration(
                        1L,
                        "/api/clientes",
                        "NONE"
                );

        Endpoint endpoint =
                createEndpoint(
                        10L,
                        1L,
                        "/buscar"
                );

        Map<String, String> parameters =
                Map.of(
                        "codigo",
                        "10"
                );

        List<Map<String, Object>> result =
                List.of(
                        Map.of(
                                "CODIGO",
                                10
                        )
                );

        when(request.getRequestURI())
                .thenReturn(
                        "/api/clientes/buscar");

        when(integrationService
                .findBestMatchByRequestPath(
                        "/api/clientes/buscar"))
                .thenReturn(
                        Optional.of(
                                integration));

        when(endpointService
                .findByIntegrationIdAndPathAndMethod(
                        1L,
                        "/buscar",
                        "GET"))
                .thenReturn(
                        Optional.of(
                                endpoint));

        when(dynamicEndpointService.executeGet(
                endpoint,
                parameters))
                .thenReturn(
                        result);

        ResponseEntity<?> response =
                controller.executeGet(
                        request,
                        parameters);

        assertEquals(
                200,
                response.getStatusCode()
                        .value());

        assertEquals(
                result,
                response.getBody());

        verify(integrationCredentialService, never())
                .authenticate(
                        1L,
                        null);

        verify(dynamicEndpointService)
                .executeGet(
                        endpoint,
                        parameters);
    }

    @Test
    void shouldExecuteGetWithValidApiKey() {
        Integration integration =
                createIntegration(
                        1L,
                        "/api/clientes",
                        "API_KEY"
                );

        IntegrationCredential credential =
                createCredential(
                        100L,
                        1L,
                        "Sistema Tasy"
                );

        Endpoint endpoint =
                createEndpoint(
                        10L,
                        1L,
                        "/buscar"
                );

        Map<String, String> parameters =
                Map.of(
                        "codigo",
                        "10"
                );

        List<Map<String, Object>> result =
                List.of(
                        Map.of(
                                "CODIGO",
                                10
                        )
                );

        when(request.getRequestURI())
                .thenReturn(
                        "/api/clientes/buscar");

        when(request.getHeader(
                "X-API-Key"))
                .thenReturn(
                        "ihub-chave-valida");

        when(integrationService
                .findBestMatchByRequestPath(
                        "/api/clientes/buscar"))
                .thenReturn(
                        Optional.of(
                                integration));

        when(integrationCredentialService
                .authenticate(
                        1L,
                        "ihub-chave-valida"))
                .thenReturn(
                        Optional.of(
                                credential));

        when(endpointService
                .findByIntegrationIdAndPathAndMethod(
                        1L,
                        "/buscar",
                        "GET"))
                .thenReturn(
                        Optional.of(
                                endpoint));

        when(dynamicEndpointService.executeGet(
                endpoint,
                parameters))
                .thenReturn(
                        result);

        ResponseEntity<?> response =
                controller.executeGet(
                        request,
                        parameters);

        assertEquals(
                200,
                response.getStatusCode()
                        .value());

        assertEquals(
                result,
                response.getBody());

        verify(integrationCredentialService)
                .authenticate(
                        1L,
                        "ihub-chave-valida");

        verify(dynamicEndpointService)
                .executeGet(
                        endpoint,
                        parameters);
    }

    @Test
    void shouldRejectInvalidApiKey() {
        Integration integration =
                createIntegration(
                        1L,
                        "/api/clientes",
                        "API_KEY"
                );

        when(request.getRequestURI())
                .thenReturn(
                        "/api/clientes/buscar");

        when(request.getHeader(
                "X-API-Key"))
                .thenReturn(
                        "ihub-chave-invalida");

        when(integrationService
                .findBestMatchByRequestPath(
                        "/api/clientes/buscar"))
                .thenReturn(
                        Optional.of(
                                integration));

        when(integrationCredentialService
                .authenticate(
                        1L,
                        "ihub-chave-invalida"))
                .thenReturn(
                        Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.executeGet(
                                request,
                                Map.of())
                );

        assertEquals(
                401,
                exception.getStatusCode()
                        .value());

        assertEquals(
                "API Key inválida",
                exception.getReason());

        verify(endpointService, never())
                .findByIntegrationIdAndPathAndMethod(
                        1L,
                        "/buscar",
                        "GET");
    }

    @Test
    void shouldRejectMissingApiKeyWithGenericMessage() {
        Integration integration =
                createIntegration(
                        1L,
                        "/api/clientes",
                        "API_KEY"
                );

        when(request.getRequestURI())
                .thenReturn(
                        "/api/clientes/buscar");

        when(request.getHeader(
                "X-API-Key"))
                .thenReturn(
                        null);

        when(integrationService
                .findBestMatchByRequestPath(
                        "/api/clientes/buscar"))
                .thenReturn(
                        Optional.of(
                                integration));

        when(integrationCredentialService
                .authenticate(
                        1L,
                        null))
                .thenReturn(
                        Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.executeGet(
                                request,
                                Map.of())
                );

        assertEquals(
                401,
                exception.getStatusCode()
                        .value());

        assertEquals(
                "API Key inválida",
                exception.getReason());

        verify(integrationCredentialService)
                .authenticate(
                        1L,
                        null);

        verify(endpointService, never())
                .findByIntegrationIdAndPathAndMethod(
                        1L,
                        "/buscar",
                        "GET");
    }

    @Test
    void shouldReturnNotFoundWhenIntegrationDoesNotExist() {
        when(request.getRequestURI())
                .thenReturn(
                        "/api/inexistente/buscar");

        when(integrationService
                .findBestMatchByRequestPath(
                        "/api/inexistente/buscar"))
                .thenReturn(
                        Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.executeGet(
                                request,
                                Map.of())
                );

        assertEquals(
                404,
                exception.getStatusCode()
                        .value());

        assertEquals(
                "Integração não encontrada",
                exception.getReason());

        verify(integrationCredentialService, never())
                .authenticate(
                        1L,
                        null);
    }

    @Test
    void shouldReturnNotFoundWhenEndpointDoesNotExist() {
        Integration integration =
                createIntegration(
                        1L,
                        "/api/clientes",
                        "NONE"
                );

        when(request.getRequestURI())
                .thenReturn(
                        "/api/clientes/inexistente");

        when(integrationService
                .findBestMatchByRequestPath(
                        "/api/clientes/inexistente"))
                .thenReturn(
                        Optional.of(
                                integration));

        when(endpointService
                .findByIntegrationIdAndPathAndMethod(
                        1L,
                        "/inexistente",
                        "GET"))
                .thenReturn(
                        Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.executeGet(
                                request,
                                Map.of())
                );

        assertEquals(
                404,
                exception.getStatusCode()
                        .value());

        assertEquals(
                "Endpoint não encontrado",
                exception.getReason());

        verify(
                dynamicEndpointService,
                never())
                .executeGet(
                        any(Endpoint.class),
                        anyMap());
    }

    @Test
    void shouldNormalizeRootEndpointPath() {
        Integration integration =
                createIntegration(
                        1L,
                        "/api/clientes",
                        "NONE"
                );

        Endpoint endpoint =
                createEndpoint(
                        10L,
                        1L,
                        "/"
                );

        when(request.getRequestURI())
                .thenReturn(
                        "/api/clientes");

        when(integrationService
                .findBestMatchByRequestPath(
                        "/api/clientes"))
                .thenReturn(
                        Optional.of(
                                integration));

        when(endpointService
                .findByIntegrationIdAndPathAndMethod(
                        1L,
                        "/",
                        "GET"))
                .thenReturn(
                        Optional.of(
                                endpoint));

        when(dynamicEndpointService.executeGet(
                endpoint,
                Map.of()))
                .thenReturn(
                        List.of());

        ResponseEntity<?> response =
                controller.executeGet(
                        request,
                        Map.of());

        assertEquals(
                200,
                response.getStatusCode()
                        .value());

        verify(endpointService)
                .findByIntegrationIdAndPathAndMethod(
                        1L,
                        "/",
                        "GET");
    }

    private Integration createIntegration(
            Long id,
            String basePath,
            String authType) {

        Integration integration =
                new Integration();

        integration.setId(id);
        integration.setName(
                "Integração Teste");
        integration.setBasePath(
                basePath);
        integration.setActive(
                "S");
        integration.setAuthType(
                authType);

        return integration;
    }

    private Endpoint createEndpoint(
            Long id,
            Long integrationId,
            String path) {

        Endpoint endpoint =
                new Endpoint();

        endpoint.setId(id);
        endpoint.setIntegrationId(
                integrationId);
        endpoint.setName(
                "Endpoint Teste");
        endpoint.setPath(
                path);
        endpoint.setMethod(
                "GET");
        endpoint.setActive(
                "S");

        return endpoint;
    }

    private IntegrationCredential createCredential(
            Long id,
            Long integrationId,
            String name) {

        IntegrationCredential credential =
                new IntegrationCredential();

        credential.setId(id);
        credential.setIntegrationId(
                integrationId);
        credential.setName(
                name);
        credential.setActive(
                "S");

        return credential;
    }
}
