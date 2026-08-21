package br.com.integrationhub.integration.model;

public class Integration {

    private Long id;
    private String name;
    private String description;
    private String basePath;
    private boolean active;

    public Integration() {
    }

    public Integration(
            Long id,
            String name,
            String description,
            String basePath,
            boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePath = basePath;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}