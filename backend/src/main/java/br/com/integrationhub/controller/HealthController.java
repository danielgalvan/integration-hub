package br.com.integrationhub.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.integrationhub.service.DatabaseHealthService;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DatabaseHealthService databaseHealthService;

    public HealthController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        boolean databaseOnline = databaseHealthService.isDatabaseOnline();

        return Map.of(
            "status", "OK",
            "database", databaseOnline ? "Online" : "Offline"
        );
    }
}