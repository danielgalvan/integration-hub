package br.com.integrationhub.integration.model;

import jakarta.validation.constraints.NotNull;

public record IntegrationCredentialActiveRequest(

        @NotNull(message = "active é obrigatório")
        Boolean active

) {
}
