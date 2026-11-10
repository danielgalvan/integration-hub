
package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Execution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExecutionRepository {

    Execution save(Execution execution);

    Optional<Execution> findById(Long id);

    List<Execution> findAll(
            Long integrationId,
            Long endpointId,
            String executionStatus,
            LocalDateTime startedFrom,
            LocalDateTime startedTo,
            int page,
            int size
    );

    long count(
            Long integrationId,
            Long endpointId,
            String executionStatus,
            LocalDateTime startedFrom,
            LocalDateTime startedTo
    );
}
