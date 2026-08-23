package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        dynamicEndpointService = new DynamicEndpointService(jdbcTemplate);
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
                eq(endpoint.getSqlText()),
                any(MapSqlParameterSource.class)
        )).thenReturn(expectedResult);

        List<Map<String, Object>> result =
                dynamicEndpointService.executeGet(
                        endpoint,
                        Map.of("id", "1")
                );

        assertEquals(expectedResult, result);

        verify(jdbcTemplate).queryForList(
                eq(endpoint.getSqlText()),
                any(MapSqlParameterSource.class)
        );
    }

    @Test
    void deveConverterNumberParaBigDecimal() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where id = :id",
                new EndpointParameter("id", "NUMBER", true)
        );

        when(jdbcTemplate.queryForList(
                eq(endpoint.getSqlText()),
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
                eq(endpoint.getSqlText()),
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
                any(String.class),
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
    }

    @Test
    void deveRejeitarTipoNaoSuportado() {

        Endpoint endpoint = createEndpoint(
                "select id from pedido where data_pedido = :data",
                new EndpointParameter("data", "DATE", true)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> dynamicEndpointService.executeGet(
                                endpoint,
                                Map.of("data", "2026-08-23")
                        )
                );

        assertEquals(
                "Tipo de parâmetro não suportado: DATE",
                exception.getMessage()
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
}