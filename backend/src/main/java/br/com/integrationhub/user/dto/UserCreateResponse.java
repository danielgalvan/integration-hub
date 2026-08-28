package br.com.integrationhub.user.dto;

public record UserCreateResponse(

        UserResponse user,
        String temporaryPassword

) {
}
