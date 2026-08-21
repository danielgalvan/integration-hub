package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Integration;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryIntegrationRepository implements IntegrationRepository {

    private final Map<Long, Integration> integrations = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public List<Integration> findAll() {
        return new ArrayList<>(integrations.values());
    }

    @Override
    public Optional<Integration> findById(Long id) {
        return Optional.ofNullable(integrations.get(id));
    }

    @Override
    public Optional<Integration> findByBasePath(String basePath) {
        return integrations.values()
                .stream()
                .filter(integration -> integration.getBasePath().equals(basePath))
                .findFirst();
    }

    @Override
    public Integration save(Integration integration) {

        if (integration.getId() == null) {
            integration.setId(sequence.incrementAndGet());
        }

        integrations.put(integration.getId(), integration);

        return integration;
    }
}