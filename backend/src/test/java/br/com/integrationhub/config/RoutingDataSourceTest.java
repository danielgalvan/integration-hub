package br.com.integrationhub.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoutingDataSourceTest {

    @AfterEach
    void clearEnvironment() {
        EnvironmentContext.clear();
    }

    @Test
    void deveUsarDatasourceDoAmbienteSelecionado() throws Exception {
        DataSource development = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(development.getConnection())
                .thenReturn(connection);

        RoutingDataSource dataSource = createDataSource(
                Map.of("development", development)
        );

        EnvironmentContext.set("development");

        assertSame(
                connection,
                dataSource.getConnection()
        );

        verify(development).getConnection();
    }

    @Test
    void deveUsarDatasourcePadraoQuandoExisteApenasUmaConexao()
            throws Exception {

        DataSource cloud = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(cloud.getConnection())
                .thenReturn(connection);

        RoutingDataSource dataSource = createDataSource(
                Map.of("cloud", cloud)
        );

        assertSame(
                connection,
                dataSource.getConnection()
        );

        verify(cloud).getConnection();
    }

    @Test
    void deveRejeitarAmbienteInexistente() {

        RoutingDataSource dataSource = createDataSource(
                Map.of(
                        "cloud",
                        mock(DataSource.class)
                )
        );

        EnvironmentContext.set("inexistente");

        assertThrows(
                IllegalStateException.class,
                dataSource::getConnection
        );
    }

    private RoutingDataSource createDataSource(
            Map<Object, Object> dataSources) {

        RoutingDataSource dataSource =
                new RoutingDataSource();

        dataSource.setTargetDataSources(
                dataSources
        );

        if (dataSources.size() == 1) {
            dataSource.setDefaultTargetDataSource(
                    dataSources.values()
                            .iterator()
                            .next()
            );
        }

        dataSource.setLenientFallback(false);
        dataSource.afterPropertiesSet();

        return dataSource;
    }
}
