package br.com.integrationhub.auth;

public record AuthenticatedUserResponse(
        Long id,
        String username,
        String name,
        String email,
        String role
) {
}
