package br.com.integrationhub.auth;

import br.com.integrationhub.security.JwtService;
import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final long expirationMinutes;

    public AuthService(
            JwtService jwtService,
            UserService userService,
            @Value("${integration-hub.security.jwt.expiration-minutes}")
            long expirationMinutes
    ) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.expirationMinutes = expirationMinutes;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userService.findByUsername(
                        request.username()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Usuário ou senha inválidos"
                        )
                );

        if (!"A".equals(user.status())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário ou senha inválidos"
            );
        }

        boolean validPassword =
                userService.passwordMatches(
                        request.password(),
                        user
                );

        if (!validPassword) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário ou senha inválidos"
            );
        }

        String token = jwtService.generateToken(
                user.username(),
                request.environment(),
                user.type()
        );

        return new LoginResponse(
                token,
                "Bearer",
                expirationMinutes * 60,
                "S".equals(user.passwordChangeRequired())
        );
    }

    public void changePassword(
            String username,
            String newPassword
    ) {

        User user = userService.findByUsername(username)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuário não encontrado"
                        )
                );

        userService.changePassword(
                user.id(),
                newPassword
        );
    }
}
