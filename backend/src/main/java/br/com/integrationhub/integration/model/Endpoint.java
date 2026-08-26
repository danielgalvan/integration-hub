package br.com.integrationhub.integration.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public class Endpoint {

    private Long id;
    @NotNull(message = "integrationId é obrigatório")
    private Long integrationId;

    @NotBlank(message = "name é obrigatório")
    private String name;
    private String description;
    @NotBlank(message = "path é obrigatório")
    @Pattern(regexp = "/.*", message = "path deve iniciar com /")
    private String path;

    @NotBlank(message = "method é obrigatório")
    @Pattern(regexp = "GET", flags = Pattern.Flag.CASE_INSENSITIVE, message = "a V1 suporta apenas o método GET")
    private String method;

    @NotBlank(message = "sqlText é obrigatório")
    private String sqlText;

    @Valid
    private List<EndpointParameter> parameters;

    @Pattern(regexp = "[SNsn]", message = "active deve ser S ou N")
    private String active;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public Endpoint() {
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

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }

    public List<EndpointParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<EndpointParameter> parameters) {
        this.parameters = parameters;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
