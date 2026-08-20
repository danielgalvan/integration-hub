package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.IntegrationEndpoint;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryIntegrationRepository implements IntegrationRepository {

    private final Map<String, IntegrationEndpoint> integrations =
            new ConcurrentHashMap<>();

    @Override
    public java.util.List<IntegrationEndpoint> findAll() {
        return new ArrayList<>(integrations.values());
    }

    @Override
    public Optional<IntegrationEndpoint> findByPath(String path) {
        return Optional.ofNullable(integrations.get(path));
    }

    @Override
    public IntegrationEndpoint save(IntegrationEndpoint integrationEndpoint) {
        integrations.put(
                integrationEndpoint.getPath(),
                integrationEndpoint
        );

        return integrationEndpoint;
    }
}