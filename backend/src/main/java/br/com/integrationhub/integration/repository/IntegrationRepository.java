package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Integration;

import java.util.List;
import java.util.Optional;

public interface IntegrationRepository {

    List<Integration> findAll();

    Optional<Integration> findById(Long id);

    Optional<Integration> findByBasePath(String basePath);

    Optional<Integration> findBestMatchByRequestPath(String requestPath);

    Integration save(Integration integration);

    Integration update(Long id, Integration integration);

    void deleteById(Long id);
}