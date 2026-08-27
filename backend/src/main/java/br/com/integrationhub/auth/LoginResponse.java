package br.com.integrationhub.auth;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}