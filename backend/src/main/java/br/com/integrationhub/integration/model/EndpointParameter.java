package br.com.integrationhub.integration.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EndpointParameter {

    @NotBlank(message = "nome do parâmetro é obrigatório")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "nome do parâmetro é inválido")
    private String name;

    @NotBlank(message = "tipo do parâmetro é obrigatório")
    @Pattern(regexp = "VARCHAR2|VARCHAR|CHAR|NUMBER|DATE|TIMESTAMP", flags = Pattern.Flag.CASE_INSENSITIVE, message = "tipo de parâmetro não suportado")
    private String type;
    private boolean required;

    public EndpointParameter() {
    }

    public EndpointParameter(String name, String type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
