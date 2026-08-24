package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.repository.EndpointRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final EndpointRepository endpointRepository;

    public IntegrationService(
            IntegrationRepository integrationRepository,
            EndpointRepository endpointRepository) {

        this.integrationRepository = integrationRepository;
        this.endpointRepository = endpointRepository;
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
                basePath
        );
    }

    public Optional<Integration> findBestMatchByRequestPath(
            String requestPath) {

        return integrationRepository
                .findBestMatchByRequestPath(
                        requestPath
                );
    }

    public Integration save(Integration integration) {

        validateBasePath(
                integration.getBasePath()
        );

        if (integration.getActive() == null) {
            integration.setActive("S");
        }

        if (integration.getCreatedBy() == null) {
            integration.setCreatedBy("SYSTEM");
        }

        return integrationRepository.save(
                integration
        );
    }

    public Integration update(
            Long id,
            Integration integration) {

        Integration current = integrationRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Integração não encontrada: " + id
                        )
                );

        validateBasePath(
                integration.getBasePath()
        );

        if (integration.getActive() == null) {
            integration.setActive(
                    current.getActive()
            );
        }

        if (integration.getUpdatedBy() == null) {
            integration.setUpdatedBy("SYSTEM");
        }

        return integrationRepository.update(
                id,
                integration
        );
    }

    public void delete(Long id) {

        integrationRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Integração não encontrada: " + id
                        )
                );

        if (!endpointRepository
                .findByIntegrationId(id)
                .isEmpty()) {

            throw new IllegalStateException(
                    "A integração possui endpoints vinculados"
            );
        }

        integrationRepository.deleteById(id);
    }

    private void validateBasePath(String basePath) {

        if (basePath == null || basePath.isBlank()) {
            throw new IllegalArgumentException(
                    "basePath é obrigatório"
            );
        }

        if (!basePath.startsWith("/api/")) {
            throw new IllegalArgumentException(
                    "basePath deve iniciar com /api/"
            );
        }

        if (basePath.endsWith("/")) {
            throw new IllegalArgumentException(
                    "basePath não deve terminar com /"
            );
        }

        if (basePath.contains(" ")) {
            throw new IllegalArgumentException(
                    "basePath não deve conter espaços"
            );
        }
    }
}