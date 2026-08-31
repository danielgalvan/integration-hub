package br.com.integrationhub.user;

import br.com.integrationhub.config.DataSourceProperties;
import br.com.integrationhub.user.bootstrap.AdminUserBootstrap;
import br.com.integrationhub.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserBootstrapTest {

    @Test
    void deveCriarAdministradorQuandoNaoExistirUsuario() throws Exception {
        UserService userService = mock(UserService.class);
        when(userService.count()).thenReturn(0L);

        AdminUserBootstrap bootstrap = createBootstrap(userService);

        bootstrap.run();

        verify(userService).create(
                "admin",
                "Administrador",
                "admin@example.com",
                "senha-bootstrap",
                "A",
                "A"
        );
    }

    @Test
    void naoDeveCriarAdministradorQuandoJaExistirUsuario()
            throws Exception {
        UserService userService = mock(UserService.class);
        when(userService.count()).thenReturn(1L);

        createBootstrap(userService).run();

        verify(userService, never()).create(
                "admin",
                "Administrador",
                "admin@example.com",
                "senha-bootstrap",
                "A",
                "A"
        );
    }

    private AdminUserBootstrap createBootstrap(
            UserService userService
    ) {
        DataSourceProperties dataSourceProperties =
                new DataSourceProperties();
        dataSourceProperties.setConnections(Map.of(
                "test",
                new DataSourceProperties.ConnectionProperties()
        ));

        return new AdminUserBootstrap(
                userService,
                dataSourceProperties,
                "admin",
                "Administrador",
                "admin@example.com",
                "senha-bootstrap"
        );
    }
}
