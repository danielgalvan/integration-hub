
package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.repository.ExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExecutionAuditService {

    private static final Logger logger =
            LoggerFactory.getLogger(ExecutionAuditService.class);

    private final ExecutionRepository executionRepository;

    public ExecutionAuditService(
            ExecutionRepository executionRepository) {

        this.executionRepository = executionRepository;
    }

    public void register(Execution execution) {

        if (execution == null) {
            logger.warn(
                    "Registro de execução ignorado: execução nula"
            );
            return;
        }

        try {

            executionRepository.save(execution);

        } catch (Exception exception) {

            logger.error(
                    "Falha ao registrar auditoria da execução. "
                            + "Integração: {}, Endpoint: {}, Status: {}",
                    execution.getIntegrationId(),
                    execution.getEndpointId(),
                    execution.getExecutionStatus(),
                    exception
            );
        }
    }
}
