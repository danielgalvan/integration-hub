package br.com.integrationhub.auth;

import br.com.integrationhub.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final String adminUsername;
    private final String adminPassword;
    private final long expirationMinutes;

    public AuthService(
            JwtService jwtService,
            @Value("${integration-hub.security.admin.username}")
            String adminUsername,
            @Value("${integration-hub.security.admin.password}")
            String adminPassword,
            @Value("${integration-hub.security.jwt.expiration-minutes}")
            long expirationMinutes) {

        this.jwtService = jwtService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.expirationMinutes = expirationMinutes;
    }

    public LoginResponse login(LoginRequest request) {

        boolean validUsername =
                adminUsername.equals(request.username());

        boolean validPassword =
                adminPassword.equals(request.password());

        if (!validUsername || !validPassword) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário ou senha inválidos"
            );
        }

        String token =
                jwtService.generateToken(request.username());

        return new LoginResponse(
                token,
                "Bearer",
                expirationMinutes * 60
        );
    }
}