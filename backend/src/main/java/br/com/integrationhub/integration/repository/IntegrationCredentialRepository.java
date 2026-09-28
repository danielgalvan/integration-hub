package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.IntegrationCredential;

import java.util.List;
import java.util.Optional;

public interface IntegrationCredentialRepository {

    List<IntegrationCredential> findByIntegrationId(Long integrationId);

    Optional<IntegrationCredential> findById(Long id);

    IntegrationCredential save(IntegrationCredential credential);

    IntegrationCredential updateName(
            Long id,
            String name,
            String updatedBy);

    IntegrationCredential updateActive(
            Long id,
            String active,
            String updatedBy);

    IntegrationCredential updateApiKey(
            Long id,
            String apiKeyEncrypted,
            String updatedBy);
}
