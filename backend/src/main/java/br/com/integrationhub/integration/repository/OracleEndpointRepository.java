package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OracleEndpointRepository implements EndpointRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public OracleEndpointRepository(
            NamedParameterJdbcTemplate jdbcTemplate,
            JsonMapper jsonMapper) {

        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public List<Endpoint> findAll() {

        String sql = """
                select
                    id,
                    integration_id,
                    name,
                    description,
                    path,
                    method,
                    sql_text,
                    parameters,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_endpoint
                order by id
                """;

        return jdbcTemplate.query(
                sql,
                Map.of(),
                (rs, rowNum) -> mapRow(rs)
        );
    }

    @Override
    public Optional<Endpoint> findById(Long id) {

        String sql = """
                select
                    id,
                    integration_id,
                    name,
                    description,
                    path,
                    method,
                    sql_text,
                    parameters,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_endpoint
                where id = :id
                """;

        List<Endpoint> results = jdbcTemplate.query(
                sql,
                Map.of("id", id),
                (rs, rowNum) -> mapRow(rs)
        );

        return results.stream().findFirst();
    }

    @Override
    public List<Endpoint> findByIntegrationId(Long integrationId) {

        String sql = """
                select
                    id,
                    integration_id,
                    name,
                    description,
                    path,
                    method,
                    sql_text,
                    parameters,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_endpoint
                where integration_id = :integrationId
                order by id
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("integrationId", integrationId),
                (rs, rowNum) -> mapRow(rs)
        );
    }

    @Override
    public Optional<Endpoint> findByIntegrationIdAndPathAndMethod(
            Long integrationId,
            String path,
            String method) {

        String sql = """
                select
                    id,
                    integration_id,
                    name,
                    description,
                    path,
                    method,
                    sql_text,
                    parameters,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_endpoint
                where integration_id = :integrationId
                  and path = :path
                  and upper(method) = upper(:method)
                  and active = 'S'
                """;

        Map<String, Object> params = Map.of(
                "integrationId", integrationId,
                "path", path,
                "method", method
        );

        List<Endpoint> results = jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> mapRow(rs)
        );

        return results.stream().findFirst();
    }

    @Override
    public Endpoint save(Endpoint endpoint) {

        Long id = jdbcTemplate.queryForObject(
                "select ih_endpoint_seq.nextval from dual",
                Map.of(),
                Long.class
        );

        String sql = """
                insert into ih_endpoint (
                    id,
                    integration_id,
                    name,
                    description,
                    path,
                    method,
                    sql_text,
                    parameters,
                    active,
                    created_by
                ) values (
                    :id,
                    :integrationId,
                    :name,
                    :description,
                    :path,
                    :method,
                    :sqlText,
                    :parameters,
                    :active,
                    :createdBy
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("integrationId", endpoint.getIntegrationId())
                .addValue("name", endpoint.getName())
                .addValue("description", endpoint.getDescription())
                .addValue("path", endpoint.getPath())
                .addValue("method", endpoint.getMethod())
                .addValue("sqlText", endpoint.getSqlText())
                .addValue(
                        "parameters",
                        serializeParameters(endpoint.getParameters())
                )
                .addValue(
                        "active",
                        endpoint.getActive() == null
                                ? "S"
                                : endpoint.getActive()
                )
                .addValue(
                        "createdBy",
                        endpoint.getCreatedBy() == null
                                ? "SYSTEM"
                                : endpoint.getCreatedBy()
                );

        jdbcTemplate.update(sql, params);

        endpoint.setId(id);

        return findById(id)
                .orElse(endpoint);
    }

    @Override
    public void deleteById(Long id) {

        String sql = """
                delete from ih_endpoint
                where id = :id
                """;

        jdbcTemplate.update(
                sql,
                Map.of("id", id)
        );
    }

    private Endpoint mapRow(ResultSet rs) throws SQLException {

        Endpoint endpoint = new Endpoint();

        endpoint.setId(rs.getLong("id"));
        endpoint.setIntegrationId(rs.getLong("integration_id"));
        endpoint.setName(rs.getString("name"));
        endpoint.setDescription(rs.getString("description"));
        endpoint.setPath(rs.getString("path"));
        endpoint.setMethod(rs.getString("method"));
        endpoint.setSqlText(rs.getString("sql_text"));
        endpoint.setParameters(
                deserializeParameters(rs.getString("parameters"))
        );
        endpoint.setActive(rs.getString("active"));
        endpoint.setCreatedBy(rs.getString("created_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");

        if (createdAt != null) {
            endpoint.setCreatedAt(createdAt.toLocalDateTime());
        }

        endpoint.setUpdatedBy(rs.getString("updated_by"));

        Timestamp updatedAt = rs.getTimestamp("updated_at");

        if (updatedAt != null) {
            endpoint.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return endpoint;
    }

    private String serializeParameters(List<EndpointParameter> parameters) {

        if (parameters == null || parameters.isEmpty()) {
            return null;
        }

        try {
            return jsonMapper.writeValueAsString(parameters);
        } catch (JacksonException e) {
            throw new IllegalArgumentException(
                    "Erro ao serializar parâmetros do endpoint",
                    e
            );
        }
    }

    private List<EndpointParameter> deserializeParameters(
            String parametersJson) {

        if (parametersJson == null || parametersJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return jsonMapper.readValue(
                    parametersJson,
                    new TypeReference<List<EndpointParameter>>() {
                    }
            );
        } catch (JacksonException e) {
            throw new IllegalArgumentException(
                    "Erro ao desserializar parâmetros do endpoint",
                    e
            );
        }
    }
}