package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.IntegrationEndpoint;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IntegrationService {

    private final IntegrationRepository integrationRepository;

    public IntegrationService(IntegrationRepository integrationRepository) {
        this.integrationRepository = integrationRepository;
    }

    public List<IntegrationEndpoint> findAll() {
        return integrationRepository.findAll();
    }

    public Optional<IntegrationEndpoint> findByPath(String path) {
        return integrationRepository.findByPath(path);
    }

    public IntegrationEndpoint save(IntegrationEndpoint integrationEndpoint) {
        return integrationRepository.save(integrationEndpoint);
    }
}