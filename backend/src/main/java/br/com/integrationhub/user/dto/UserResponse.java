package br.com.integrationhub.user.dto;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,
        String username,
        String name,
        String email,
        String status,
        String type,
        boolean passwordChangeRequired,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
