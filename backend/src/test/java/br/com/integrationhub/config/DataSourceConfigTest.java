package br.com.integrationhub.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSourceConfigTest {

    @Test
    void deveRejeitarConfiguracaoSemAmbientes() {
        DataSourceProperties properties = new DataSourceProperties();

        assertThrows(
                IllegalStateException.class,
                () -> new DataSourceConfig(properties).dataSource()
        );
    }

    @Test
    void deveCriarDatasourceDeRoteamentoParaAmbienteConfigurado() {
        DataSourceProperties properties = new DataSourceProperties();
        DataSourceProperties.ConnectionProperties connection =
                new DataSourceProperties.ConnectionProperties();
        connection.setUrl("jdbc:oracle:thin:@localhost:1521/xepdb1");
        connection.setUsername("integration_hub");
        connection.setPassword("senha");
        LinkedHashMap<String, DataSourceProperties.ConnectionProperties>
                connections = new LinkedHashMap<>();
        connections.put("dev", connection);
        properties.setConnections(connections);

        Object dataSource = new DataSourceConfig(properties).dataSource();

        assertInstanceOf(AbstractRoutingDataSource.class, dataSource);
    }
}
