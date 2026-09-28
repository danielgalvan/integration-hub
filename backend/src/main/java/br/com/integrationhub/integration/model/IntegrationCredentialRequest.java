package br.com.integrationhub.integration.model;

import jakarta.validation.constraints.NotBlank;

public record IntegrationCredentialRequest(

        @NotBlank(message = "name é obrigatório")
        String name

) {
}
