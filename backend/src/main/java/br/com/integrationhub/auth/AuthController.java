package br.com.integrationhub.auth;

import br.com.integrationhub.config.DataSourceProperties;
import br.com.integrationhub.config.EnvironmentContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final DataSourceProperties dataSourceProperties;

    public AuthController(
            AuthService authService,
            DataSourceProperties dataSourceProperties) {

        this.authService = authService;
        this.dataSourceProperties = dataSourceProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        validateEnvironment(
                request.environment()
        );

        try {
            EnvironmentContext.set(
                    request.environment()
            );

            return ResponseEntity.ok(
                    authService.login(request)
            );

        } finally {
            EnvironmentContext.clear();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(
            Authentication authentication) {

        return ResponseEntity.ok(
                authService.getAuthenticatedUser(
                        authentication.getName()
                )
        );
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody
            ChangePasswordRequest request) {

        authService.changePassword(
                authentication.getName(),
                request.newPassword()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    private void validateEnvironment(
            String environment) {

        if (dataSourceProperties
                .getConnection(environment) == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ambiente inválido"
            );
        }
    }
}
