package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.model.IntegrationCredential;
import br.com.integrationhub.integration.repository.IntegrationCredentialRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import br.com.integrationhub.security.ApiKeyEncryptionService;
import br.com.integrationhub.security.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationCredentialServiceTest {

    private IntegrationCredentialRepository credentialRepository;
    private IntegrationRepository integrationRepository;
    private ApiKeyService apiKeyService;
    private ApiKeyEncryptionService apiKeyEncryptionService;
    private IntegrationCredentialService service;

    @BeforeEach
    void setUp() {
        credentialRepository =
                mock(IntegrationCredentialRepository.class);

        integrationRepository =
                mock(IntegrationRepository.class);

        apiKeyService =
                mock(ApiKeyService.class);

        apiKeyEncryptionService =
                mock(ApiKeyEncryptionService.class);

        service =
                new IntegrationCredentialService(
                        credentialRepository,
                        integrationRepository,
                        apiKeyService,
                        apiKeyEncryptionService
                );
    }

    @Test
    void shouldFindCredentialsByIntegrationId() {
        Integration integration =
                createIntegration(
                        1L,
                        "API_KEY"
                );

        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(integrationRepository.findById(
                1L))
                .thenReturn(
                        Optional.of(
                                integration));

        when(credentialRepository.findByIntegrationId(
                1L))
                .thenReturn(
                        List.of(
                                credential));

        List<IntegrationCredential> result =
                service.findByIntegrationId(
                        1L);

        assertEquals(
                1,
                result.size());

        assertEquals(
                10L,
                result.getFirst().getId());

        verify(credentialRepository)
                .findByIntegrationId(
                        1L);
    }

    @Test
    void shouldFindCredentialById() {
        Integration integration =
                createIntegration(
                        1L,
                        "API_KEY"
                );

        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(integrationRepository.findById(
                1L))
                .thenReturn(
                        Optional.of(
                                integration));

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        IntegrationCredential result =
                service.findById(
                        1L,
                        10L);

        assertEquals(
                10L,
                result.getId());

        assertEquals(
                "Sistema Tasy",
                result.getName());
    }

    @Test
    void shouldCreateCredential() {
        Integration integration =
                createIntegration(
                        1L,
                        "API_KEY"
                );

        IntegrationCredential persisted =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        persisted.setApiKeyEncrypted(
                "chave-criptografada");

        when(integrationRepository.findById(
                1L))
                .thenReturn(
                        Optional.of(
                                integration));

        when(apiKeyService.generateApiKey())
                .thenReturn(
                        "ihub-chave");

        when(apiKeyEncryptionService.encrypt(
                "ihub-chave"))
                .thenReturn(
                        "chave-criptografada");

        when(credentialRepository.save(
                any(IntegrationCredential.class)))
                .thenReturn(
                        persisted);

        IntegrationCredential result =
                service.create(
                        1L,
                        "Sistema Tasy",
                        "admin");

        assertEquals(
                10L,
                result.getId());

        assertEquals(
                "Sistema Tasy",
                result.getName());

        verify(apiKeyService)
                .generateApiKey();

        verify(apiKeyEncryptionService)
                .encrypt(
                        "ihub-chave");

        verify(credentialRepository)
                .save(
                        any(IntegrationCredential.class));
    }

    @Test
    void shouldNotCreateCredentialForNoneIntegration() {
        Integration integration =
                createIntegration(
                        1L,
                        "NONE"
                );

        when(integrationRepository.findById(
                1L))
                .thenReturn(
                        Optional.of(
                                integration));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.create(
                                1L,
                                "Sistema Tasy",
                                "admin")
                );

        assertEquals(
                "A integração não está configurada para utilizar API Key",
                exception.getMessage());

        verify(apiKeyService, never())
                .generateApiKey();

        verify(apiKeyEncryptionService, never())
                .encrypt(
                        any());

        verify(credentialRepository, never())
                .save(
                        any());
    }

    @Test
    void shouldGetApiKey() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        credential.setApiKeyEncrypted(
                "chave-criptografada");

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        when(apiKeyEncryptionService.decrypt(
                "chave-criptografada"))
                .thenReturn(
                        "ihub-chave");

        String result =
                service.getApiKey(
                        1L,
                        10L);

        assertEquals(
                "ihub-chave",
                result);

        verify(apiKeyEncryptionService)
                .decrypt(
                        "chave-criptografada");
    }

    @Test
    void shouldUpdateCredentialName() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Antigo",
                        "S"
                );

        IntegrationCredential updated =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        when(credentialRepository.updateName(
                10L,
                "Sistema Tasy",
                "admin"))
                .thenReturn(
                        updated);

        IntegrationCredential result =
                service.updateName(
                        1L,
                        10L,
                        "Sistema Tasy",
                        "admin");

        assertEquals(
                "Sistema Tasy",
                result.getName());

        verify(credentialRepository)
                .updateName(
                        10L,
                        "Sistema Tasy",
                        "admin");

        verify(credentialRepository, never())
                .updateApiKey(
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldDeactivateCredential() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        IntegrationCredential updated =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "N"
                );

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        when(credentialRepository.updateActive(
                10L,
                "N",
                "admin"))
                .thenReturn(
                        updated);

        IntegrationCredential result =
                service.setActive(
                        1L,
                        10L,
                        false,
                        "admin");

        assertEquals(
                "N",
                result.getActive());

        verify(credentialRepository)
                .updateActive(
                        10L,
                        "N",
                        "admin");

        verify(credentialRepository, never())
                .updateApiKey(
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldActivateCredential() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "N"
                );

        IntegrationCredential updated =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        when(credentialRepository.updateActive(
                10L,
                "S",
                "admin"))
                .thenReturn(
                        updated);

        IntegrationCredential result =
                service.setActive(
                        1L,
                        10L,
                        true,
                        "admin");

        assertEquals(
                "S",
                result.getActive());

        verify(credentialRepository)
                .updateActive(
                        10L,
                        "S",
                        "admin");
    }

    @Test
    void shouldRegenerateApiKey() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        when(apiKeyService.generateApiKey())
                .thenReturn(
                        "ihub-nova-chave");

        when(apiKeyEncryptionService.encrypt(
                "ihub-nova-chave"))
                .thenReturn(
                        "nova-chave-criptografada");

        String result =
                service.regenerateApiKey(
                        1L,
                        10L,
                        "admin");

        assertEquals(
                "ihub-nova-chave",
                result);

        verify(credentialRepository)
                .updateApiKey(
                        10L,
                        "nova-chave-criptografada",
                        "admin");

        verify(credentialRepository, never())
                .updateName(
                        any(),
                        any(),
                        any());

        verify(credentialRepository, never())
                .updateActive(
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldAuthenticateValidApiKey() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        credential.setApiKeyEncrypted(
                "chave-criptografada");

        when(credentialRepository.findActiveByIntegrationId(
                1L))
                .thenReturn(
                        List.of(
                                credential));

        when(apiKeyEncryptionService.decrypt(
                "chave-criptografada"))
                .thenReturn(
                        "ihub-chave-valida");

        Optional<IntegrationCredential> result =
                service.authenticate(
                        1L,
                        "ihub-chave-valida");

        assertTrue(
                result.isPresent());

        assertEquals(
                10L,
                result.get().getId());

        assertEquals(
                "Sistema Tasy",
                result.get().getName());

        verify(credentialRepository)
                .findActiveByIntegrationId(
                        1L);

        verify(apiKeyEncryptionService)
                .decrypt(
                        "chave-criptografada");
    }

    @Test
    void shouldRejectInvalidApiKey() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        credential.setApiKeyEncrypted(
                "chave-criptografada");

        when(credentialRepository.findActiveByIntegrationId(
                1L))
                .thenReturn(
                        List.of(
                                credential));

        when(apiKeyEncryptionService.decrypt(
                "chave-criptografada"))
                .thenReturn(
                        "ihub-chave-correta");

        Optional<IntegrationCredential> result =
                service.authenticate(
                        1L,
                        "ihub-chave-errada");

        assertTrue(
                result.isEmpty());

        verify(credentialRepository)
                .findActiveByIntegrationId(
                        1L);
    }

    @Test
    void shouldAuthenticateSecondCredentialWhenFirstDoesNotMatch() {
        IntegrationCredential first =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        first.setApiKeyEncrypted(
                "chave-1-criptografada");

        IntegrationCredential second =
                createCredential(
                        20L,
                        1L,
                        "Sistema Financeiro",
                        "S"
                );

        second.setApiKeyEncrypted(
                "chave-2-criptografada");

        when(credentialRepository.findActiveByIntegrationId(
                1L))
                .thenReturn(
                        List.of(
                                first,
                                second));

        when(apiKeyEncryptionService.decrypt(
                "chave-1-criptografada"))
                .thenReturn(
                        "ihub-chave-1");

        when(apiKeyEncryptionService.decrypt(
                "chave-2-criptografada"))
                .thenReturn(
                        "ihub-chave-2");

        Optional<IntegrationCredential> result =
                service.authenticate(
                        1L,
                        "ihub-chave-2");

        assertTrue(
                result.isPresent());

        assertEquals(
                20L,
                result.get().getId());

        assertEquals(
                "Sistema Financeiro",
                result.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenThereAreNoActiveCredentials() {
        when(credentialRepository.findActiveByIntegrationId(
                1L))
                .thenReturn(
                        List.of());

        Optional<IntegrationCredential> result =
                service.authenticate(
                        1L,
                        "ihub-chave");

        assertTrue(
                result.isEmpty());

        verify(credentialRepository)
                .findActiveByIntegrationId(
                        1L);

        verify(apiKeyEncryptionService, never())
                .decrypt(
                        any());
    }

    @Test
    void shouldReturnEmptyWhenApiKeyIsNull() {
        Optional<IntegrationCredential> result =
                service.authenticate(
                        1L,
                        null);

        assertTrue(
                result.isEmpty());

        verify(credentialRepository, never())
                .findActiveByIntegrationId(
                        any());
    }

    @Test
    void shouldReturnEmptyWhenApiKeyIsBlank() {
        Optional<IntegrationCredential> result =
                service.authenticate(
                        1L,
                        " ");

        assertTrue(
                result.isEmpty());

        verify(credentialRepository, never())
                .findActiveByIntegrationId(
                        any());
    }

    @Test
    void shouldRejectCredentialFromAnotherIntegrationWhenFindingById() {
        Integration integration =
                createIntegration(
                        1L,
                        "API_KEY"
                );

        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(integrationRepository.findById(
                1L))
                .thenReturn(
                        Optional.of(
                                integration));

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findById(
                                1L,
                                10L)
                );

        assertEquals(
                "Credencial não encontrada",
                exception.getMessage());
    }

    @Test
    void shouldRejectCredentialFromAnotherIntegrationWhenUpdatingName() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateName(
                        1L,
                        10L,
                        "Novo Nome",
                        "admin")
        );

        verify(credentialRepository, never())
                .updateName(
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldRejectCredentialFromAnotherIntegrationWhenChangingActive() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.setActive(
                        1L,
                        10L,
                        false,
                        "admin")
        );

        verify(credentialRepository, never())
                .updateActive(
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldRejectCredentialFromAnotherIntegrationWhenRegeneratingApiKey() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(
                10L))
                .thenReturn(
                        Optional.of(
                                credential));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.regenerateApiKey(
                        1L,
                        10L,
                        "admin")
        );

        verify(credentialRepository, never())
                .updateApiKey(
                        any(),
                        any(),
                        any());

        verify(apiKeyService, never())
                .generateApiKey();
    }

    @Test
    void shouldFailWhenIntegrationDoesNotExist() {
        when(integrationRepository.findById(
                999L))
                .thenReturn(
                        Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findByIntegrationId(
                                999L)
                );

        assertEquals(
                "Integração não encontrada",
                exception.getMessage());

        verify(credentialRepository, never())
                .findByIntegrationId(
                        any());
    }

    @Test
    void shouldFailWhenCredentialDoesNotExist() {
        Integration integration =
                createIntegration(
                        1L,
                        "API_KEY"
                );

        when(integrationRepository.findById(
                1L))
                .thenReturn(
                        Optional.of(
                                integration));

        when(credentialRepository.findById(
                999L))
                .thenReturn(
                        Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findById(
                                1L,
                                999L)
                );

        assertEquals(
                "Credencial não encontrada",
                exception.getMessage());
    }

    private Integration createIntegration(
            Long id,
            String authType) {

        Integration integration =
                new Integration();

        integration.setId(id);
        integration.setName(
                "Integração Teste");
        integration.setBasePath(
                "/api/teste");
        integration.setActive(
                "S");
        integration.setAuthType(
                authType);

        return integration;
    }

    private IntegrationCredential createCredential(
            Long id,
            Long integrationId,
            String name,
            String active) {

        IntegrationCredential credential =
                new IntegrationCredential();

        credential.setId(id);
        credential.setIntegrationId(
                integrationId);
        credential.setName(
                name);
        credential.setActive(
                active);

        return credential;
    }
}
