package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.service.DynamicEndpointService;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/**")
    public ResponseEntity<?> executeGet(
            HttpServletRequest request,
            @RequestParam Map<String, String> requestParameters) {

        String requestPath = request.getRequestURI();

        Integration integration = integrationService
                .findAll()
                .stream()
                .filter(item -> "S".equalsIgnoreCase(item.getActive()))
                .filter(item -> requestPath.startsWith(item.getBasePath()))
                .findFirst()
                .orElse(null);

        if (integration == null) {
            return ResponseEntity.notFound().build();
        }

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
                .orElse(null);

        if (endpoint == null) {
            return ResponseEntity.notFound().build();
        }

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