package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
public class DynamicEndpointService {

    private static final String MAX_RESULTS_PARAMETER =
            "__ih_max_results";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final int maxResults;

    public DynamicEndpointService(
            NamedParameterJdbcTemplate jdbcTemplate,
            @Value("${integration-hub.dynamic.max-results:1000}")
            int maxResults) {

        this.jdbcTemplate = jdbcTemplate;
        this.maxResults = maxResults;
    }

    public List<Map<String, Object>> executeGet(
            Endpoint endpoint,
            Map<String, String> requestParameters) {

        String sql = normalizeSql(
                endpoint.getSqlText()
        );

        validateSql(sql);

        MapSqlParameterSource sqlParameters =
                buildSqlParameters(
                        endpoint,
                        requestParameters
                );

        sqlParameters.addValue(
                MAX_RESULTS_PARAMETER,
                maxResults
        );

        String limitedSql = applyResultLimit(sql);

        return jdbcTemplate.queryForList(
                limitedSql,
                sqlParameters
        );
    }

    private String applyResultLimit(String sql) {

        return """
                select *
                from (
                    %s
                )
                where rownum <= :%s
                """.formatted(
                sql,
                MAX_RESULTS_PARAMETER
        );
    }

    private String normalizeSql(String sql) {

        if (sql == null) {
            return null;
        }

        String normalizedSql = sql.trim();

        while (normalizedSql.endsWith(";")) {
            normalizedSql = normalizedSql
                    .substring(
                            0,
                            normalizedSql.length() - 1
                    )
                    .trim();
        }

        return normalizedSql;
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

            case "DATE" ->
                    convertDate(
                            parameter.getName(),
                            value
                    );

            case "TIMESTAMP" ->
                    convertTimestamp(
                            parameter.getName(),
                            value
                    );

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

    private Date convertDate(
            String parameterName,
            String value) {

        try {
            LocalDate date = LocalDate.parse(value);

            return Date.valueOf(date);

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Parâmetro "
                            + parameterName
                            + " deve estar no formato yyyy-MM-dd"
            );
        }
    }

    private Timestamp convertTimestamp(
            String parameterName,
            String value) {

        try {
            LocalDateTime dateTime =
                    LocalDateTime.parse(value);

            return Timestamp.valueOf(dateTime);

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Parâmetro "
                            + parameterName
                            + " deve estar no formato yyyy-MM-dd'T'HH:mm:ss"
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

        if (normalizedSql.matches("(?s).*\\bfor\\s+update\\b.*")) {
            throw new IllegalArgumentException(
                    "Endpoints GET não permitem SELECT FOR UPDATE"
            );
        }
    }
}
