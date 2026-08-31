package br.com.integrationhub.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    private final DataSourceProperties properties;

    public DataSourceConfig(
            DataSourceProperties properties) {

        this.properties = properties;
    }

    @Bean
    @Primary
    public DataSource dataSource() {

        if (properties
                .getConnections()
                .isEmpty()) {

            throw new IllegalStateException(
                    "Nenhuma conexão de banco foi configurada"
            );
        }

        Map<Object, Object> dataSources =
                new LinkedHashMap<>();

        properties
                .getConnections()
                .forEach(
                        (environment, connection) ->
                                dataSources.put(
                                        environment,
                                        createDataSource(
                                                environment,
                                                connection
                                        )
                                )
                );

        RoutingDataSource routingDataSource =
                new RoutingDataSource();

        routingDataSource.setTargetDataSources(
                dataSources
        );

        /*
         * Não existe datasource padrão.
         *
         * Toda operação no banco deve ter
         * um ambiente selecionado.
         */
        routingDataSource.setLenientFallback(false);

        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    private DataSource createDataSource(
            String environment,
            DataSourceProperties.ConnectionProperties connection) {

        HikariDataSource dataSource =
                new HikariDataSource();

        dataSource.setPoolName(
                "IntegrationHub-" + environment
        );

        dataSource.setDriverClassName(
                "oracle.jdbc.OracleDriver"
        );

        dataSource.setJdbcUrl(
                connection.getUrl()
        );

        dataSource.setUsername(
                connection.getUsername()
        );

        dataSource.setPassword(
                connection.getPassword()
        );

        dataSource.setMinimumIdle(1);
        dataSource.setMaximumPoolSize(5);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }
}
