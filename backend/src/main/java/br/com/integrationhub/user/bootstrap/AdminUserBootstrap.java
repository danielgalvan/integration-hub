package br.com.integrationhub.user.bootstrap;

import br.com.integrationhub.config.DataSourceProperties;
import br.com.integrationhub.config.EnvironmentContext;
import br.com.integrationhub.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "integration-hub.bootstrap.admin.enabled",
        havingValue = "true"
)
public class AdminUserBootstrap implements CommandLineRunner {

    private final UserService userService;
    private final DataSourceProperties dataSourceProperties;

    private final String username;
    private final String name;
    private final String email;
    private final String password;

    public AdminUserBootstrap(
            UserService userService,
            DataSourceProperties dataSourceProperties,

            @Value("${integration-hub.bootstrap.admin.username}")
            String username,

            @Value("${integration-hub.bootstrap.admin.name}")
            String name,

            @Value("${integration-hub.bootstrap.admin.email:}")
            String email,

            @Value("${integration-hub.bootstrap.admin.password}")
            String password
    ) {
        this.userService = userService;
        this.dataSourceProperties = dataSourceProperties;

        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {

        dataSourceProperties
                .getConnections()
                .keySet()
                .forEach(this::bootstrapEnvironment);
    }

    private void bootstrapEnvironment(
            String environment) {

        try {
            EnvironmentContext.set(environment);

            if (userService.count() > 0) {
                return;
            }

            userService.create(
                    username,
                    name,
                    email,
                    password,
                    "A",
                    "A"
            );

        } finally {
            EnvironmentContext.clear();
        }
    }
}
