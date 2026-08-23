package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class DynamicEndpointService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DynamicEndpointService(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeGet(
            Endpoint endpoint,
            Map<String, String> requestParameters) {

        validateSql(endpoint.getSqlText());

        MapSqlParameterSource sqlParameters =
                buildSqlParameters(
                        endpoint,
                        requestParameters
                );

        return jdbcTemplate.queryForList(
                endpoint.getSqlText(),
                sqlParameters
        );
    }

    private MapSqlParameterSource buildSqlParameters(
            Endpoint endpoint,
            Map<String, String> requestParameters) {

        MapSqlParameterSource sqlParameters =
                new MapSqlParameterSource();

        List<EndpointParameter> parameters =
                endpoint.getParameters();

        if (parameters == null || parameters.isEmpty()) {
            return sqlParameters;
        }

        for (EndpointParameter parameter : parameters) {

            String value = requestParameters.get(
                    parameter.getName()
            );

            if (parameter.isRequired()
                    && (value == null || value.isBlank())) {

                throw new IllegalArgumentException(
                        "Parâmetro obrigatório não informado: "
                                + parameter.getName()
                );
            }

            if (value == null || value.isBlank()) {
                sqlParameters.addValue(
                        parameter.getName(),
                        null
                );

                continue;
            }

            sqlParameters.addValue(
                    parameter.getName(),
                    convertValue(
                            parameter,
                            value
                    )
            );
        }

        return sqlParameters;
    }

    private Object convertValue(
            EndpointParameter parameter,
            String value) {

        String type = parameter.getType();

        if (type == null) {
            return value;
        }

        return switch (type.toUpperCase()) {

            case "NUMBER" ->
                    convertNumber(
                            parameter.getName(),
                            value
                    );

            case "VARCHAR2", "VARCHAR", "CHAR" ->
                    value;

            default ->
                    throw new IllegalArgumentException(
                            "Tipo de parâmetro não suportado: "
                                    + type
                    );
        };
    }

    private BigDecimal convertNumber(
            String parameterName,
            String value) {

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Parâmetro "
                            + parameterName
                            + " deve ser numérico"
            );
        }
    }

    private void validateSql(String sql) {

        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException(
                    "SQL do endpoint não informado"
            );
        }

        String normalizedSql =
                sql.trim().toLowerCase();

        if (!normalizedSql.startsWith("select")) {
            throw new IllegalArgumentException(
                    "Endpoints GET permitem apenas comandos SELECT"
            );
        }
    }
}