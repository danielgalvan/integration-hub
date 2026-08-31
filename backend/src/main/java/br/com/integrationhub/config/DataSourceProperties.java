package br.com.integrationhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(
        prefix = "integration-hub.datasource"
)
public class DataSourceProperties {

    private Map<String, ConnectionProperties> connections =
            new LinkedHashMap<>();

    public Map<String, ConnectionProperties> getConnections() {
        return connections;
    }

    public void setConnections(
            Map<String, ConnectionProperties> connections) {

        this.connections = connections;
    }

    public ConnectionProperties getConnection(
            String environment) {

        if (environment == null ||
                environment.isBlank()) {

            return null;
        }

        return connections.get(environment);
    }

    public static class ConnectionProperties {

        private String name;
        private String url;
        private String username;
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
