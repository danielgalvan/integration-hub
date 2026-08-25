package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.service.EndpointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
public class EndpointController {

    private final EndpointService endpointService;

    public EndpointController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @GetMapping
    public List<Endpoint> findAll() {
        return endpointService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endpoint> findById(
            @PathVariable("id") Long id) {

        return endpointService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/integration/{integrationId}")
    public List<Endpoint> findByIntegrationId(
            @PathVariable("integrationId") Long integrationId) {

        return endpointService.findByIntegrationId(integrationId);
    }

    @PostMapping
    public ResponseEntity<Endpoint> save(
            @RequestBody Endpoint endpoint) {

        Endpoint saved = endpointService.save(endpoint);

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id) {

        endpointService.delete(id);

        return ResponseEntity.noContent().build();
    }
}