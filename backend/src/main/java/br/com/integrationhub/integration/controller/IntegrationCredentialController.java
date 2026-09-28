package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.IntegrationCredential;
import br.com.integrationhub.integration.model.IntegrationCredentialActiveRequest;
import br.com.integrationhub.integration.model.IntegrationCredentialApiKeyResponse;
import br.com.integrationhub.integration.model.IntegrationCredentialRequest;
import br.com.integrationhub.integration.model.IntegrationCredentialResponse;
import br.com.integrationhub.integration.service.IntegrationCredentialService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integrations/{integrationId}/credentials")
public class IntegrationCredentialController {

    private final IntegrationCredentialService credentialService;

    public IntegrationCredentialController(
            IntegrationCredentialService credentialService) {

        this.credentialService = credentialService;
    }

    @GetMapping
    public List<IntegrationCredentialResponse> findAll(
            @PathVariable("integrationId") Long integrationId) {

        return credentialService
                .findByIntegrationId(integrationId)
                .stream()
                .map(IntegrationCredentialResponse::from)
                .toList();
    }

    @GetMapping("/{credentialId}")
    public ResponseEntity<IntegrationCredentialResponse> findById(
            @PathVariable("integrationId") Long integrationId,
            @PathVariable("credentialId") Long credentialId) {

        IntegrationCredential credential =
                credentialService.findById(
                        integrationId,
                        credentialId
                );

        return ResponseEntity.ok(
                IntegrationCredentialResponse.from(
                        credential
                )
        );
    }

    @PostMapping
    public ResponseEntity<IntegrationCredentialApiKeyResponse> create(
            @PathVariable("integrationId") Long integrationId,
            @Valid @RequestBody IntegrationCredentialRequest request,
            Authentication authentication) {

        IntegrationCredential credential =
                credentialService.create(
                        integrationId,
                        request.name(),
                        authentication.getName()
                );

        String apiKey =
                credentialService.getApiKey(
                        integrationId,
                        credential.getId()
                );

        return ResponseEntity.ok(
                new IntegrationCredentialApiKeyResponse(
                        apiKey
                )
        );
    }

    @PutMapping("/{credentialId}")
    public ResponseEntity<IntegrationCredentialResponse> updateName(
            @PathVariable("integrationId") Long integrationId,
            @PathVariable("credentialId") Long credentialId,
            @Valid @RequestBody IntegrationCredentialRequest request,
            Authentication authentication) {

        IntegrationCredential credential =
                credentialService.updateName(
                        integrationId,
                        credentialId,
                        request.name(),
                        authentication.getName()
                );

        return ResponseEntity.ok(
                IntegrationCredentialResponse.from(
                        credential
                )
        );
    }

    @PutMapping("/{credentialId}/active")
    public ResponseEntity<IntegrationCredentialResponse> setActive(
            @PathVariable("integrationId") Long integrationId,
            @PathVariable("credentialId") Long credentialId,
            @Valid @RequestBody IntegrationCredentialActiveRequest request,
            Authentication authentication) {

        IntegrationCredential credential =
                credentialService.setActive(
                        integrationId,
                        credentialId,
                        request.active(),
                        authentication.getName()
                );

        return ResponseEntity.ok(
                IntegrationCredentialResponse.from(
                        credential
                )
        );
    }

    @GetMapping("/{credentialId}/api-key")
    public ResponseEntity<IntegrationCredentialApiKeyResponse> getApiKey(
            @PathVariable("integrationId") Long integrationId,
            @PathVariable("credentialId") Long credentialId) {

        String apiKey =
                credentialService.getApiKey(
                        integrationId,
                        credentialId
                );

        return ResponseEntity.ok(
                new IntegrationCredentialApiKeyResponse(
                        apiKey
                )
        );
    }

    @PostMapping("/{credentialId}/api-key")
    public ResponseEntity<IntegrationCredentialApiKeyResponse> regenerateApiKey(
            @PathVariable("integrationId") Long integrationId,
            @PathVariable("credentialId") Long credentialId,
            Authentication authentication) {

        String apiKey =
                credentialService.regenerateApiKey(
                        integrationId,
                        credentialId,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new IntegrationCredentialApiKeyResponse(
                        apiKey
                )
        );
    }
}
