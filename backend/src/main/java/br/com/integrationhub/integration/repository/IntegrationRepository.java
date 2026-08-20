package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.IntegrationEndpoint;

import java.util.List;
import java.util.Optional;

public interface IntegrationRepository {

    List<IntegrationEndpoint> findAll();

    Optional<IntegrationEndpoint> findByPath(String path);

    IntegrationEndpoint save(IntegrationEndpoint integrationEndpoint);
}