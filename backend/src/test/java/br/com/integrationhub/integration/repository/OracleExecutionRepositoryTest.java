
package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Execution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OracleExecutionRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private OracleExecutionRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        repository = new OracleExecutionRepository(jdbcTemplate);
    }

    @Test
    void deveSalvarExecucaoComSucesso() {

        Execution execution = createExecution();

        stubSequence(10L);
        stubInsert(1);

        Execution result = repository.save(execution);

        assertSame(execution, result);
        assertEquals(10L, result.getId());

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture()
        );

        String sql = normalizeSql(sqlCaptor.getValue());
        MapSqlParameterSource params = paramsCaptor.getValue();

        assertTrue(sql.contains("insert into ih_execution"));

        assertEquals(10L, params.getValue("id"));
        assertEquals(2L, params.getValue("integrationId"));
        assertEquals("Pedidos", params.getValue("integrationName"));

        assertEquals(5L, params.getValue("endpointId"));
        assertEquals("Buscar pedido", params.getValue("endpointName"));

        assertEquals(3L, params.getValue("credentialId"));
        assertEquals("Sistema externo", params.getValue("credentialName"));

        assertEquals("GET", params.getValue("requestMethod"));
        assertEquals(
                "/api/pedidos/buscar",
                params.getValue("requestPath")
        );

        assertEquals(
                "{\"id\":\"123\"}",
                params.getValue("requestParameters")
        );

        assertEquals(
                Types.CLOB,
                params.getSqlType("requestParameters")
        );

        assertEquals(
                Timestamp.valueOf(execution.getStartedAt()),
                params.getValue("startedAt")
        );

        assertEquals(
                Timestamp.valueOf(execution.getFinishedAt()),
                params.getValue("finishedAt")
        );

        assertEquals(45L, params.getValue("durationMs"));
        assertEquals(200, params.getValue("httpStatus"));
        assertEquals("SUCCESS", params.getValue("executionStatus"));

        assertNull(params.getValue("errorType"));
        assertNull(params.getValue("errorMessage"));

        verify(jdbcTemplate, never()).query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Execution>>any()
        );
    }

    @Test
    void deveGerarIdUtilizandoSequenceOracle() {

        stubSequence(25L);
        stubInsert(1);

        Execution execution = createExecution();

        Execution result = repository.save(execution);

        assertEquals(25L, result.getId());

        verify(jdbcTemplate).queryForObject(
                contains("ih_execution_seq.nextval"),
                ArgumentMatchers.<Map<String, ?>>any(),
                eq(Long.class)
        );

        verify(jdbcTemplate).update(
                anyString(),
                argThat((MapSqlParameterSource params) ->
                        Long.valueOf(25L).equals(params.getValue("id"))
                )
        );
    }

    @Test
    void naoDeveConsultarRegistroAposInsert() {

        stubSequence(15L);
        stubInsert(1);

        Execution result = repository.save(createExecution());

        assertEquals(15L, result.getId());

        verify(jdbcTemplate, times(1)).queryForObject(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                eq(Long.class)
        );

        verify(jdbcTemplate, times(1)).update(
                anyString(),
                any(MapSqlParameterSource.class)
        );

        verify(jdbcTemplate, never()).query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Execution>>any()
        );
    }

    @Test
    void deveBuscarExecucaoPorId() throws Exception {

        ResultSet rs = mock(ResultSet.class);

        when(rs.getLong("id")).thenReturn(10L);

        when(rs.getLong("integration_id")).thenReturn(2L);
        when(rs.getString("integration_name"))
                .thenReturn("Pedidos");

        when(rs.getLong("endpoint_id")).thenReturn(5L);
        when(rs.getString("endpoint_name"))
                .thenReturn("Buscar pedido");

        when(rs.getLong("credential_id")).thenReturn(3L);
        when(rs.getString("credential_name"))
                .thenReturn("Sistema externo");

        when(rs.getString("request_method"))
                .thenReturn("GET");

        when(rs.getString("request_path"))
                .thenReturn("/api/pedidos/buscar");

        when(rs.getString("request_parameters"))
                .thenReturn("{\"id\":\"123\"}");

        LocalDateTime startedAt =
                LocalDateTime.of(2026, 11, 5, 10, 0, 0);

        LocalDateTime finishedAt =
                startedAt.plusNanos(45_000_000);

        when(rs.getTimestamp("started_at"))
                .thenReturn(Timestamp.valueOf(startedAt));

        when(rs.getTimestamp("finished_at"))
                .thenReturn(Timestamp.valueOf(finishedAt));

        when(rs.getLong("duration_ms"))
                .thenReturn(45L);

        when(rs.getInt("http_status"))
                .thenReturn(200);

        when(rs.getString("execution_status"))
                .thenReturn("SUCCESS");

        when(rs.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(startedAt));

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Execution>>any()
        )).thenAnswer(invocation -> {

            RowMapper<Execution> mapper =
                    invocation.getArgument(2);

            return List.of(mapper.mapRow(rs, 0));
        });

        Optional<Execution> result = repository.findById(10L);

        assertTrue(result.isPresent());

        Execution execution = result.orElseThrow();

        assertEquals(10L, execution.getId());

        assertEquals(2L, execution.getIntegrationId());
        assertEquals("Pedidos", execution.getIntegrationName());

        assertEquals(5L, execution.getEndpointId());
        assertEquals("Buscar pedido", execution.getEndpointName());

        assertEquals(3L, execution.getCredentialId());
        assertEquals("Sistema externo", execution.getCredentialName());

        assertEquals("GET", execution.getRequestMethod());

        assertEquals(
                "/api/pedidos/buscar",
                execution.getRequestPath()
        );

        assertEquals(
                "{\"id\":\"123\"}",
                execution.getRequestParameters()
        );

        assertEquals(startedAt, execution.getStartedAt());
        assertEquals(finishedAt, execution.getFinishedAt());

        assertEquals(45L, execution.getDurationMs());
        assertEquals(200, execution.getHttpStatus());
        assertEquals("SUCCESS", execution.getExecutionStatus());

        assertEquals(startedAt, execution.getCreatedAt());
    }

    @Test
    void devePermitirCamposOpcionaisNulos() throws Exception {

        ResultSet rs = mock(ResultSet.class);

        when(rs.getLong("id")).thenReturn(11L);

        when(rs.getLong("integration_id"))
                .thenReturn(2L);

        when(rs.getLong("endpoint_id"))
                .thenReturn(0L);

        when(rs.getLong("credential_id"))
                .thenReturn(0L);

        when(rs.getLong("duration_ms"))
                .thenReturn(0L);

        when(rs.getInt("http_status"))
                .thenReturn(0);

        when(rs.wasNull()).thenReturn(true);

        when(rs.getString("request_method"))
                .thenReturn("GET");

        when(rs.getString("request_path"))
                .thenReturn("/api/inexistente");

        when(rs.getString("execution_status"))
                .thenReturn("NOT_FOUND");

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Execution>>any()
        )).thenAnswer(invocation -> {

            RowMapper<Execution> mapper =
                    invocation.getArgument(2);

            return List.of(mapper.mapRow(rs, 0));
        });

        Optional<Execution> result = repository.findById(11L);

        assertTrue(result.isPresent());

        Execution execution = result.orElseThrow();

        assertNull(execution.getEndpointId());
        assertNull(execution.getCredentialId());
        assertNull(execution.getDurationMs());
        assertNull(execution.getHttpStatus());

        assertNull(execution.getFinishedAt());
        assertNull(execution.getErrorType());
        assertNull(execution.getErrorMessage());

        assertEquals(
                "NOT_FOUND",
                execution.getExecutionStatus()
        );
    }

    @Test
    void deveRetornarVazioQuandoExecucaoNaoExistir() {

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Execution>>any()
        )).thenReturn(List.of());

        Optional<Execution> result =
                repository.findById(999L);

        assertTrue(result.isEmpty());

        verify(jdbcTemplate).query(
                contains("where id = :id"),
                ArgumentMatchers.<Map<String, ?>>argThat(
                        params -> Long.valueOf(999L)
                                .equals(params.get("id"))
                ),
                ArgumentMatchers.<RowMapper<Execution>>any()
        );
    }

    @Test
    void deveRetornarErroQuandoSequenceNaoGerarId() {

        stubSequence(null);

        Execution execution = createExecution();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.save(execution)
        );

        assertEquals(
                "Não foi possível gerar o ID da execução",
                exception.getMessage()
        );

        assertNull(execution.getId());

        verify(jdbcTemplate, never()).update(
                anyString(),
                any(MapSqlParameterSource.class)
        );
    }

    @Test
    void deveUsarHorarioOracleQuandoInicioNaoInformado() {

        Execution execution = createExecution();

        execution.setStartedAt(null);

        stubSequence(30L);
        stubInsert(1);

        repository.save(execution);

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture()
        );

        String sql = normalizeSql(sqlCaptor.getValue());

        assertTrue(sql.contains("current_timestamp"));
        assertFalse(sql.contains(":startedat,"));

        assertEquals(
                Types.TIMESTAMP,
                paramsCaptor.getValue().getSqlType("startedAt")
        );

        assertEquals(30L, execution.getId());
    }

    @Test
    void deveRetornarErroQuandoInsertNaoAfetarRegistros() {

        stubSequence(40L);
        stubInsert(0);

        Execution execution = createExecution();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.save(execution)
        );

        assertEquals(
                "Falha ao registrar execução. Registros afetados: 0",
                exception.getMessage()
        );

        assertNull(execution.getId());

        verify(jdbcTemplate, never()).query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Execution>>any()
        );
    }

    @Test
    void deveRetornarErroQuandoInsertAfetarMultiplosRegistros() {

        stubSequence(50L);
        stubInsert(2);

        Execution execution = createExecution();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.save(execution)
        );

        assertEquals(
                "Falha ao registrar execução. Registros afetados: 2",
                exception.getMessage()
        );

        assertNull(execution.getId());
    }

    private void stubSequence(Long id) {

        when(jdbcTemplate.queryForObject(
                contains("ih_execution_seq.nextval"),
                ArgumentMatchers.<Map<String, ?>>any(),
                eq(Long.class)
        )).thenReturn(id);
    }

    private void stubInsert(int rowsAffected) {

        when(jdbcTemplate.update(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenReturn(rowsAffected);
    }

    private Execution createExecution() {

        Execution execution = new Execution();

        execution.setIntegrationId(2L);
        execution.setIntegrationName("Pedidos");

        execution.setEndpointId(5L);
        execution.setEndpointName("Buscar pedido");

        execution.setCredentialId(3L);
        execution.setCredentialName("Sistema externo");

        execution.setRequestMethod("GET");
        execution.setRequestPath("/api/pedidos/buscar");

        execution.setRequestParameters(
                "{\"id\":\"123\"}"
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

    private String normalizeSql(String sql) {

        return sql.replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}
