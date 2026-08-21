package br.com.integrationhub.integration.model;

import java.util.List;

public class Endpoint {

    private Long id;
    private Long integrationId;
    private String name;
    private String description;
    private String path;
    private String method;
    private String sql;
    private List<String> parameters;
    private boolean active;

    public Endpoint() {
    }

    public Endpoint(
            Long id,
            Long integrationId,
            String name,
            String description,
            String path,
            String method,
            String sql,
            List<String> parameters,
            boolean active) {
        this.id = id;
        this.integrationId = integrationId;
        this.name = name;
        this.description = description;
        this.path = path;
        this.method = method;
        this.sql = sql;
        this.parameters = parameters;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(Long integrationId) {
        this.integrationId = integrationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}