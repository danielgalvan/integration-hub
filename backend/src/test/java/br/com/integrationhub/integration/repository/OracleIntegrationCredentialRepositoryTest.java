package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.IntegrationCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleIntegrationCredentialRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private OracleIntegrationCredentialRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate =
                mock(NamedParameterJdbcTemplate.class);

        repository =
                new OracleIntegrationCredentialRepository(
                        jdbcTemplate);
    }

    @Test
    void deveBuscarCredenciaisPorIntegrationId() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of(credential));

        List<IntegrationCredential> result =
                repository.findByIntegrationId(1L);

        assertEquals(1, result.size());
        assertEquals(
                10L,
                result.getFirst().getId()
        );
        assertEquals(
                1L,
                result.getFirst().getIntegrationId()
        );
        assertEquals(
                "Sistema Tasy",
                result.getFirst().getName()
        );
    }

    @Test
    void deveEnviarIntegrationIdComoParametroDaConsulta() {
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of());

        repository.findByIntegrationId(5L);

        ArgumentCaptor<Map<String, ?>> paramsCaptor =
                createMapCaptor();

        verify(jdbcTemplate).query(
                anyString(),
                paramsCaptor.capture(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        );

        assertEquals(
                5L,
                paramsCaptor.getValue()
                        .get("integrationId")
        );
    }

    @Test
    void deveOrdenarCredenciaisPorNome() {
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of());

        repository.findByIntegrationId(1L);

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(
                        String.class);

        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        );

        assertTrue(
                normalizeSql(
                        sqlCaptor.getValue()
                ).contains(
                        "order by name"
                )
        );
    }

    @Test
    void deveBuscarCredencialPorId() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of(credential));

        Optional<IntegrationCredential> result =
                repository.findById(10L);

        assertTrue(result.isPresent());
        assertEquals(
                10L,
                result.get().getId()
        );
        assertEquals(
                "Sistema Tasy",
                result.get().getName()
        );
    }

    @Test
    void deveRetornarEmptyQuandoCredencialNaoExistir() {
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of());

        Optional<IntegrationCredential> result =
                repository.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void devePersistirCredencial() {
        IntegrationCredential credential =
                createCredential(
                        null,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        credential.setApiKeyEncrypted(
                "chave-criptografada");

        credential.setCreatedBy(
                "admin");

        when(jdbcTemplate.queryForObject(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.eq(Long.class)
        )).thenReturn(10L);

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(
                        MapSqlParameterSource.class)
        )).thenReturn(1);

        IntegrationCredential persisted =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of(persisted));

        IntegrationCredential result =
                repository.save(credential);

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(
                        String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(
                        MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture()
        );

        String sql =
                normalizeSql(
                        sqlCaptor.getValue());

        assertTrue(
                sql.contains(
                        "insert into ih_integration_credential"
                )
        );

        assertEquals(
                10L,
                paramsCaptor.getValue()
                        .getValue("id")
        );

        assertEquals(
                1L,
                paramsCaptor.getValue()
                        .getValue("integrationId")
        );

        assertEquals(
                "Sistema Tasy",
                paramsCaptor.getValue()
                        .getValue("name")
        );

        assertEquals(
                "chave-criptografada",
                paramsCaptor.getValue()
                        .getValue("apiKeyEncrypted")
        );

        assertEquals(
                "S",
                paramsCaptor.getValue()
                        .getValue("active")
        );

        assertEquals(
                "admin",
                paramsCaptor.getValue()
                        .getValue("createdBy")
        );

        assertEquals(
                10L,
                result.getId()
        );
    }

    @Test
    void deveFalharQuandoCredencialNaoForEncontradaAposCriacao() {
        IntegrationCredential credential =
                createCredential(
                        null,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        credential.setApiKeyEncrypted(
                "chave-criptografada");

        credential.setCreatedBy(
                "admin");

        when(jdbcTemplate.queryForObject(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.eq(Long.class)
        )).thenReturn(10L);

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(
                        MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.save(
                                credential)
                );

        assertEquals(
                "Credencial não encontrada após criação",
                exception.getMessage()
        );
    }

    @Test
    void deveAtualizarNomeDaCredencial() {
        IntegrationCredential updated =
                createCredential(
                        10L,
                        1L,
                        "Sistema Financeiro",
                        "S"
                );

        updated.setUpdatedBy(
                "admin");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(
                        MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of(updated));

        IntegrationCredential result =
                repository.updateName(
                        10L,
                        "Sistema Financeiro",
                        "admin"
                );

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(
                        String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(
                        MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture()
        );

        String sql =
                normalizeSql(
                        sqlCaptor.getValue());

        assertTrue(
                sql.contains(
                        "update ih_integration_credential"
                )
        );

        assertTrue(
                sql.contains(
                        "set name = :name"
                )
        );

        assertTrue(
                !sql.contains(
                        "api_key_encrypted ="
                )
        );

        assertTrue(
                !sql.contains(
                        "active ="
                )
        );

        assertEquals(
                10L,
                paramsCaptor.getValue()
                        .getValue("id")
        );

        assertEquals(
                "Sistema Financeiro",
                paramsCaptor.getValue()
                        .getValue("name")
        );

        assertEquals(
                "admin",
                paramsCaptor.getValue()
                        .getValue("updatedBy")
        );

        assertEquals(
                "Sistema Financeiro",
                result.getName()
        );
    }

    @Test
    void deveAtualizarStatusDaCredencial() {
        IntegrationCredential updated =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "N"
                );

        updated.setUpdatedBy(
                "admin");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(
                        MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of(updated));

        IntegrationCredential result =
                repository.updateActive(
                        10L,
                        "N",
                        "admin"
                );

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(
                        String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(
                        MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture()
        );

        String sql =
                normalizeSql(
                        sqlCaptor.getValue());

        assertTrue(
                sql.contains(
                        "set active = :active"
                )
        );

        assertTrue(
                !sql.contains(
                        "api_key_encrypted ="
                )
        );

        assertTrue(
                !sql.contains(
                        "name ="
                )
        );

        assertEquals(
                10L,
                paramsCaptor.getValue()
                        .getValue("id")
        );

        assertEquals(
                "N",
                paramsCaptor.getValue()
                        .getValue("active")
        );

        assertEquals(
                "admin",
                paramsCaptor.getValue()
                        .getValue("updatedBy")
        );

        assertEquals(
                "N",
                result.getActive()
        );
    }

    @Test
    void deveAtualizarApiKeyDaCredencial() {
        IntegrationCredential updated =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        updated.setApiKeyEncrypted(
                "nova-chave-criptografada");

        updated.setUpdatedBy(
                "admin");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(
                        MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of(updated));

        IntegrationCredential result =
                repository.updateApiKey(
                        10L,
                        "nova-chave-criptografada",
                        "admin"
                );

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(
                        String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(
                        MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture()
        );

        String sql =
                normalizeSql(
                        sqlCaptor.getValue());

        assertTrue(
                sql.contains(
                        "set api_key_encrypted = :apikeyencrypted"
                )
        );

        assertTrue(
                !sql.contains(
                        "name ="
                )
        );

        assertTrue(
                !sql.contains(
                        "active ="
                )
        );

        assertEquals(
                10L,
                paramsCaptor.getValue()
                        .getValue("id")
        );

        assertEquals(
                "nova-chave-criptografada",
                paramsCaptor.getValue()
                        .getValue("apiKeyEncrypted")
        );

        assertEquals(
                "admin",
                paramsCaptor.getValue()
                        .getValue("updatedBy")
        );

        assertEquals(
                "nova-chave-criptografada",
                result.getApiKeyEncrypted()
        );
    }

    @Test
    void deveFalharQuandoCredencialNaoForEncontradaAposAtualizarNome() {
        prepareUpdateWithoutResult();

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.updateName(
                                10L,
                                "Sistema Tasy",
                                "admin"
                        )
                );

        assertEquals(
                "Credencial não encontrada após atualização",
                exception.getMessage()
        );
    }

    @Test
    void deveFalharQuandoCredencialNaoForEncontradaAposAtualizarStatus() {
        prepareUpdateWithoutResult();

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.updateActive(
                                10L,
                                "N",
                                "admin"
                        )
                );

        assertEquals(
                "Credencial não encontrada após atualização",
                exception.getMessage()
        );
    }

    @Test
    void deveFalharQuandoCredencialNaoForEncontradaAposAtualizarApiKey() {
        prepareUpdateWithoutResult();

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.updateApiKey(
                                10L,
                                "nova-chave-criptografada",
                                "admin"
                        )
                );

        assertEquals(
                "Credencial não encontrada após atualização",
                exception.getMessage()
        );
    }

    private void prepareUpdateWithoutResult() {
        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(
                        MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<IntegrationCredential>>any()
        )).thenReturn(List.of());
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, ?>> createMapCaptor() {
        return ArgumentCaptor.forClass(
                (Class<Map<String, ?>>)
                        (Class<?>) Map.class
        );
    }

    private String normalizeSql(
            String sql) {

        return sql
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }

    private IntegrationCredential createCredential(
            Long id,
            Long integrationId,
            String name,
            String active) {

        IntegrationCredential credential =
                new IntegrationCredential();

        credential.setId(id);
        credential.setIntegrationId(
                integrationId);
        credential.setName(name);
        credential.setActive(active);

        return credential;
    }
}
