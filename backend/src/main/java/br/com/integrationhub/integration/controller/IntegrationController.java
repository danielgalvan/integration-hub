package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.IntegrationEndpoint;
import br.com.integrationhub.integration.service.IntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping
    public List<IntegrationEndpoint> findAll() {
        return integrationService.findAll();
    }

    @GetMapping("/{integrationPath}")
    public ResponseEntity<IntegrationEndpoint> findByPath(
            @PathVariable(name = "integrationPath") String integrationPath) {

        return integrationService.findByPath("/" + integrationPath)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<IntegrationEndpoint> save(
            @RequestBody IntegrationEndpoint integrationEndpoint) {

        return ResponseEntity.ok(
                integrationService.save(integrationEndpoint)
        );
    }
}