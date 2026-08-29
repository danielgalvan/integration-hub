package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.ApiKeyResponse;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.repository.EndpointRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import br.com.integrationhub.security.ApiKeyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final EndpointRepository endpointRepository;
    private final ApiKeyService apiKeyService;

    public IntegrationService(
            IntegrationRepository integrationRepository,
            EndpointRepository endpointRepository,
            ApiKeyService apiKeyService) {

        this.integrationRepository = integrationRepository;
        this.endpointRepository = endpointRepository;
        this.apiKeyService = apiKeyService;
    }

    public List<Integration> findAll() {
        return integrationRepository.findAll();
    }

    public Optional<Integration> findById(Long id) {
        return integrationRepository.findById(id);
    }

    public Optional<Integration> findByBasePath(
            String basePath) {

        return integrationRepository.findByBasePath(
                basePath);
    }

    public Optional<Integration> findBestMatchByRequestPath(
            String requestPath) {

        return integrationRepository
                .findBestMatchByRequestPath(
                        requestPath);
    }

    public Integration save(
            Integration integration) {

        validateBasePath(
                integration.getBasePath());

        if (integration.getActive() == null) {
            integration.setActive("S");
        }

        if (integration.getAuthType() == null) {
            integration.setAuthType("NONE");
        }

        if (integration.getCreatedBy() == null) {
            integration.setCreatedBy("SYSTEM");
        }

        return integrationRepository.save(
                integration);
    }

    public Integration update(
            Long id,
            Integration integration) {

        Integration current = integrationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Integração não encontrada: " + id));

        validateBasePath(
                integration.getBasePath());

        if (integration.getActive() == null) {
            integration.setActive(
                    current.getActive());
        }

        if (integration.getAuthType() == null) {
            integration.setAuthType(
                    current.getAuthType());
        }

        if (integration.getUpdatedBy() == null) {
            integration.setUpdatedBy("SYSTEM");
        }

        return integrationRepository.update(
                id,
                integration);
    }

    public ApiKeyResponse generateApiKey(
            Long id) {

        Integration integration = integrationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Integração não encontrada: " + id));

        if (!"API_KEY".equals(
                integration.getAuthType())) {

            throw new IllegalStateException(
                    "A integração não está configurada para utilizar API Key");
        }

        String apiKey = apiKeyService.generateApiKey();

        String apiKeyHash = apiKeyService.hashApiKey(
                apiKey);

        LocalDateTime createdAt = LocalDateTime.now();

        integrationRepository.updateApiKey(
                id,
                apiKeyHash,
                createdAt);

        return new ApiKeyResponse(
                apiKey);
    }

    public boolean validateApiKey(
            Integration integration,
            String apiKey) {

        if (!"API_KEY".equals(
                integration.getAuthType())) {

            return true;
        }

        return apiKeyService.matches(
                apiKey,
                integration.getApiKeyHash());
    }

    public void delete(Long id) {

        integrationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Integração não encontrada: " + id));

        if (!endpointRepository
                .findByIntegrationId(id)
                .isEmpty()) {

            throw new IllegalStateException(
                    "A integração possui endpoints vinculados");
        }

        integrationRepository.deleteById(id);
    }

    private void validateBasePath(
            String basePath) {

        if (basePath == null ||
                basePath.isBlank()) {

            throw new IllegalArgumentException(
                    "basePath é obrigatório");
        }

        if (!basePath.startsWith("/api/")) {
            throw new IllegalArgumentException(
                    "basePath deve iniciar com /api/");
        }

        if (basePath.endsWith("/")) {
            throw new IllegalArgumentException(
                    "basePath não deve terminar com /");
        }

        if (basePath.contains(" ")) {
            throw new IllegalArgumentException(
                    "basePath não deve conter espaços");
        }
    }
}
