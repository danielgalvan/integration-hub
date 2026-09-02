package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Integration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IntegrationRepository {

    List<Integration> findAll();

    Optional<Integration> findById(Long id);

    Optional<Integration> findBestMatchByRequestPath(
            String requestPath);

    Integration save(
            Integration integration);

    Integration update(
            Long id,
            Integration integration);

    Integration updateApiKey(
            Long id,
            String apiKeyHash,
            LocalDateTime apiKeyCreatedAt);

    void deleteById(Long id);
}
