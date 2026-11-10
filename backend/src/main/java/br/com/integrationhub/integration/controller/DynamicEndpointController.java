
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
public class DynamicEndpointController {

    private static final String API_KEY_HEADER = "X-API-Key";

    private static final Set<String> SENSITIVE_PARAMETERS =
            Set.of(
                    "apikey",
                    "api_key",
                    "api-key",
                    "password",
                    "senha",
                    "token",
                    "secret",
                    "authorization",
                    "access_token",
                    "refresh_token"
            );

    private final IntegrationService integrationService;
    private final EndpointService endpointService;
    private final DynamicEndpointService dynamicEndpointService;
    private final IntegrationCredentialService integrationCredentialService;
    private final ExecutionAuditService executionAuditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DynamicEndpointController(
            IntegrationService integrationService,
            EndpointService endpointService,
            DynamicEndpointService dynamicEndpointService,
            IntegrationCredentialService integrationCredentialService,
            ExecutionAuditService executionAuditService) {

        this.integrationService = integrationService;
        this.endpointService = endpointService;
        this.dynamicEndpointService = dynamicEndpointService;
        this.integrationCredentialService = integrationCredentialService;
        this.executionAuditService = executionAuditService;
    }

    @GetMapping("/api/**")
    public ResponseEntity<?> executeGet(
            HttpServletRequest request,
            @RequestParam Map<String, String> requestParameters) {

        long startedNanos = System.nanoTime();

        Execution execution = new Execution();

        execution.setStartedAt(LocalDateTime.now());
        execution.setRequestMethod("GET");
        execution.setRequestPath(request.getRequestURI());
        execution.setRequestParameters(
                serializeParameters(requestParameters)
        );

        try {

            String requestPath = request.getRequestURI();

            Integration integration =
                    integrationService
                            .findBestMatchByRequestPath(requestPath)
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Integração não encontrada"
                                    )
                            );

            execution.setIntegrationId(integration.getId());
            execution.setIntegrationName(integration.getName());

            IntegrationCredential credential =
                    validateAuthentication(
                            request,
                            integration
                    );

            if (credential != null) {

                execution.setCredentialId(
                        credential.getId()
                );

                execution.setCredentialName(
                        credential.getName()
                );
            }

            String endpointPath =
                    requestPath.substring(
                            integration.getBasePath().length()
                    );

            endpointPath = normalizePath(endpointPath);

            Endpoint endpoint =
                    endpointService
                            .findByIntegrationIdAndPathAndMethod(
                                    integration.getId(),
                                    endpointPath,
                                    "GET"
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Endpoint não encontrado"
                                    )
                            );

            execution.setEndpointId(endpoint.getId());
            execution.setEndpointName(endpoint.getName());

            Object result = dynamicEndpointService.executeGet(
                    endpoint,
                    requestParameters
            );

            execution.setHttpStatus(HttpStatus.OK.value());
            execution.setExecutionStatus("SUCCESS");

            return ResponseEntity.ok(result);

        } catch (ResponseStatusException exception) {

            int httpStatus =
                    exception.getStatusCode().value();

            execution.setHttpStatus(httpStatus);
            execution.setExecutionStatus(
                    resolveExecutionStatus(httpStatus)
            );

            execution.setErrorType(
                    exception.getClass().getSimpleName()
            );

            execution.setErrorMessage(
                    exception.getReason()
            );

            throw exception;

        } catch (IllegalArgumentException exception) {

            execution.setHttpStatus(
                    HttpStatus.BAD_REQUEST.value()
            );

            execution.setExecutionStatus("ERROR");

            execution.setErrorType(
                    exception.getClass().getSimpleName()
            );

            execution.setErrorMessage(
                    exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            execution.setHttpStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );

            execution.setExecutionStatus("ERROR");

            execution.setErrorType(
                    exception.getClass().getSimpleName()
            );

            execution.setErrorMessage(
                    exception.getMessage()
            );

            throw exception;

        } finally {

            execution.setFinishedAt(LocalDateTime.now());

            execution.setDurationMs(
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startedNanos
                    )
            );

            try {
                executionAuditService.register(execution);
            } catch (Exception exception) {
                // Uma falha na auditoria não deve alterar o resultado do endpoint.
                org.slf4j.LoggerFactory
                .getLogger(DynamicEndpointController.class)
                .error("Falha inesperada ao registrar auditoria da execução", exception);
            }
        }
    }

    private String resolveExecutionStatus(int httpStatus) {

        if (httpStatus == HttpStatus.UNAUTHORIZED.value()) {
            return "UNAUTHORIZED";
        }

        if (httpStatus == HttpStatus.NOT_FOUND.value()) {
            return "NOT_FOUND";
        }

        return "ERROR";
    }

    private String serializeParameters(
            Map<String, String> requestParameters) {

        try {

            Map<String, String> sanitizedParameters =
                    new java.util.LinkedHashMap<>();

            if (requestParameters != null) {

                requestParameters.forEach((name, value) -> {

                    if (name == null) {
                        return;
                    }

                    String normalizedName =
                            name.toLowerCase(
                                    java.util.Locale.ROOT
                            );

                    boolean sensitive =
                            SENSITIVE_PARAMETERS.contains(
                                    normalizedName
                            );

                    sanitizedParameters.put(
                            name,
                            sensitive ? "[REDACTED]" : value
                    );
                });
            }

            return objectMapper.writeValueAsString(
                    sanitizedParameters
            );

        } catch (JsonProcessingException exception) {

            return "{}";
        }
    }

    private IntegrationCredential validateAuthentication(
            HttpServletRequest request,
            Integration integration) {

        if (!"API_KEY".equals(integration.getAuthType())) {
            return null;
        }

        String apiKey =
                request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "API Key inválida"
            );
        }

        Optional<IntegrationCredential> credential =
                integrationCredentialService.authenticate(
                        integration.getId(),
                        apiKey
                );

        if (credential.isPresent()) {
            return credential.get();
        }

        if (integrationService.validateApiKey(
                integration,
                apiKey
        )) {

            return null;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "API Key inválida"
        );
    }

    private String normalizePath(String path) {

        if (path == null || path.isBlank()) {
            return "/";
        }

        return path.startsWith("/")
                ? path
                : "/" + path;
    }
}
