package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamicEndpointServiceTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private DynamicEndpointService dynamicEndpointService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        dynamicEndpointService = new DynamicEndpointService(
                jdbcTemplate,
                1000
        );
    }

    @Test
    void deveExecutarSelectComParametroNumber() {

        Endpoint endpoint = createEndpoint(
                "select id, status from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        List<Map<String, Object>> expectedResult = List.of(
                Map.of(
                        "ID", 1,
                        "STATUS", "ABERTO"
                )
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenReturn(expectedResult);

        List<Map<String, Object>> result =
                dynamicEndpointService.executeGet(
                        endpoint,
                        Map.of("id", "1")
                );

        assertEquals(expectedResult, result);
    }

    @Test
    void deveConverterNumberParaBigDecimal() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenAnswer(invocation -> {

            MapSqlParameterSource parameters =
                    invocation.getArgument(1);

            assertEquals(
                    new BigDecimal("10"),
                    parameters.getValue("id")
            );

            return List.of();
        });

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of("id", "10")
        );
    }

    @Test
    void deveExecutarSelectComParametroVarchar2() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where status = :status",
                new EndpointParameter("status", "VARCHAR2", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenAnswer(invocation -> {

            MapSqlParameterSource parameters =
                    invocation.getArgument(1);

            assertEquals(
                    "ABERTO",
                    parameters.getValue("status")
            );

            return List.of();
        });

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of("status", "ABERTO")
        );
    }

    @Test
    void deveConverterDate() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where data_pedido >= :data",
                new EndpointParameter("data", "DATE", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenAnswer(invocation -> {

            MapSqlParameterSource parameters =
                    invocation.getArgument(1);

            assertEquals(
                    Date.valueOf("2026-08-23"),
                    parameters.getValue("data")
            );

            return List.of();
        });

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of("data", "2026-08-23")
        );
    }

    @Test
    void deveRejeitarDateInvalida() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where data_pedido >= :data",
                new EndpointParameter("data", "DATE", true)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> dynamicEndpointService.executeGet(
                                endpoint,
                                Map.of("data", "23/08/2026")
                        )
                );

        assertEquals(
                "Parâmetro data deve estar no formato yyyy-MM-dd",
                exception.getMessage()
        );

        verify(
                jdbcTemplate,
                never()
        ).queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        );
    }

    @Test
    void deveConverterTimestamp() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where data_pedido >= :dataHora",
                new EndpointParameter("dataHora", "TIMESTAMP", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenAnswer(invocation -> {

            MapSqlParameterSource parameters =
                    invocation.getArgument(1);

            assertEquals(
                    Timestamp.valueOf(
                            "2026-08-23 16:45:30"
                    ),
                    parameters.getValue("dataHora")
            );

            return List.of();
        });

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of(
                        "dataHora",
                        "2026-08-23T16:45:30"
                )
        );
    }

    @Test
    void deveRejeitarTimestampInvalido() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where data_pedido >= :dataHora",
                new EndpointParameter("dataHora", "TIMESTAMP", true)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> dynamicEndpointService.executeGet(
                                endpoint,
                                Map.of(
                                        "dataHora",
                                        "23/08/2026 16:45"
                                )
                        )
                );

        assertEquals(
                "Parâmetro dataHora deve estar no formato yyyy-MM-dd'T'HH:mm:ss",
                exception.getMessage()
        );

        verify(
                jdbcTemplate,
                never()
        ).queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        );
    }

    @Test
    void deveAplicarLimiteGlobalDeResultados() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenReturn(List.of());

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of("id", "1")
        );

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(
                        MapSqlParameterSource.class
                );

        verify(jdbcTemplate).queryForList(
                anyString(),
                paramsCaptor.capture()
        );

        assertEquals(
                1000,
                paramsCaptor
                        .getValue()
                        .getValue("__ih_max_results")
        );
    }

    @Test
    void deveEnveloparSqlComLimiteDeResultados() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenReturn(List.of());

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of("id", "1")
        );

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate).queryForList(
                sqlCaptor.capture(),
                any(MapSqlParameterSource.class)
        );

        String sql = normalizeSql(
                sqlCaptor.getValue()
        );

        assertTrue(
                sql.contains(
                        "select * from ( select id from pedido where id = :id )"
                )
        );

        assertTrue(
                sql.contains(
                        "where rownum <= :__ih_max_results"
                )
        );
    }

    @Test
    void devePreservarParametrosOriginaisAoAplicarLimite() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenReturn(List.of());

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of("id", "25")
        );

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor =
                ArgumentCaptor.forClass(
                        MapSqlParameterSource.class
                );

        verify(jdbcTemplate).queryForList(
                anyString(),
                paramsCaptor.capture()
        );

        MapSqlParameterSource parameters =
                paramsCaptor.getValue();

        assertEquals(
                new BigDecimal("25"),
                parameters.getValue("id")
        );

        assertEquals(
                1000,
                parameters.getValue("__ih_max_results")
        );
    }

    @Test
    void deveRemoverPontoEVirgulaDoFinalDoSql() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id;",
                new EndpointParameter("id", "NUMBER", true)
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        )).thenReturn(List.of());

        dynamicEndpointService.executeGet(
                endpoint,
                Map.of("id", "1")
        );

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate).queryForList(
                sqlCaptor.capture(),
                any(MapSqlParameterSource.class)
        );

        String sql = sqlCaptor.getValue();

        assertFalse(
                sql.contains("where id = :id;")
        );

        assertTrue(
                normalizeSql(sql).contains(
                        "where id = :id"
                )
        );
    }

    @Test
    void deveRejeitarParametroObrigatorioAusente() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> dynamicEndpointService.executeGet(
                                endpoint,
                                Map.of()
                        )
                );

        assertEquals(
                "Parâmetro obrigatório não informado: id",
                exception.getMessage()
        );

        verify(
                jdbcTemplate,
                never()
        ).queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        );
    }

    @Test
    void deveRejeitarNumberInvalido() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> dynamicEndpointService.executeGet(
                                endpoint,
                                Map.of("id", "abc")
                        )
                );

        assertEquals(
                "Parâmetro id deve ser numérico",
                exception.getMessage()
        );

        verify(
                jdbcTemplate,
                never()
        ).queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        );
    }

    @Test
    void deveRejeitarComandoQueNaoSejaSelect() {

        Endpoint endpoint = createEndpoint(
                "delete from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> dynamicEndpointService.executeGet(
                                endpoint,
                                Map.of("id", "1")
                        )
                );

        assertEquals(
                "Endpoints GET permitem apenas comandos SELECT",
                exception.getMessage()
        );

        verify(
                jdbcTemplate,
                never()
        ).queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        );
    }

    @Test
    void deveRejeitarTipoNaoSuportado() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where arquivo = :arquivo",
                new EndpointParameter("arquivo", "BLOB", true)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> dynamicEndpointService.executeGet(
                                endpoint,
                                Map.of("arquivo", "teste")
                        )
                );

        assertEquals(
                "Tipo de parâmetro não suportado: BLOB",
                exception.getMessage()
        );

        verify(
                jdbcTemplate,
                never()
        ).queryForList(
                anyString(),
                any(MapSqlParameterSource.class)
        );
    }

    private Endpoint createEndpoint(
            String sql,
            EndpointParameter parameter) {

        Endpoint endpoint = new Endpoint();

        endpoint.setId(1L);
        endpoint.setIntegrationId(8L);
        endpoint.setName("Endpoint teste");
        endpoint.setPath("/buscar");
        endpoint.setMethod("GET");
        endpoint.setSqlText(sql);
        endpoint.setParameters(List.of(parameter));
        endpoint.setActive("S");

        return endpoint;
    }

    private String normalizeSql(String sql) {
        return sql
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}