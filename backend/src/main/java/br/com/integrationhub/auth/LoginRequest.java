package br.com.integrationhub.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Usuário é obrigatório")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        String password,

        @NotBlank(message = "Ambiente é obrigatório")
        String environment

) {
}
