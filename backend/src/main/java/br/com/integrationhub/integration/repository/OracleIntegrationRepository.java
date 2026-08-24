package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Integration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OracleIntegrationRepository implements IntegrationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OracleIntegrationRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Integration> findAll() {

        String sql = """
                select
                    id,
                    name,
                    description,
                    base_path,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_integration
                order by id
                """;

        return jdbcTemplate.query(
                sql,
                Map.of(),
                (rs, rowNum) -> mapRow(rs)
        );
    }

    @Override
    public Optional<Integration> findById(Long id) {

        String sql = """
                select
                    id,
                    name,
                    description,
                    base_path,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_integration
                where id = :id
                """;

        List<Integration> results = jdbcTemplate.query(
                sql,
                Map.of("id", id),
                (rs, rowNum) -> mapRow(rs)
        );

        return results.stream().findFirst();
    }

    @Override
    public Optional<Integration> findByBasePath(String basePath) {

        String sql = """
                select
                    id,
                    name,
                    description,
                    base_path,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_integration
                where base_path = :basePath
                """;

        List<Integration> results = jdbcTemplate.query(
                sql,
                Map.of("basePath", basePath),
                (rs, rowNum) -> mapRow(rs)
        );

        return results.stream().findFirst();
    }

    @Override
    public Optional<Integration> findBestMatchByRequestPath(
            String requestPath) {

        String sql = """
                select
                    id,
                    name,
                    description,
                    base_path,
                    active,
                    created_by,
                    created_at,
                    updated_by,
                    updated_at
                from ih_integration
                where active = 'S'
                  and (
                        :requestPath = base_path
                        or substr(
                            :requestPath,
                            1,
                            length(base_path) + 1
                        ) = base_path || '/'
                  )
                order by length(base_path) desc
                fetch first 1 row only
                """;

        List<Integration> results = jdbcTemplate.query(
                sql,
                Map.of("requestPath", requestPath),
                (rs, rowNum) -> mapRow(rs)
        );

        return results.stream().findFirst();
    }

    @Override
    public Integration save(Integration integration) {

        Long id = jdbcTemplate.queryForObject(
                "select ih_integration_seq.nextval from dual",
                Map.of(),
                Long.class
        );

        String sql = """
                insert into ih_integration (
                    id,
                    name,
                    description,
                    base_path,
                    active,
                    created_by
                ) values (
                    :id,
                    :name,
                    :description,
                    :basePath,
                    :active,
                    :createdBy
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", integration.getName())
                .addValue(
                        "description",
                        integration.getDescription()
                )
                .addValue(
                        "basePath",
                        integration.getBasePath()
                )
                .addValue(
                        "active",
                        integration.getActive() == null
                                ? "S"
                                : integration.getActive()
                )
                .addValue(
                        "createdBy",
                        integration.getCreatedBy() == null
                                ? "SYSTEM"
                                : integration.getCreatedBy()
                );

        jdbcTemplate.update(sql, params);

        integration.setId(id);

        return findById(id)
                .orElse(integration);
    }

    @Override
    public Integration update(
            Long id,
            Integration integration) {

        String sql = """
                update ih_integration
                   set name = :name,
                       description = :description,
                       base_path = :basePath,
                       active = :active,
                       updated_by = :updatedBy,
                       updated_at = systimestamp
                 where id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", integration.getName())
                .addValue(
                        "description",
                        integration.getDescription()
                )
                .addValue(
                        "basePath",
                        integration.getBasePath()
                )
                .addValue(
                        "active",
                        integration.getActive() == null
                                ? "S"
                                : integration.getActive()
                )
                .addValue(
                        "updatedBy",
                        integration.getUpdatedBy() == null
                                ? "SYSTEM"
                                : integration.getUpdatedBy()
                );

        int rows = jdbcTemplate.update(sql, params);

        if (rows == 0) {
            throw new IllegalArgumentException(
                    "Integração não encontrada: " + id
            );
        }

        return findById(id)
                .orElseThrow();
    }

    @Override
    public void deleteById(Long id) {

        String sql = """
                delete from ih_integration
                where id = :id
                """;

        int rows = jdbcTemplate.update(
                sql,
                Map.of("id", id)
        );

        if (rows == 0) {
            throw new IllegalArgumentException(
                    "Integração não encontrada: " + id
            );
        }
    }

    private Integration mapRow(ResultSet rs)
            throws SQLException {

        Integration integration = new Integration();

        integration.setId(rs.getLong("id"));
        integration.setName(rs.getString("name"));
        integration.setDescription(
                rs.getString("description")
        );
        integration.setBasePath(
                rs.getString("base_path")
        );
        integration.setActive(
                rs.getString("active")
        );
        integration.setCreatedBy(
                rs.getString("created_by")
        );

        Timestamp createdAt =
                rs.getTimestamp("created_at");

        if (createdAt != null) {
            integration.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        integration.setUpdatedBy(
                rs.getString("updated_by")
        );

        Timestamp updatedAt =
                rs.getTimestamp("updated_at");

        if (updatedAt != null) {
            integration.setUpdatedAt(
                    updatedAt.toLocalDateTime()
            );
        }

        return integration;
    }
}