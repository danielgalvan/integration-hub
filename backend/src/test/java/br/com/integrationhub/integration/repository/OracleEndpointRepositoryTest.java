package br.com.integrationhub.integration.repository;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleEndpointRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private JsonMapper jsonMapper;
    private OracleEndpointRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        jsonMapper = mock(JsonMapper.class);
        repository = new OracleEndpointRepository(jdbcTemplate, jsonMapper);
    }

    @Test
    void deveMapearParametrosJsonAoBuscarPorIntegracao() throws Exception {

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("id")).thenReturn(3L);
        when(resultSet.getLong("integration_id")).thenReturn(8L);
        when(resultSet.getString("name")).thenReturn("Buscar pedido");
        when(resultSet.getString("path")).thenReturn("/buscar");
        when(resultSet.getString("method")).thenReturn("GET");
        when(resultSet.getString("sql_text")).thenReturn("select id from pedido");
        when(resultSet.getString("parameters")).thenReturn(
                "[{\"name\":\"id\",\"type\":\"NUMBER\",\"required\":true}]"
        );
        when(resultSet.getString("active")).thenReturn("S");
        when(jsonMapper.readValue(
                anyString(),
                ArgumentMatchers.<TypeReference<List<EndpointParameter>>>any()
        )).thenReturn(List.of(new EndpointParameter("id", "NUMBER", true)));

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Endpoint>>any()
        )).thenAnswer(invocation -> {
            RowMapper<Endpoint> mapper = invocation.getArgument(2);
            return List.of(mapper.mapRow(resultSet, 0));
        });

        List<Endpoint> result = repository.findByIntegrationId(8L);

        assertEquals(1, result.size());
        assertEquals(3L, result.getFirst().getId());
        assertEquals("id", result.getFirst().getParameters().getFirst().getName());

        ArgumentCaptor<Map<String, ?>> paramsCaptor = createMapCaptor();
        verify(jdbcTemplate).query(
                anyString(),
                paramsCaptor.capture(),
                ArgumentMatchers.<RowMapper<Endpoint>>any()
        );
        assertEquals(8L, paramsCaptor.getValue().get("integrationId"));
    }

    @Test
    void deveBuscarParaExecucaoSomenteQuandoAtivo() {

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Endpoint>>any()
        )).thenReturn(List.of());

        Optional<Endpoint> result = repository
                .findByIntegrationIdAndPathAndMethod(8L, "/buscar", "GET");

        assertTrue(result.isEmpty());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, ?>> paramsCaptor = createMapCaptor();
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                paramsCaptor.capture(),
                ArgumentMatchers.<RowMapper<Endpoint>>any()
        );
        assertTrue(normalizeSql(sqlCaptor.getValue()).contains("and active = 's'"));
        assertEquals("GET", paramsCaptor.getValue().get("method"));
    }

    @Test
    void deveSerializarParametrosAoSalvar() throws Exception {

        Endpoint endpoint = createEndpoint();
        endpoint.setParameters(List.of(new EndpointParameter("id", "NUMBER", true)));
        when(jdbcTemplate.queryForObject(anyString(), any(Map.class), ArgumentMatchers.eq(Long.class)))
                .thenReturn(10L);
        when(jsonMapper.writeValueAsString(endpoint.getParameters()))
                .thenReturn("[{\"name\":\"id\"}]");
        stubFindByIdAsEmpty();

        Endpoint result = repository.save(endpoint);

        assertEquals(10L, result.getId());
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(anyString(), paramsCaptor.capture());
        assertEquals("[{\"name\":\"id\"}]", paramsCaptor.getValue().getValue("parameters"));
        assertEquals("S", paramsCaptor.getValue().getValue("active"));
        assertEquals("SYSTEM", paramsCaptor.getValue().getValue("createdBy"));
    }

    @Test
    void deveExcluirEndpointPeloId() {

        repository.deleteById(9L);

        verify(jdbcTemplate).update(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>argThat(params ->
                        params.get("id").equals(9L)
                )
        );
    }

    private void stubFindByIdAsEmpty() {
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<Map<String, ?>>any(),
                ArgumentMatchers.<RowMapper<Endpoint>>any()
        )).thenReturn(List.of());
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, ?>> createMapCaptor() {
        return ArgumentCaptor.forClass((Class<Map<String, ?>>) (Class<?>) Map.class);
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    private Endpoint createEndpoint() {
        Endpoint endpoint = new Endpoint();
        endpoint.setIntegrationId(8L);
        endpoint.setName("Buscar pedido");
        endpoint.setPath("/buscar");
        endpoint.setMethod("GET");
        endpoint.setSqlText("select id from pedido where id = :id");
        return endpoint;
    }
}
