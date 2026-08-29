package br.com.integrationhub.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseHealthServiceTest {

    @Test
    void deveInformarBancoOnlineQuandoConexaoForValida()
            throws SQLException {

        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        boolean online = new DatabaseHealthService(dataSource)
                .isDatabaseOnline();

        assertTrue(online);
        verify(connection).close();
    }

    @Test
    void deveInformarBancoOfflineQuandoNaoConseguirConectar()
            throws SQLException {

        DataSource dataSource = mock(DataSource.class);

        when(dataSource.getConnection())
                .thenThrow(new SQLException("Banco indisponível"));

        boolean online = new DatabaseHealthService(dataSource)
                .isDatabaseOnline();

        assertFalse(online);
    }
}
