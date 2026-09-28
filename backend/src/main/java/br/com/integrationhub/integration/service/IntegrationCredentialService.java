package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.model.IntegrationCredential;
import br.com.integrationhub.integration.repository.IntegrationCredentialRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import br.com.integrationhub.security.ApiKeyEncryptionService;
import br.com.integrationhub.security.ApiKeyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntegrationCredentialService {

    private final IntegrationCredentialRepository credentialRepository;
    private final IntegrationRepository integrationRepository;
    private final ApiKeyService apiKeyService;
    private final ApiKeyEncryptionService apiKeyEncryptionService;

    public IntegrationCredentialService(
            IntegrationCredentialRepository credentialRepository,
            IntegrationRepository integrationRepository,
            ApiKeyService apiKeyService,
            ApiKeyEncryptionService apiKeyEncryptionService) {

        this.credentialRepository = credentialRepository;
        this.integrationRepository = integrationRepository;
        this.apiKeyService = apiKeyService;
        this.apiKeyEncryptionService = apiKeyEncryptionService;
    }

    public List<IntegrationCredential> findByIntegrationId(
            Long integrationId) {

        getIntegration(integrationId);

        return credentialRepository.findByIntegrationId(
                integrationId);
    }

    public IntegrationCredential findById(
            Long integrationId,
            Long credentialId) {

        getIntegration(integrationId);

        return getCredential(
                integrationId,
                credentialId);
    }

    public IntegrationCredential create(
            Long integrationId,
            String name,
            String username) {

        Integration integration =
                getIntegration(integrationId);

        if (!"API_KEY".equals(
                integration.getAuthType())) {

            throw new IllegalStateException(
                    "A integração não está configurada para utilizar API Key"
            );
        }

        String apiKey =
                apiKeyService.generateApiKey();

        IntegrationCredential credential =
                new IntegrationCredential();

        credential.setIntegrationId(
                integrationId);

        credential.setName(
                name);

        credential.setApiKeyEncrypted(
                apiKeyEncryptionService.encrypt(
                        apiKey)
        );

        credential.setActive(
                "S");

        credential.setCreatedBy(
                username);

        return credentialRepository.save(
                credential);
    }

    public String getApiKey(
            Long integrationId,
            Long credentialId) {

        IntegrationCredential credential =
                getCredential(
                        integrationId,
                        credentialId);

        return apiKeyEncryptionService.decrypt(
                credential.getApiKeyEncrypted()
        );
    }

    public IntegrationCredential updateName(
            Long integrationId,
            Long credentialId,
            String name,
            String username) {

        getCredential(
                integrationId,
                credentialId);

        return credentialRepository.updateName(
                credentialId,
                name,
                username
        );
    }

    public IntegrationCredential setActive(
            Long integrationId,
            Long credentialId,
            boolean active,
            String username) {

        getCredential(
                integrationId,
                credentialId);

        return credentialRepository.updateActive(
                credentialId,
                active ? "S" : "N",
                username
        );
    }

    public String regenerateApiKey(
            Long integrationId,
            Long credentialId,
            String username) {

        getCredential(
                integrationId,
                credentialId);

        String apiKey =
                apiKeyService.generateApiKey();

        String apiKeyEncrypted =
                apiKeyEncryptionService.encrypt(
                        apiKey);

        credentialRepository.updateApiKey(
                credentialId,
                apiKeyEncrypted,
                username
        );

        return apiKey;
    }

    private IntegrationCredential getCredential(
            Long integrationId,
            Long credentialId) {

        IntegrationCredential credential =
                credentialRepository.findById(
                                credentialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credencial não encontrada"
                                )
                        );

        if (!integrationId.equals(
                credential.getIntegrationId())) {

            throw new IllegalArgumentException(
                    "Credencial não encontrada"
            );
        }

        return credential;
    }

    private Integration getIntegration(
            Long integrationId) {

        return integrationRepository.findById(
                        integrationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Integração não encontrada"
                        )
                );
    }
}
