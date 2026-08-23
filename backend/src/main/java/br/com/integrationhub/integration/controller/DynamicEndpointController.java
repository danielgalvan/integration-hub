package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.service.DynamicEndpointService;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class DynamicEndpointController {

    private final IntegrationService integrationService;
    private final EndpointService endpointService;
    private final DynamicEndpointService dynamicEndpointService;

    public DynamicEndpointController(
            IntegrationService integrationService,
            EndpointService endpointService,
            DynamicEndpointService dynamicEndpointService) {

        this.integrationService = integrationService;
        this.endpointService = endpointService;
        this.dynamicEndpointService = dynamicEndpointService;
    }

    @GetMapping("/api/**")
    public ResponseEntity<?> executeGet(
            HttpServletRequest request,
            @RequestParam Map<String, String> requestParameters) {

        String requestPath = request.getRequestURI();

        Integration integration = integrationService
                .findBestMatchByRequestPath(requestPath)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Integração não encontrada"
                        )
                );

        String endpointPath = requestPath.substring(
                integration.getBasePath().length()
        );

        endpointPath = normalizePath(endpointPath);

        Endpoint endpoint = endpointService
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

        return ResponseEntity.ok(
                dynamicEndpointService.executeGet(
                        endpoint,
                        requestParameters
                )
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