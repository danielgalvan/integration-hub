package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.repository.EndpointRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class EndpointService {

    private final EndpointRepository endpointRepository;
    private final IntegrationRepository integrationRepository;

    public EndpointService(
            EndpointRepository endpointRepository,
            IntegrationRepository integrationRepository) {

        this.endpointRepository = endpointRepository;
        this.integrationRepository = integrationRepository;
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

    public Optional<Endpoint> findByIntegrationIdAndPathAndMethod(
            Long integrationId,
            String path,
            String method) {

        return endpointRepository.findByIntegrationIdAndPathAndMethod(
                integrationId,
                path,
                method
        );
    }

    public Endpoint save(Endpoint endpoint) {

        validateIntegrationExists(endpoint.getIntegrationId());

        if (endpoint.getActive() == null) {
            endpoint.setActive("S");
        }

        if (endpoint.getCreatedBy() == null) {
            endpoint.setCreatedBy("SYSTEM");
        }

        return endpointRepository.save(endpoint);
    }

    public Endpoint update(
            Long id,
            Endpoint endpoint) {

        Endpoint existing = endpointRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Endpoint não encontrado"
                        )
                );

        validateIntegrationExists(endpoint.getIntegrationId());

        endpoint.setId(id);

        endpoint.setCreatedBy(
                existing.getCreatedBy()
        );

        endpoint.setCreatedAt(
                existing.getCreatedAt()
        );

        if (endpoint.getActive() == null) {
            endpoint.setActive(
                    existing.getActive()
            );
        }

        if (endpoint.getUpdatedBy() == null) {
            endpoint.setUpdatedBy("SYSTEM");
        }

        return endpointRepository.update(endpoint);
    }

    public void delete(Long id) {

        Endpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Endpoint não encontrado"
                        )
                );

        endpointRepository.deleteById(endpoint.getId());
    }

    private void validateIntegrationExists(Long integrationId) {

        if (integrationId == null
                || integrationRepository.findById(integrationId).isEmpty()) {

            throw new IllegalArgumentException(
                    "Integração não encontrada: " + integrationId
            );
        }
    }
}
