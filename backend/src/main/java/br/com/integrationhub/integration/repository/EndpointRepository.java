package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Endpoint;

import java.util.List;
import java.util.Optional;

public interface EndpointRepository {

    List<Endpoint> findAll();

    Optional<Endpoint> findById(Long id);

    List<Endpoint> findByIntegrationId(Long integrationId);

    Optional<Endpoint> findByIntegrationIdAndPathAndMethod(
            Long integrationId,
            String path,
            String method
    );

    Endpoint save(Endpoint endpoint);

    void deleteById(Long id);
}