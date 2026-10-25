
package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Integration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleIntegrationRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private OracleIntegrationRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        repository = new OracleIntegrationRepository(jdbcTemplate);
    }

    @Test
    void deveRetornarMelhorIntegrationParaRequestPath() {

        Integration integration = createIntegration(
                8L, "Pedidos", "/api/pedidos", "S");

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of(integration));

        Optional<Integration> result =
                repository.findBestMatchByRequestPath(
                        "/api/pedidos/buscar");

        assertTrue(result.isPresent());
        assertEquals(8L, result.get().getId());
        assertEquals("/api/pedidos", result.get().getBasePath());
    }

    @Test
    void deveRetornarEmptyQuandoNaoEncontrarIntegration() {

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of());

        Optional<Integration> result =
                repository.findBestMatchByRequestPath(
                        "/api/inexistente/buscar");

        assertTrue(result.isEmpty());
    }

    @Test
    void deveEnviarRequestPathComoParametroDaConsulta() {

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of());

        repository.findBestMatchByRequestPath(
                "/api/pedidos/buscar");

        ArgumentCaptor<Map<String, ?>> paramsCaptor =
                createMapCaptor();

        verify(jdbcTemplate).query(
                anyString(),
                paramsCaptor.capture(),
                ArgumentMatchers.<RowMapper<Integration>>any());

        assertEquals(
                "/api/pedidos/buscar",
                paramsCaptor.getValue().get("requestPath"));
    }

    @Test
    void deveBuscarSomenteIntegrationsAtivas() {

        String sql = captureSql("/api/pedidos/buscar");

        assertTrue(
                normalizeSql(sql).contains("where active = 's'"));
    }

    @Test
    void naoDevePermitirPrefixoParcialDeBasePath() {

        String sql = captureSql(
                "/api/pedidos-especiais/buscar");

        String normalizedSql = normalizeSql(sql);

        assertTrue(normalizedSql.contains(
                ":requestpath = base_path"));

        assertTrue(normalizedSql.contains(
                "= base_path || '/'"));
    }

    @Test
    void devePriorizarBasePathMaisEspecifico() {

        String sql = captureSql("/api/pedidos/buscar");

        assertTrue(normalizeSql(sql).contains(
                "order by length(base_path) desc"));
    }

    @Test
    void deveRetornarApenasUmaIntegration() {

        String sql = captureSql("/api/pedidos/buscar");

        assertTrue(normalizeSql(sql).contains(
                "fetch first 1 row only"));
    }

    @Test
    void devePersistirHashEDataAoAtualizarApiKey() {

        LocalDateTime createdAt = LocalDateTime.of(
                2026, 8, 29, 16, 0);

        Integration integration = createIntegration(
                8L, "Pedidos", "/api/pedidos", "S");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of(integration));

        repository.updateApiKey(
                8L, "hash-da-chave", createdAt);

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture());

        assertTrue(normalizeSql(sqlCaptor.getValue()).contains(
                "set api_key_hash = :apikeyhash"));

        assertEquals(8L,
                paramsCaptor.getValue().getValue("id"));

        assertEquals("hash-da-chave",
                paramsCaptor.getValue().getValue("apiKeyHash"));

        assertEquals(createdAt,
                paramsCaptor.getValue().getValue("apiKeyCreatedAt"));
    }

    @Test
    void deveConsultarIndicadorLegadoNaBuscaPorCaminho() {

        String sql = captureSql("/api/pedidos/buscar");

        assertTrue(normalizeSql(sql).contains(
                "legacy_api_key_active"));
    }

    @Test
    void deveConsultarIndicadorLegadoNoFindAll() {

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of());

        repository.findAll();

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any());

        assertTrue(normalizeSql(sqlCaptor.getValue()).contains(
                "legacy_api_key_active"));
    }

    @Test
    void deveMapearIndicadorLegadoNoFindById() throws Exception {

        ResultSet rs = mock(ResultSet.class);

        when(rs.getLong("id")).thenReturn(8L);
        when(rs.getString("name")).thenReturn("Pedidos");
        when(rs.getString("base_path")).thenReturn("/api/pedidos");
        when(rs.getString("legacy_api_key_active")).thenReturn("S");

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenAnswer(invocation -> {

            RowMapper<Integration> mapper =
                    invocation.getArgument(2);

            return List.of(mapper.mapRow(rs, 0));
        });

        Optional<Integration> result = repository.findById(8L);

        assertTrue(result.isPresent());
        assertEquals("S", result.get().getLegacyApiKeyActive());
    }

    @Test
    void deveSalvarNovaIntegracaoComChaveLegadaDesativada() {

        Integration integration = createIntegration(
                null, "Pedidos", "/api/pedidos", "S");

        when(jdbcTemplate.queryForObject(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                eq(Long.class)
        )).thenReturn(8L);

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(1);

        repository.save(integration);

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture());

        assertTrue(normalizeSql(sqlCaptor.getValue()).contains(
                "legacy_api_key_active"));

        assertEquals("N",
                paramsCaptor.getValue().getValue(
                        "legacyApiKeyActive"));
    }

    @Test
    void deveAtualizarIndicadorLegadoParaS() {

        Integration integration = createIntegration(
                8L, "Pedidos", "/api/pedidos", "S");

        integration.setLegacyApiKeyActive("S");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of(integration));

        Integration result =
                repository.updateLegacyApiKeyActive(8L, "S");

        assertEquals("S", result.getLegacyApiKeyActive());

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                paramsCaptor.capture());

        assertTrue(normalizeSql(sqlCaptor.getValue()).contains(
                "set legacy_api_key_active = :legacyapikeyactive"));

        assertEquals(8L,
                paramsCaptor.getValue().getValue("id"));

        assertEquals("S",
                paramsCaptor.getValue().getValue(
                        "legacyApiKeyActive"));
    }

    @Test
    void deveAtualizarIndicadorLegadoParaN() {

        Integration integration = createIntegration(
                8L, "Pedidos", "/api/pedidos", "S");

        integration.setLegacyApiKeyActive("N");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(1);

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of(integration));

        Integration result =
                repository.updateLegacyApiKeyActive(8L, "N");

        assertEquals("N", result.getLegacyApiKeyActive());

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                anyString(),
                paramsCaptor.capture());

        assertEquals("N",
                paramsCaptor.getValue().getValue(
                        "legacyApiKeyActive"));
    }

    @Test
    void deveRejeitarIndicadorLegadoInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateLegacyApiKeyActive(8L, "X"));

        verify(jdbcTemplate, never()).update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class));
    }

    @Test
    void deveRejeitarAtualizacaoLegadaDeIntegracaoInexistente() {

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateLegacyApiKeyActive(99L, "N"));

        assertEquals(
                "Integração não encontrada: 99",
                exception.getMessage());
    }

    private String captureSql(String requestPath) {

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any()
        )).thenReturn(List.of());

        repository.findBestMatchByRequestPath(requestPath);

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Integration>>any());

        return sqlCaptor.getValue();
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, ?>> createMapCaptor() {

        return ArgumentCaptor.forClass(
                (Class<Map<String, ?>>) (Class<?>) Map.class);
    }

    private String normalizeSql(String sql) {

        return sql
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }

    private Integration createIntegration(
            Long id,
            String name,
            String basePath,
            String active) {

        Integration integration = new Integration();

        integration.setId(id);
        integration.setName(name);
        integration.setBasePath(basePath);
        integration.setActive(active);

        return integration;
    }
}
