package br.com.integrationhub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank(message = "Usuário é obrigatório")
        @Size(max = 100, message = "Usuário deve possuir no máximo 100 caracteres")
        String username,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 200, message = "Nome deve possuir no máximo 200 caracteres")
        String name,

        @Email(message = "E-mail inválido")
        @Size(max = 200, message = "E-mail deve possuir no máximo 200 caracteres")
        String email,

        @NotBlank(message = "Tipo de usuário é obrigatório")
        @Pattern(
                regexp = "[ACU]",
                message = "Tipo de usuário deve ser A, C ou U"
        )
        String type

) {
}
