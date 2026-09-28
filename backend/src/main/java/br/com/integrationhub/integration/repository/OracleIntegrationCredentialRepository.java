package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.IntegrationCredential;
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
public class OracleIntegrationCredentialRepository implements IntegrationCredentialRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OracleIntegrationCredentialRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<IntegrationCredential> findByIntegrationId(Long integrationId) {
        String sql = """
                select id,
                       integration_id,
                       name,
                       api_key_encrypted,
                       active,
                       last_used_at,
                       created_by,
                       created_at,
                       updated_by,
                       updated_at
                  from ih_integration_credential
                 where integration_id = :integrationId
                 order by name
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("integrationId", integrationId),
                this::mapRow
        );
    }

    @Override
    public Optional<IntegrationCredential> findById(Long id) {
        String sql = """
                select id,
                       integration_id,
                       name,
                       api_key_encrypted,
                       active,
                       last_used_at,
                       created_by,
                       created_at,
                       updated_by,
                       updated_at
                  from ih_integration_credential
                 where id = :id
                """;

        List<IntegrationCredential> credentials = jdbcTemplate.query(
                sql,
                Map.of("id", id),
                this::mapRow
        );

        return credentials.stream().findFirst();
    }

    @Override
    public IntegrationCredential save(IntegrationCredential credential) {
        Long id = jdbcTemplate.queryForObject(
                "select ih_integration_credential_seq.nextval from dual",
                Map.of(),
                Long.class
        );

        credential.setId(id);

        String sql = """
                insert into ih_integration_credential (
                    id,
                    integration_id,
                    name,
                    api_key_encrypted,
                    active,
                    created_by,
                    created_at
                ) values (
                    :id,
                    :integrationId,
                    :name,
                    :apiKeyEncrypted,
                    :active,
                    :createdBy,
                    current_timestamp
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", credential.getId())
                .addValue("integrationId", credential.getIntegrationId())
                .addValue("name", credential.getName())
                .addValue("apiKeyEncrypted", credential.getApiKeyEncrypted())
                .addValue("active", credential.getActive())
                .addValue("createdBy", credential.getCreatedBy());

        jdbcTemplate.update(sql, params);

        return findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Credencial não encontrada após criação"
                ));
    }

    @Override
    public IntegrationCredential update(Long id, IntegrationCredential credential) {
        String sql = """
                update ih_integration_credential
                   set name = :name,
                       api_key_encrypted = :apiKeyEncrypted,
                       active = :active,
                       updated_by = :updatedBy,
                       updated_at = current_timestamp
                 where id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", credential.getName())
                .addValue("apiKeyEncrypted", credential.getApiKeyEncrypted())
                .addValue("active", credential.getActive())
                .addValue("updatedBy", credential.getUpdatedBy());

        jdbcTemplate.update(sql, params);

        return findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Credencial não encontrada após atualização"
                ));
    }

    private IntegrationCredential mapRow(ResultSet rs, int rowNum) throws SQLException {
        IntegrationCredential credential = new IntegrationCredential();

        credential.setId(rs.getLong("id"));
        credential.setIntegrationId(rs.getLong("integration_id"));
        credential.setName(rs.getString("name"));
        credential.setApiKeyEncrypted(rs.getString("api_key_encrypted"));
        credential.setActive(rs.getString("active"));

        Timestamp lastUsedAt = rs.getTimestamp("last_used_at");
        if (lastUsedAt != null) {
            credential.setLastUsedAt(lastUsedAt.toLocalDateTime());
        }

        credential.setCreatedBy(rs.getString("created_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            credential.setCreatedAt(createdAt.toLocalDateTime());
        }

        credential.setUpdatedBy(rs.getString("updated_by"));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            credential.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return credential;
    }
}
