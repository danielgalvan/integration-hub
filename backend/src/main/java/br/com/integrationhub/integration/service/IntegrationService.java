package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Integration;
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

    public List<Integration> findAll() {
        return integrationRepository.findAll();
    }

    public Optional<Integration> findById(Long id) {
        return integrationRepository.findById(id);
    }

    public Optional<Integration> findByBasePath(String basePath) {
        return integrationRepository.findByBasePath(basePath);
    }

    public Integration save(Integration integration) {
        return integrationRepository.save(integration);
    }
}