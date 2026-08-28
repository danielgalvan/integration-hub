package br.com.integrationhub.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(
                min = 6,
                message = "A nova senha deve possuir no mínimo 6 caracteres"
        )
        String newPassword

) {
}
