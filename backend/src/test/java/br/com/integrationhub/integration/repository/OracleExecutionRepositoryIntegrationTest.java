
package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Execution;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OracleExecutionRepositoryIntegrationTest {

    @Test
    void devePersistirEConsultarExecucaoNoOracleReal() {

        String url = requiredEnvironmentVariable("IH_TEST_DB_URL");
        String username = requiredEnvironmentVariable("IH_TEST_DB_USERNAME");
        String password = requiredEnvironmentVariable("IH_TEST_DB_PASSWORD");

        DataSource dataSource = createDataSource(
                url,
                username,
                password
        );

        NamedParameterJdbcTemplate jdbcTemplate =
                new NamedParameterJdbcTemplate(dataSource);

        OracleExecutionRepository repository =
                new OracleExecutionRepository(jdbcTemplate);

        String identifier = UUID.randomUUID().toString();

        Execution execution = createExecution(identifier);

        Long savedId = null;

        try {

            Execution saved = repository.save(execution);

            savedId = saved.getId();

            assertNotNull(savedId);
            assertSame(execution, saved);

            assertNull(
                    saved.getCreatedAt(),
                    "O save não deve executar SELECT para carregar createdAt"
            );

            Optional<Execution> result =
                    repository.findById(savedId);

            assertTrue(
                    result.isPresent(),
                    "A execução inserida deve ser encontrada"
            );

            Execution persisted = result.orElseThrow();

            assertEquals(savedId, persisted.getId());

            assertEquals(
                    execution.getIntegrationId(),
                    persisted.getIntegrationId()
            );

            assertEquals(
                    execution.getIntegrationName(),
                    persisted.getIntegrationName()
            );

            assertEquals(
                    execution.getEndpointId(),
                    persisted.getEndpointId()
            );

            assertEquals(
                    execution.getEndpointName(),
                    persisted.getEndpointName()
            );

            assertEquals(
                    execution.getCredentialId(),
                    persisted.getCredentialId()
            );

            assertEquals(
                    execution.getCredentialName(),
                    persisted.getCredentialName()
            );

            assertEquals("GET", persisted.getRequestMethod());

            assertEquals(
                    execution.getRequestPath(),
                    persisted.getRequestPath()
            );

            assertEquals(
                    execution.getRequestParameters(),
                    persisted.getRequestParameters()
            );

            assertEquals(
                    execution.getStartedAt(),
                    persisted.getStartedAt()
            );

            assertEquals(
                    execution.getFinishedAt(),
                    persisted.getFinishedAt()
            );

            assertEquals(45L, persisted.getDurationMs());
            assertEquals(200, persisted.getHttpStatus());
            assertEquals("SUCCESS", persisted.getExecutionStatus());

            assertNull(persisted.getErrorType());
            assertNull(persisted.getErrorMessage());

            assertNotNull(
                    persisted.getCreatedAt(),
                    "O Oracle deve preencher created_at"
            );

        } finally {

            if (savedId != null) {

                jdbcTemplate.update(
                        """
                        delete from ih_execution
                         where id = :id
                        """,
                        Map.of("id", savedId)
                );
            }
        }
    }

    @Test
    void deveUsarHorarioOracleQuandoStartedAtNaoInformado() {

        String url = requiredEnvironmentVariable("IH_TEST_DB_URL");
        String username = requiredEnvironmentVariable("IH_TEST_DB_USERNAME");
        String password = requiredEnvironmentVariable("IH_TEST_DB_PASSWORD");

        NamedParameterJdbcTemplate jdbcTemplate =
                new NamedParameterJdbcTemplate(
                        createDataSource(url, username, password)
                );

        OracleExecutionRepository repository =
                new OracleExecutionRepository(jdbcTemplate);

        Execution execution = createExecution(
                UUID.randomUUID().toString()
        );

        execution.setStartedAt(null);

        Long savedId = null;

        try {

            Execution saved = repository.save(execution);

            savedId = saved.getId();

            assertNotNull(savedId);

            Execution persisted = repository.findById(savedId)
                    .orElseThrow();

            assertNotNull(
                    persisted.getStartedAt(),
                    "O Oracle deve preencher started_at"
            );

            assertNotNull(persisted.getCreatedAt());

        } finally {

            if (savedId != null) {

                jdbcTemplate.update(
                        """
                        delete from ih_execution
                         where id = :id
                        """,
                        Map.of("id", savedId)
                );
            }
        }
    }

    private DataSource createDataSource(
            String url,
            String username,
            String password) {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setDriverClassName(
                "oracle.jdbc.OracleDriver"
        );

        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    private Execution createExecution(String identifier) {

        Execution execution = new Execution();

        execution.setIntegrationId(2L);
        execution.setIntegrationName("Integration Test");

        execution.setEndpointId(5L);
        execution.setEndpointName("Test Endpoint");

        execution.setCredentialId(null);
        execution.setCredentialName(null);

        execution.setRequestMethod("GET");

        execution.setRequestPath(
                "/api/integration-test/" + identifier
        );

        execution.setRequestParameters(
                "{\"test\":\"" + identifier + "\"}"
        );

        LocalDateTime startedAt =
                LocalDateTime.of(2026, 11, 5, 10, 0, 0);

        execution.setStartedAt(startedAt);

        execution.setFinishedAt(
                startedAt.plusNanos(45_000_000)
        );

        execution.setDurationMs(45L);
        execution.setHttpStatus(200);
        execution.setExecutionStatus("SUCCESS");

        return execution;
    }

    private String requiredEnvironmentVariable(String name) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Variável de ambiente obrigatória não definida: "
                            + name
            );
        }

        return value;
    }
}
