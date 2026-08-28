package br.com.integrationhub.user.model;

import java.time.LocalDateTime;

public record User(

        Long id,
        String username,
        String name,
        String email,
        String password,
        String status,
        String type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
