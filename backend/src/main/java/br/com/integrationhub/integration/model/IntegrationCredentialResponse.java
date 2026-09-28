package br.com.integrationhub.integration.model;

import java.time.LocalDateTime;

public record IntegrationCredentialResponse(
        Long id,
        Long integrationId,
        String name,
        String active,
        LocalDateTime lastUsedAt,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt
) {

    public static IntegrationCredentialResponse from(
            IntegrationCredential credential) {

        return new IntegrationCredentialResponse(
                credential.getId(),
                credential.getIntegrationId(),
                credential.getName(),
                credential.getActive(),
                credential.getLastUsedAt(),
                credential.getCreatedBy(),
                credential.getCreatedAt(),
                credential.getUpdatedBy(),
                credential.getUpdatedAt()
        );
    }
}
