package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.repository.EndpointRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EndpointService {

    private final EndpointRepository endpointRepository;

    public EndpointService(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }

    public List<Endpoint> findAll() {
        return endpointRepository.findAll();
    }

    public Optional<Endpoint> findById(Long id) {
        return endpointRepository.findById(id);
    }

    public List<Endpoint> findByIntegrationId(Long integrationId) {
        return endpointRepository.findByIntegrationId(integrationId);
    }

    public Endpoint save(Endpoint endpoint) {
        if (endpoint.getActive() == null) {
            endpoint.setActive("S");
        }

        if (endpoint.getCreatedBy() == null) {
            endpoint.setCreatedBy("SYSTEM");
        }

        return endpointRepository.save(endpoint);
    }
}