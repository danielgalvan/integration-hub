
package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.service.ExecutionQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/executions")
public class ExecutionAdminController {

    private final ExecutionQueryService executionQueryService;

    public ExecutionAdminController(
            ExecutionQueryService executionQueryService) {

        this.executionQueryService = executionQueryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(required = false) Long integrationId,
            @RequestParam(required = false) Long endpointId,
            @RequestParam(required = false) String executionStatus,
            @RequestParam(required = false) LocalDateTime startedFrom,
            @RequestParam(required = false) LocalDateTime startedTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        List<Execution> executions = executionQueryService.findAll(
                integrationId,
                endpointId,
                executionStatus,
                startedFrom,
                startedTo,
                page,
                size
        );

        long totalElements = executionQueryService.count(
                integrationId,
                endpointId,
                executionStatus,
                startedFrom,
                startedTo
        );

        int totalPages = (int) Math.ceil(
                (double) totalElements / size
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", executions);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Execution> findById(
            @PathVariable Long id) {

        return executionQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
