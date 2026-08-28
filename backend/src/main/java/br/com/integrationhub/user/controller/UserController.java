package br.com.integrationhub.user.controller;

import br.com.integrationhub.user.dto.UserCreateRequest;
import br.com.integrationhub.user.dto.UserCreateResponse;
import br.com.integrationhub.user.dto.UserResetPasswordResponse;
import br.com.integrationhub.user.dto.UserResponse;
import br.com.integrationhub.user.dto.UserUpdateRequest;
import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {

        List<UserResponse> users =
                userService.findAll()
                        .stream()
                        .map(userService::toResponse)
                        .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(
            @PathVariable Long id
    ) {

        User user = userService.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuário não encontrado"
                        )
                );

        return ResponseEntity.ok(
                userService.toResponse(user)
        );
    }

    @PostMapping
    public ResponseEntity<UserCreateResponse> create(
            @Valid @RequestBody UserCreateRequest request
    ) {

        UserCreateResponse response =
                userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {

        return ResponseEntity.ok(
                userService.update(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        userService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<UserResetPasswordResponse> resetPassword(
            @PathVariable Long id
    ) {

        String temporaryPassword =
                userService.resetPassword(id);

        return ResponseEntity.ok(
                new UserResetPasswordResponse(
                        temporaryPassword
                )
        );
    }
}
