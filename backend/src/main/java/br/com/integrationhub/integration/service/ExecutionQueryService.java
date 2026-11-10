
package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.repository.ExecutionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExecutionQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ExecutionRepository executionRepository;

    public ExecutionQueryService(
            ExecutionRepository executionRepository) {

        this.executionRepository = executionRepository;
    }

    public List<Execution> findAll(
            Long integrationId,
            Long endpointId,
            String executionStatus,
            LocalDateTime startedFrom,
            LocalDateTime startedTo,
            Integer page,
            Integer size) {

        validateFilters(
                integrationId,
                endpointId,
                executionStatus,
                startedFrom,
                startedTo
        );

        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size);

        return executionRepository.findAll(
                integrationId,
                endpointId,
                normalizeStatus(executionStatus),
                startedFrom,
                startedTo,
                resolvedPage,
                resolvedSize
        );
    }

    public long count(
            Long integrationId,
            Long endpointId,
            String executionStatus,
            LocalDateTime startedFrom,
            LocalDateTime startedTo) {

        validateFilters(
                integrationId,
                endpointId,
                executionStatus,
                startedFrom,
                startedTo
        );

        return executionRepository.count(
                integrationId,
                endpointId,
                normalizeStatus(executionStatus),
                startedFrom,
                startedTo
        );
    }

    public Optional<Execution> findById(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID da execução inválido"
            );
        }

        return executionRepository.findById(id);
    }

    private void validateFilters(
            Long integrationId,
            Long endpointId,
            String executionStatus,
            LocalDateTime startedFrom,
            LocalDateTime startedTo) {

        if (integrationId != null && integrationId <= 0) {
            throw new IllegalArgumentException(
                    "ID da integração inválido"
            );
        }

        if (endpointId != null && endpointId <= 0) {
            throw new IllegalArgumentException(
                    "ID do endpoint inválido"
            );
        }

        if (startedFrom != null
                && startedTo != null
                && startedFrom.isAfter(startedTo)) {

            throw new IllegalArgumentException(
                    "Data inicial não pode ser posterior à data final"
            );
        }

        String status = normalizeStatus(executionStatus);

        if (status != null
                && !status.equals("SUCCESS")
                && !status.equals("ERROR")
                && !status.equals("UNAUTHORIZED")
                && !status.equals("NOT_FOUND")) {

            throw new IllegalArgumentException(
                    "Status da execução inválido"
            );
        }
    }

    private int resolvePage(Integer page) {

        if (page == null) {
            return 0;
        }

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Número da página inválido"
            );
        }

        return page;
    }

    private int resolveSize(Integer size) {

        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Tamanho da página deve estar entre 1 e "
                            + MAX_PAGE_SIZE
            );
        }

        return size;
    }

    private String normalizeStatus(String executionStatus) {

        if (executionStatus == null
                || executionStatus.isBlank()) {

            return null;
        }

        return executionStatus.trim().toUpperCase(
                java.util.Locale.ROOT
        );
    }
}
