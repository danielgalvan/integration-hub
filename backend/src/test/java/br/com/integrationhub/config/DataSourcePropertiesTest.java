package br.com.integrationhub.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DataSourcePropertiesTest {

    @Test
    void deveRetornarConexaoDoAmbienteInformado() {
        DataSourceProperties properties = new DataSourceProperties();
        DataSourceProperties.ConnectionProperties connection =
                new DataSourceProperties.ConnectionProperties();
        LinkedHashMap<String, DataSourceProperties.ConnectionProperties>
                connections = new LinkedHashMap<>();
        connections.put("dev", connection);
        properties.setConnections(connections);

        assertSame(connection, properties.getConnection("dev"));
        assertNull(properties.getConnection(null));
        assertNull(properties.getConnection("   "));
        assertNull(properties.getConnection("inexistente"));
    }
}
