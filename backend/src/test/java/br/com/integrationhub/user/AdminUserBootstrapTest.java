package br.com.integrationhub.user;

import br.com.integrationhub.config.DataSourceProperties;
import br.com.integrationhub.config.EnvironmentContext;
import br.com.integrationhub.user.bootstrap.AdminUserBootstrap;
import br.com.integrationhub.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

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

    @Test
    void deveExecutarBootstrapParaCadaAmbienteConfigurado()
            throws Exception {
        UserService userService = mock(UserService.class);
        when(userService.count()).thenReturn(0L);
        DataSourceProperties dataSourceProperties =
                new DataSourceProperties();
        dataSourceProperties.setConnections(Map.of(
                "development",
                new DataSourceProperties.ConnectionProperties(),
                "cloud",
                new DataSourceProperties.ConnectionProperties()
        ));

        new AdminUserBootstrap(
                userService,
                dataSourceProperties,
                "admin",
                "Administrador",
                "admin@example.com",
                "senha-bootstrap"
        ).run();

        verify(userService, times(2)).count();
        verify(userService, times(2)).create(
                "admin",
                "Administrador",
                "admin@example.com",
                "senha-bootstrap",
                "A",
                "A"
        );
        assertNull(EnvironmentContext.get());
    }

    @Test
    void deveLimparAmbienteQuandoBootstrapFalha() {
        UserService userService = mock(UserService.class);
        when(userService.count()).thenThrow(
                new IllegalStateException("Falha no banco")
        );

        assertThrows(
                IllegalStateException.class,
                () -> createBootstrap(userService).run()
        );

        assertNull(EnvironmentContext.get());
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
