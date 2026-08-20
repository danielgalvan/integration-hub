package br.com.integrationhub.integration.model;

import java.util.List;

public class IntegrationEndpoint {

    private String name;
    private String path;
    private String method;
    private String sql;
    private List<String> parameters;

    public IntegrationEndpoint() {
    }

    public IntegrationEndpoint(
            String name,
            String path,
            String method,
            String sql,
            List<String> parameters) {
        this.name = name;
        this.path = path;
        this.method = method;
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}