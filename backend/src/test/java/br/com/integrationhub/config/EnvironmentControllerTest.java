package br.com.integrationhub.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentControllerTest {

    @Test
    void deveListarIdentificadorENomeDosAmbientes() {
        DataSourceProperties properties = new DataSourceProperties();
        DataSourceProperties.ConnectionProperties dev =
                new DataSourceProperties.ConnectionProperties();
        dev.setName("Desenvolvimento");
        DataSourceProperties.ConnectionProperties homolog =
                new DataSourceProperties.ConnectionProperties();
        homolog.setName("Homologação");
        LinkedHashMap<String, DataSourceProperties.ConnectionProperties>
                connections = new LinkedHashMap<>();
        connections.put("dev", dev);
        connections.put("homolog", homolog);
        properties.setConnections(connections);

        var environments = new EnvironmentController(properties).findAll();

        assertEquals(2, environments.size());
        assertEquals(new EnvironmentResponse("dev", "Desenvolvimento"), environments.get(0));
        assertEquals(new EnvironmentResponse("homolog", "Homologação"), environments.get(1));
    }
}
