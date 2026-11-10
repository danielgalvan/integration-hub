
package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Execution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OracleExecutionRepositoryIntegrationTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private OracleExecutionRepository repository;

    @BeforeEach
    void setUp() {

        String url = requiredEnvironmentVariable("IH_TEST_DB_URL");
        String username = requiredEnvironmentVariable("IH_TEST_DB_USERNAME");
        String password = requiredEnvironmentVariable("IH_TEST_DB_PASSWORD");

        DataSource dataSource = createDataSource(
                url,
                username,
                password
        );

        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        repository = new OracleExecutionRepository(jdbcTemplate);
    }

    @Test
    void devePersistirEConsultarExecucaoNoOracleReal() {

        String identifier = UUID.randomUUID().toString();

        Execution execution = createExecution(identifier);

        List<Long> savedIds = new ArrayList<>();

        try {

            Execution saved = saveExecution(execution, savedIds);

            Long savedId = saved.getId();

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

            deleteExecutions(savedIds);
        }
    }

    @Test
    void deveUsarHorarioOracleQuandoStartedAtNaoInformado() {

        Execution execution = createExecution(
                UUID.randomUUID().toString()
        );

        execution.setStartedAt(null);

        List<Long> savedIds = new ArrayList<>();

        try {

            Execution saved = saveExecution(execution, savedIds);

            assertNotNull(saved.getId());

            Execution persisted = repository.findById(saved.getId())
                    .orElseThrow();

            assertNotNull(
                    persisted.getStartedAt(),
                    "O Oracle deve preencher started_at"
            );

            assertNotNull(persisted.getCreatedAt());

        } finally {

            deleteExecutions(savedIds);
        }
    }

    @Test
    void deveListarExecucoesComFiltrosNoOracleReal() {

        Long integrationId = createTestIntegrationId();

        LocalDateTime startedAt =
                LocalDateTime.of(2026, 11, 5, 10, 0, 0);

        List<Long> savedIds = new ArrayList<>();

        try {

            Execution success = createExecution(
                    UUID.randomUUID().toString()
            );

            success.setIntegrationId(integrationId);
            success.setEndpointId(10L);
            success.setStartedAt(startedAt);
            success.setExecutionStatus("SUCCESS");

            Execution error = createExecution(
                    UUID.randomUUID().toString()
            );

            error.setIntegrationId(integrationId);
            error.setEndpointId(20L);
            error.setStartedAt(startedAt.plusHours(1));
            error.setExecutionStatus("ERROR");
            error.setHttpStatus(500);

            Execution unauthorized = createExecution(
                    UUID.randomUUID().toString()
            );

            unauthorized.setIntegrationId(integrationId);
            unauthorized.setEndpointId(null);
            unauthorized.setStartedAt(startedAt.plusHours(2));
            unauthorized.setExecutionStatus("UNAUTHORIZED");
            unauthorized.setHttpStatus(401);

            saveExecution(success, savedIds);
            saveExecution(error, savedIds);
            saveExecution(unauthorized, savedIds);

            List<Execution> all = repository.findAll(
                    integrationId,
                    null,
                    null,
                    null,
                    null,
                    0,
                    10
            );

            assertEquals(3, all.size());

            List<Execution> successResults = repository.findAll(
                    integrationId,
                    null,
                    "SUCCESS",
                    null,
                    null,
                    0,
                    10
            );

            assertEquals(1, successResults.size());
            assertEquals(
                    success.getId(),
                    successResults.get(0).getId()
            );

            List<Execution> endpointResults = repository.findAll(
                    integrationId,
                    20L,
                    null,
                    null,
                    null,
                    0,
                    10
            );

            assertEquals(1, endpointResults.size());
            assertEquals(
                    error.getId(),
                    endpointResults.get(0).getId()
            );

            List<Execution> combinedResults = repository.findAll(
                    integrationId,
                    20L,
                    "ERROR",
                    startedAt.plusMinutes(30),
                    startedAt.plusHours(1),
                    0,
                    10
            );

            assertEquals(1, combinedResults.size());
            assertEquals(
                    error.getId(),
                    combinedResults.get(0).getId()
            );

        } finally {

            deleteExecutions(savedIds);
        }
    }

    @Test
    void devePaginarEOrdenarExecucoesNoOracleReal() {

        Long integrationId = createTestIntegrationId();

        LocalDateTime startedAt =
                LocalDateTime.of(2026, 11, 5, 10, 0, 0);

        List<Long> savedIds = new ArrayList<>();

        try {

            Execution first = createExecution(
                    UUID.randomUUID().toString()
            );

            first.setIntegrationId(integrationId);
            first.setStartedAt(startedAt);

            Execution second = createExecution(
                    UUID.randomUUID().toString()
            );

            second.setIntegrationId(integrationId);
            second.setStartedAt(startedAt.plusMinutes(1));

            Execution third = createExecution(
                    UUID.randomUUID().toString()
            );

            third.setIntegrationId(integrationId);
            third.setStartedAt(startedAt.plusMinutes(2));

            saveExecution(first, savedIds);
            saveExecution(second, savedIds);
            saveExecution(third, savedIds);

            List<Execution> page0 = repository.findAll(
                    integrationId,
                    null,
                    null,
                    null,
                    null,
                    0,
                    2
            );

            List<Execution> page1 = repository.findAll(
                    integrationId,
                    null,
                    null,
                    null,
                    null,
                    1,
                    2
            );

            assertEquals(2, page0.size());
            assertEquals(1, page1.size());

            assertEquals(
                    third.getId(),
                    page0.get(0).getId()
            );

            assertEquals(
                    second.getId(),
                    page0.get(1).getId()
            );

            assertEquals(
                    first.getId(),
                    page1.get(0).getId()
            );

        } finally {

            deleteExecutions(savedIds);
        }
    }

    @Test
    void deveContarExecucoesComFiltrosNoOracleReal() {

        Long integrationId = createTestIntegrationId();

        List<Long> savedIds = new ArrayList<>();

        try {

            Execution first = createExecution(
                    UUID.randomUUID().toString()
            );

            first.setIntegrationId(integrationId);
            first.setExecutionStatus("SUCCESS");

            Execution second = createExecution(
                    UUID.randomUUID().toString()
            );

            second.setIntegrationId(integrationId);
            second.setExecutionStatus("SUCCESS");

            Execution third = createExecution(
                    UUID.randomUUID().toString()
            );

            third.setIntegrationId(integrationId);
            third.setExecutionStatus("ERROR");
            third.setHttpStatus(500);

            saveExecution(first, savedIds);
            saveExecution(second, savedIds);
            saveExecution(third, savedIds);

            long total = repository.count(
                    integrationId,
                    null,
                    null,
                    null,
                    null
            );

            long successTotal = repository.count(
                    integrationId,
                    null,
                    "SUCCESS",
                    null,
                    null
            );

            long errorTotal = repository.count(
                    integrationId,
                    null,
                    "ERROR",
                    null,
                    null
            );

            assertEquals(3L, total);
            assertEquals(2L, successTotal);
            assertEquals(1L, errorTotal);

        } finally {

            deleteExecutions(savedIds);
        }
    }

    @Test
    void deveRetornarVazioQuandoFiltrosNaoEncontraremRegistros() {

        Long integrationId = createTestIntegrationId();

        List<Execution> executions = repository.findAll(
                integrationId,
                null,
                null,
                null,
                null,
                0,
                10
        );

        long total = repository.count(
                integrationId,
                null,
                null,
                null,
                null
        );

        assertTrue(executions.isEmpty());
        assertEquals(0L, total);
    }

    @Test
    void deveFiltrarExecucoesPorPeriodoNoOracleReal() {

        Long integrationId = createTestIntegrationId();

        LocalDateTime startedAt =
                LocalDateTime.of(2026, 11, 5, 10, 0, 0);

        List<Long> savedIds = new ArrayList<>();

        try {

            Execution before = createExecution(
                    UUID.randomUUID().toString()
            );

            before.setIntegrationId(integrationId);
            before.setStartedAt(startedAt.minusDays(1));

            Execution inside = createExecution(
                    UUID.randomUUID().toString()
            );

            inside.setIntegrationId(integrationId);
            inside.setStartedAt(startedAt);

            Execution after = createExecution(
                    UUID.randomUUID().toString()
            );

            after.setIntegrationId(integrationId);
            after.setStartedAt(startedAt.plusDays(1));

            saveExecution(before, savedIds);
            saveExecution(inside, savedIds);
            saveExecution(after, savedIds);

            List<Execution> executions = repository.findAll(
                    integrationId,
                    null,
                    null,
                    startedAt,
                    startedAt,
                    0,
                    10
            );

            long total = repository.count(
                    integrationId,
                    null,
                    null,
                    startedAt,
                    startedAt
            );

            assertEquals(1, executions.size());
            assertEquals(1L, total);

            assertEquals(
                    inside.getId(),
                    executions.get(0).getId()
            );

        } finally {

            deleteExecutions(savedIds);
        }
    }

    private Execution saveExecution(
            Execution execution,
            List<Long> savedIds) {

        Execution saved = repository.save(execution);

        if (saved.getId() != null) {
            savedIds.add(saved.getId());
        }

        return saved;
    }

    private void deleteExecutions(List<Long> savedIds) {

        for (Long id : savedIds) {

            jdbcTemplate.update(
                    """
                    delete from ih_execution
                     where id = :id
                    """,
                    Map.of("id", id)
            );
        }
    }

    private Long createTestIntegrationId() {

        return UUID.randomUUID()
                .getMostSignificantBits() & Long.MAX_VALUE;
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
