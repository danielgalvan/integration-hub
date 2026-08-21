package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Endpoint;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryEndpointRepository implements EndpointRepository {

    private final Map<Long, Endpoint> endpoints = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public List<Endpoint> findAll() {
        return new ArrayList<>(endpoints.values());
    }

    @Override
    public Optional<Endpoint> findById(Long id) {
        return Optional.ofNullable(endpoints.get(id));
    }

    @Override
    public List<Endpoint> findByIntegrationId(Long integrationId) {
        return endpoints.values()
                .stream()
                .filter(endpoint ->
                        endpoint.getIntegrationId().equals(integrationId))
                .toList();
    }

    @Override
    public Endpoint save(Endpoint endpoint) {

        if (endpoint.getId() == null) {
            endpoint.setId(sequence.incrementAndGet());
        }

        endpoints.put(endpoint.getId(), endpoint);

        return endpoint;
    }
}