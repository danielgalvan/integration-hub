
package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Execution;

import java.util.Optional;

public interface ExecutionRepository {

    Execution save(Execution execution);

    Optional<Execution> findById(Long id);

}
