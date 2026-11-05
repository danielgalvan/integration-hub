
package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Execution;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OracleExecutionRepository implements ExecutionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OracleExecutionRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Execution save(Execution execution) {

        Long id = jdbcTemplate.queryForObject(
                """
                select ih_execution_seq.nextval
                  from dual
                """,
                Map.of(),
                Long.class
        );

        if (id == null) {
            throw new IllegalStateException(
                    "Não foi possível gerar o ID da execução"
            );
        }

        String sql = """
                insert into ih_execution (
                    id,
                    integration_id,
                    integration_name,
                    endpoint_id,
                    endpoint_name,
                    credential_id,
                    credential_name,
                    request_method,
                    request_path,
                    request_parameters,
                    started_at,
                    finished_at,
                    duration_ms,
                    http_status,
                    execution_status,
                    error_type,
                    error_message,
                    created_at
                ) values (
                    :id,
                    :integrationId,
                    :integrationName,
                    :endpointId,
                    :endpointName,
                    :credentialId,
                    :credentialName,
                    :requestMethod,
                    :requestPath,
                    :requestParameters,
                    :startedAt,
                    :finishedAt,
                    :durationMs,
                    :httpStatus,
                    :executionStatus,
                    :errorType,
                    :errorMessage,
                    current_timestamp
                )
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue(
                                "integrationId",
                                execution.getIntegrationId()
                        )
                        .addValue(
                                "integrationName",
                                execution.getIntegrationName()
                        )
                        .addValue(
                                "endpointId",
                                execution.getEndpointId()
                        )
                        .addValue(
                                "endpointName",
                                execution.getEndpointName()
                        )
                        .addValue(
                                "credentialId",
                                execution.getCredentialId()
                        )
                        .addValue(
                                "credentialName",
                                execution.getCredentialName()
                        )
                        .addValue(
                                "requestMethod",
                                execution.getRequestMethod()
                        )
                        .addValue(
                                "requestPath",
                                execution.getRequestPath()
                        )
                        .addValue(
                                "requestParameters",
                                execution.getRequestParameters(),
                                Types.CLOB
                        )
                        .addValue(
                                "startedAt",
                                execution.getStartedAt() == null
                                        ? null
                                        : Timestamp.valueOf(
                                                execution.getStartedAt()
                                        )
                        )
                        .addValue(
                                "finishedAt",
                                execution.getFinishedAt() == null
                                        ? null
                                        : Timestamp.valueOf(
                                                execution.getFinishedAt()
                                        )
                        )
                        .addValue(
                                "durationMs",
                                execution.getDurationMs()
                        )
                        .addValue(
                                "httpStatus",
                                execution.getHttpStatus()
                        )
                        .addValue(
                                "executionStatus",
                                execution.getExecutionStatus()
                        )
                        .addValue(
                                "errorType",
                                execution.getErrorType()
                        )
                        .addValue(
                                "errorMessage",
                                execution.getErrorMessage()
                        );

        if (execution.getStartedAt() == null) {

            params.addValue(
                    "startedAt",
                    null,
                    Types.TIMESTAMP
            );

            sql = sql.replace(
                    ":startedAt,",
                    "current_timestamp,"
            );
        }

        int rowsAffected = jdbcTemplate.update(
                sql,
                params
        );

        if (rowsAffected != 1) {
            throw new IllegalStateException(
                    "Falha ao registrar execução. Registros afetados: "
                            + rowsAffected
            );
        }

        execution.setId(id);

        return execution;
    }

    @Override
    public Optional<Execution> findById(Long id) {

        String sql = """
                select id,
                       integration_id,
                       integration_name,
                       endpoint_id,
                       endpoint_name,
                       credential_id,
                       credential_name,
                       request_method,
                       request_path,
                       request_parameters,
                       started_at,
                       finished_at,
                       duration_ms,
                       http_status,
                       execution_status,
                       error_type,
                       error_message,
                       created_at
                  from ih_execution
                 where id = :id
                """;

        List<Execution> executions = jdbcTemplate.query(
                sql,
                Map.of("id", id),
                this::mapRow
        );

        return executions.stream()
                .findFirst();
    }

    private Execution mapRow(
            ResultSet rs,
            int rowNum) throws SQLException {

        Execution execution = new Execution();

        execution.setId(
                rs.getLong("id")
        );

        execution.setIntegrationId(
                getNullableLong(rs, "integration_id")
        );

        execution.setIntegrationName(
                rs.getString("integration_name")
        );

        execution.setEndpointId(
                getNullableLong(rs, "endpoint_id")
        );

        execution.setEndpointName(
                rs.getString("endpoint_name")
        );

        execution.setCredentialId(
                getNullableLong(rs, "credential_id")
        );

        execution.setCredentialName(
                rs.getString("credential_name")
        );

        execution.setRequestMethod(
                rs.getString("request_method")
        );

        execution.setRequestPath(
                rs.getString("request_path")
        );

        execution.setRequestParameters(
                rs.getString("request_parameters")
        );

        Timestamp startedAt =
                rs.getTimestamp("started_at");

        if (startedAt != null) {
            execution.setStartedAt(
                    startedAt.toLocalDateTime()
            );
        }

        Timestamp finishedAt =
                rs.getTimestamp("finished_at");

        if (finishedAt != null) {
            execution.setFinishedAt(
                    finishedAt.toLocalDateTime()
            );
        }

        execution.setDurationMs(
                getNullableLong(rs, "duration_ms")
        );

        execution.setHttpStatus(
                getNullableInteger(rs, "http_status")
        );

        execution.setExecutionStatus(
                rs.getString("execution_status")
        );

        execution.setErrorType(
                rs.getString("error_type")
        );

        execution.setErrorMessage(
                rs.getString("error_message")
        );

        Timestamp createdAt =
                rs.getTimestamp("created_at");

        if (createdAt != null) {
            execution.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        return execution;
    }

    private Long getNullableLong(
            ResultSet rs,
            String columnName) throws SQLException {

        long value = rs.getLong(columnName);

        return rs.wasNull()
                ? null
                : value;
    }

    private Integer getNullableInteger(
            ResultSet rs,
            String columnName) throws SQLException {

        int value = rs.getInt(columnName);

        return rs.wasNull()
                ? null
                : value;
    }
}
