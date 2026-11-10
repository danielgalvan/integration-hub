
package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.model.IntegrationCredential;
import br.com.integrationhub.integration.service.DynamicEndpointService;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.ExecutionAuditService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DynamicEndpointControllerTest {

    private IntegrationService integrationService;
    private EndpointService endpointService;
    private DynamicEndpointService dynamicEndpointService;
    private IntegrationCredentialService integrationCredentialService;
    private ExecutionAuditService executionAuditService;
    private HttpServletRequest request;

    private DynamicEndpointController controller;

    @BeforeEach
    void setUp() {
        integrationService = mock(IntegrationService.class);
        endpointService = mock(EndpointService.class);
        dynamicEndpointService = mock(DynamicEndpointService.class);
        integrationCredentialService = mock(IntegrationCredentialService.class);
        executionAuditService = mock(ExecutionAuditService.class);
        request = mock(HttpServletRequest.class);

        controller = new DynamicEndpointController(
                integrationService,
                endpointService,
                dynamicEndpointService,
                integrationCredentialService,
                executionAuditService
        );
    }

    @Test
    void shouldExecuteGetWithoutAuthentication() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "NONE");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        Map<String, String> parameters = Map.of("codigo", "10");
        List<Map<String, Object>> result =
                List.of(Map.of("CODIGO", 10));

        configureRequest("/api/clientes/buscar");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(endpoint, parameters))
                .thenReturn(result);

        ResponseEntity<?> response =
                controller.executeGet(request, parameters);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(result, response.getBody());

        verify(integrationCredentialService, never())
                .authenticate(1L, null);

        verify(integrationService, never())
                .validateApiKey(integration, null);

        verify(dynamicEndpointService)
                .executeGet(endpoint, parameters);

        Execution execution = captureExecution();

        assertEquals("SUCCESS", execution.getExecutionStatus());
        assertEquals(200, execution.getHttpStatus());
        assertEquals(1L, execution.getIntegrationId());
        assertEquals(10L, execution.getEndpointId());
        assertEquals("GET", execution.getRequestMethod());
        assertEquals("/api/clientes/buscar", execution.getRequestPath());
        assertEquals("{\"codigo\":\"10\"}",
                execution.getRequestParameters());
        assertNotNull(execution.getStartedAt());
        assertNotNull(execution.getFinishedAt());
        assertNotNull(execution.getDurationMs());
        assertTrue(execution.getDurationMs() >= 0);
    }

    @Test
    void shouldExecuteGetWithValidApiKey() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");
        IntegrationCredential credential =
                createCredential(100L, 1L, "Sistema Externo");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        Map<String, String> parameters = Map.of("codigo", "10");
        List<Map<String, Object>> result =
                List.of(Map.of("CODIGO", 10));

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("ihub-chave-valida");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(integrationCredentialService.authenticate(
                1L, "ihub-chave-valida"))
                .thenReturn(Optional.of(credential));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(endpoint, parameters))
                .thenReturn(result);

        ResponseEntity<?> response =
                controller.executeGet(request, parameters);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(result, response.getBody());

        verify(integrationCredentialService)
                .authenticate(1L, "ihub-chave-valida");

        verify(integrationService, never())
                .validateApiKey(integration, "ihub-chave-valida");

        Execution execution = captureExecution();

        assertEquals("SUCCESS", execution.getExecutionStatus());
        assertEquals(100L, execution.getCredentialId());
        assertEquals("Sistema Externo", execution.getCredentialName());
        assertEquals(200, execution.getHttpStatus());
    }

    @Test
    void shouldRejectInvalidApiKey() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("ihub-chave-invalida");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(integrationCredentialService.authenticate(
                1L, "ihub-chave-invalida"))
                .thenReturn(Optional.empty());

        when(integrationService.validateApiKey(
                integration, "ihub-chave-invalida"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("API Key inválida", exception.getReason());

        verify(integrationService)
                .validateApiKey(integration, "ihub-chave-invalida");

        verify(endpointService, never())
                .findByIntegrationIdAndPathAndMethod(
                        1L, "/buscar", "GET");

        Execution execution = captureExecution();

        assertEquals("UNAUTHORIZED", execution.getExecutionStatus());
        assertEquals(401, execution.getHttpStatus());
        assertEquals(1L, execution.getIntegrationId());
        assertNull(execution.getEndpointId());
        assertEquals("API Key inválida", execution.getErrorMessage());
    }

    @Test
    void shouldRejectMissingApiKeyWithGenericMessage() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");

        configureRequest("/api/clientes/buscar");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("API Key inválida", exception.getReason());

        verify(integrationCredentialService, never())
                .authenticate(1L, null);

        verify(integrationService, never())
                .validateApiKey(integration, null);

        Execution execution = captureExecution();

        assertEquals("UNAUTHORIZED", execution.getExecutionStatus());
        assertEquals(401, execution.getHttpStatus());
    }

    @Test
    void shouldReturnNotFoundWhenIntegrationDoesNotExist() {
        configureRequest("/api/inexistente/buscar");

        when(integrationService.findBestMatchByRequestPath(
                "/api/inexistente/buscar"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Integração não encontrada",
                exception.getReason());

        Execution execution = captureExecution();

        assertEquals("NOT_FOUND", execution.getExecutionStatus());
        assertEquals(404, execution.getHttpStatus());
        assertNull(execution.getIntegrationId());
        assertNull(execution.getEndpointId());
    }

    @Test
    void shouldReturnNotFoundWhenEndpointDoesNotExist() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "NONE");

        configureRequest("/api/clientes/inexistente");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/inexistente"))
                .thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/inexistente", "GET"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Endpoint não encontrado",
                exception.getReason());

        verify(dynamicEndpointService, never())
                .executeGet(any(Endpoint.class), anyMap());

        Execution execution = captureExecution();

        assertEquals("NOT_FOUND", execution.getExecutionStatus());
        assertEquals(404, execution.getHttpStatus());
        assertEquals(1L, execution.getIntegrationId());
        assertNull(execution.getEndpointId());
    }

    @Test
    void shouldNormalizeRootEndpointPath() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "NONE");
        Endpoint endpoint = createEndpoint(10L, 1L, "/");

        configureRequest("/api/clientes");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes"))
                .thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(endpoint, Map.of()))
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.executeGet(request, Map.of());

        assertEquals(200, response.getStatusCode().value());

        verify(endpointService)
                .findByIntegrationIdAndPathAndMethod(1L, "/", "GET");

        assertEquals("SUCCESS",
                captureExecution().getExecutionStatus());
    }

    @Test
    void shouldExecuteGetWithValidLegacyApiKey() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");
        integration.setLegacyApiKeyActive("S");

        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("ihub-chave-legada");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(integrationCredentialService.authenticate(
                1L, "ihub-chave-legada"))
                .thenReturn(Optional.empty());

        when(integrationService.validateApiKey(
                integration, "ihub-chave-legada"))
                .thenReturn(true);

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(endpoint, Map.of()))
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.executeGet(request, Map.of());

        assertEquals(200, response.getStatusCode().value());

        verify(integrationCredentialService)
                .authenticate(1L, "ihub-chave-legada");

        verify(integrationService)
                .validateApiKey(integration, "ihub-chave-legada");

        verify(dynamicEndpointService)
                .executeGet(endpoint, Map.of());

        Execution execution = captureExecution();

        assertEquals("SUCCESS", execution.getExecutionStatus());
        assertNull(execution.getCredentialId());
    }

    @Test
    void shouldRejectInvalidLegacyApiKey() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");
        integration.setLegacyApiKeyActive("S");

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("ihub-legada-invalida");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(integrationCredentialService.authenticate(
                1L, "ihub-legada-invalida"))
                .thenReturn(Optional.empty());

        when(integrationService.validateApiKey(
                integration, "ihub-legada-invalida"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("API Key inválida", exception.getReason());

        verify(endpointService, never())
                .findByIntegrationIdAndPathAndMethod(
                        1L, "/buscar", "GET");

        assertEquals("UNAUTHORIZED",
                captureExecution().getExecutionStatus());
    }

    @Test
    void shouldPrioritizeV2CredentialOverLegacyAuthentication() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");
        integration.setLegacyApiKeyActive("S");

        IntegrationCredential credential =
                createCredential(100L, 1L, "Sistema Externo");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("ihub-chave-v2");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(integrationCredentialService.authenticate(
                1L, "ihub-chave-v2"))
                .thenReturn(Optional.of(credential));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(endpoint, Map.of()))
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.executeGet(request, Map.of());

        assertEquals(200, response.getStatusCode().value());

        verify(integrationService, never())
                .validateApiKey(integration, "ihub-chave-v2");

        assertEquals(100L,
                captureExecution().getCredentialId());
    }

    @Test
    void shouldRejectBlankApiKeyWithoutCheckingLegacyAuthentication() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("   ");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("API Key inválida", exception.getReason());

        verify(integrationCredentialService, never())
                .authenticate(1L, "   ");

        verify(integrationService, never())
                .validateApiKey(integration, "   ");

        assertEquals("UNAUTHORIZED",
                captureExecution().getExecutionStatus());
    }

    @Test
    void shouldRejectLegacyApiKeyWhenDisabled() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");
        integration.setLegacyApiKeyActive("N");

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("ihub-chave-legada");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(integrationCredentialService.authenticate(
                1L, "ihub-chave-legada"))
                .thenReturn(Optional.empty());

        when(integrationService.validateApiKey(
                integration, "ihub-chave-legada"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("API Key inválida", exception.getReason());

        verify(integrationService)
                .validateApiKey(integration, "ihub-chave-legada");

        verify(endpointService, never())
                .findByIntegrationIdAndPathAndMethod(
                        1L, "/buscar", "GET");

        verify(dynamicEndpointService, never())
                .executeGet(any(Endpoint.class), anyMap());

        assertEquals("UNAUTHORIZED",
                captureExecution().getExecutionStatus());
    }

    @Test
    void shouldAllowV2CredentialWhenLegacyApiKeyDisabled() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "API_KEY");
        integration.setLegacyApiKeyActive("N");

        IntegrationCredential credential =
                createCredential(100L, 1L, "Sistema Externo");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        configureRequest("/api/clientes/buscar");

        when(request.getHeader("X-API-Key"))
                .thenReturn("ihub-chave-v2");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(integrationCredentialService.authenticate(
                1L, "ihub-chave-v2"))
                .thenReturn(Optional.of(credential));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(endpoint, Map.of()))
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.executeGet(request, Map.of());

        assertEquals(200, response.getStatusCode().value());

        verify(integrationCredentialService)
                .authenticate(1L, "ihub-chave-v2");

        verify(integrationService, never())
                .validateApiKey(integration, "ihub-chave-v2");

        verify(dynamicEndpointService)
                .executeGet(endpoint, Map.of());

        Execution execution = captureExecution();

        assertEquals("SUCCESS", execution.getExecutionStatus());
        assertEquals(100L, execution.getCredentialId());
    }

    @Test
    void shouldRegisterBadRequestWhenParameterIsInvalid() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "NONE");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        configureRequest("/api/clientes/buscar");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(eq(endpoint), anyMap()))
                .thenThrow(new IllegalArgumentException(
                        "Parâmetro inválido"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.executeGet(
                        request, Map.of("codigo", "invalido")));

        assertEquals("Parâmetro inválido", exception.getMessage());

        Execution execution = captureExecution();

        assertEquals("ERROR", execution.getExecutionStatus());
        assertEquals(400, execution.getHttpStatus());
        assertEquals("IllegalArgumentException",
                execution.getErrorType());
        assertEquals("Parâmetro inválido",
                execution.getErrorMessage());
    }

    @Test
    void shouldRegisterInternalServerError() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "NONE");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        configureRequest("/api/clientes/buscar");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(eq(endpoint), anyMap()))
                .thenThrow(new IllegalStateException(
                        "Falha na execução"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> controller.executeGet(request, Map.of()));

        assertEquals("Falha na execução", exception.getMessage());

        Execution execution = captureExecution();

        assertEquals("ERROR", execution.getExecutionStatus());
        assertEquals(500, execution.getHttpStatus());
        assertEquals("IllegalStateException",
                execution.getErrorType());
    }

    @Test
    void shouldMaskSensitiveRequestParameters() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "NONE");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        configureRequest("/api/clientes/buscar");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        when(dynamicEndpointService.executeGet(eq(endpoint), anyMap()))
                .thenReturn(List.of());

        Map<String, String> parameters = Map.of(
                "codigo", "10",
                "password", "senha-secreta",
                "token", "token-secreto",
                "API_KEY", "chave-secreta"
        );

        controller.executeGet(request, parameters);

        Execution execution = captureExecution();
        String serialized = execution.getRequestParameters();

        assertTrue(serialized.contains("\"codigo\":\"10\""));
        assertTrue(serialized.contains("\"password\":\"[REDACTED]\""));
        assertTrue(serialized.contains("\"token\":\"[REDACTED]\""));
        assertTrue(serialized.contains("\"API_KEY\":\"[REDACTED]\""));

        assertFalse(serialized.contains("senha-secreta"));
        assertFalse(serialized.contains("token-secreto"));
        assertFalse(serialized.contains("chave-secreta"));
    }

    @Test
    void shouldPreserveResponseWhenAuditServiceFails() {
        Integration integration =
                createIntegration(1L, "/api/clientes", "NONE");
        Endpoint endpoint = createEndpoint(10L, 1L, "/buscar");

        configureRequest("/api/clientes/buscar");

        when(integrationService.findBestMatchByRequestPath(
                "/api/clientes/buscar"))
                .thenReturn(Optional.of(integration));

        when(endpointService.findByIntegrationIdAndPathAndMethod(
                1L, "/buscar", "GET"))
                .thenReturn(Optional.of(endpoint));

        List<Map<String, Object>> result =
                List.of(Map.of("id", 1));

        when(dynamicEndpointService.executeGet(eq(endpoint), anyMap()))
                .thenReturn(result);

        doThrow(new RuntimeException("Falha simulada na auditoria"))
                .when(executionAuditService)
                .register(any(Execution.class));

        ResponseEntity<?> response = assertDoesNotThrow(
                () -> controller.executeGet(
                        request, Map.of("id", "1"))
        );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(result, response.getBody());

        verify(executionAuditService)
                .register(any(Execution.class));
    }

    private void configureRequest(String path) {
        when(request.getRequestURI()).thenReturn(path);
    }

    private Execution captureExecution() {
        org.mockito.ArgumentCaptor<Execution> captor =
                org.mockito.ArgumentCaptor.forClass(Execution.class);

        verify(executionAuditService).register(captor.capture());

        return captor.getValue();
    }

    private Integration createIntegration(
            Long id,
            String basePath,
            String authType) {

        Integration integration = new Integration();

        integration.setId(id);
        integration.setName("Integração Teste");
        integration.setBasePath(basePath);
        integration.setActive("S");
        integration.setAuthType(authType);
        integration.setLegacyApiKeyActive("N");

        return integration;
    }

    private Endpoint createEndpoint(
            Long id,
            Long integrationId,
            String path) {

        Endpoint endpoint = new Endpoint();

        endpoint.setId(id);
        endpoint.setIntegrationId(integrationId);
        endpoint.setName("Endpoint Teste");
        endpoint.setPath(path);
        endpoint.setMethod("GET");
        endpoint.setActive("S");

        return endpoint;
    }

    private IntegrationCredential createCredential(
            Long id,
            Long integrationId,
            String name) {

        IntegrationCredential credential = new IntegrationCredential();

        credential.setId(id);
        credential.setIntegrationId(integrationId);
        credential.setName(name);
        credential.setActive("S");

        return credential;
    }
}
