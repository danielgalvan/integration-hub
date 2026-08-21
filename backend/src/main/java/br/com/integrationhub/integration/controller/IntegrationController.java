package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Integration;
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
    public List<Integration> findAll() {
        return integrationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Integration> findById(
            @PathVariable("id") Long id) {

        return integrationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Integration> save(
            @RequestBody Integration integration) {

        Integration saved = integrationService.save(integration);

        return ResponseEntity.ok(saved);
    }
}