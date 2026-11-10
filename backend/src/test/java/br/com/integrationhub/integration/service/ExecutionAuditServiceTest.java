
package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.repository.ExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExecutionAuditServiceTest {

    private ExecutionRepository executionRepository;
    private ExecutionAuditService executionAuditService;

    @BeforeEach
    void setUp() {
        executionRepository = mock(ExecutionRepository.class);
        executionAuditService =
                new ExecutionAuditService(executionRepository);
    }

    @Test
    void shouldRegisterExecutionSuccessfully() {
        Execution execution = new Execution();
        execution.setIntegrationId(1L);
        execution.setEndpointId(10L);
        execution.setExecutionStatus("SUCCESS");
        execution.setHttpStatus(200);

        executionAuditService.register(execution);

        verify(executionRepository, times(1))
                .save(execution);
    }

    @Test
    void shouldIgnoreNullExecution() {
        assertDoesNotThrow(
                () -> executionAuditService.register(null)
        );

        verifyNoInteractions(executionRepository);
    }

    @Test
    void shouldNotPropagateRepositoryException() {
        Execution execution = new Execution();
        execution.setIntegrationId(1L);
        execution.setEndpointId(10L);
        execution.setExecutionStatus("ERROR");
        execution.setHttpStatus(500);

        when(executionRepository.save(any(Execution.class)))
                .thenThrow(new RuntimeException(
                        "Falha simulada no banco de dados"
                ));

        assertDoesNotThrow(
                () -> executionAuditService.register(execution)
        );

        verify(executionRepository, times(1))
                .save(execution);
    }

    @Test
    void shouldRegisterUnauthorizedExecution() {
        Execution execution = new Execution();
        execution.setIntegrationId(1L);
        execution.setExecutionStatus("UNAUTHORIZED");
        execution.setHttpStatus(401);

        executionAuditService.register(execution);

        verify(executionRepository)
                .save(execution);
    }

    @Test
    void shouldRegisterExecutionWithoutEndpoint() {
        Execution execution = new Execution();
        execution.setIntegrationId(1L);
        execution.setEndpointId(null);
        execution.setExecutionStatus("NOT_FOUND");
        execution.setHttpStatus(404);

        executionAuditService.register(execution);

        verify(executionRepository)
                .save(execution);
    }
}
