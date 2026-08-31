package br.com.integrationhub.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

    private final DataSourceProperties properties;

    public EnvironmentController(
            DataSourceProperties properties) {

        this.properties = properties;
    }

    @GetMapping
    public List<EnvironmentResponse> findAll() {

        return properties
                .getConnections()
                .entrySet()
                .stream()
                .map(entry ->
                        new EnvironmentResponse(
                                entry.getKey(),
                                entry.getValue().getName()
                        )
                )
                .toList();
    }
}
