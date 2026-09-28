package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.model.IntegrationCredential;
import br.com.integrationhub.integration.repository.IntegrationCredentialRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import br.com.integrationhub.security.ApiKeyEncryptionService;
import br.com.integrationhub.security.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationCredentialServiceTest {

    @Mock
    private IntegrationCredentialRepository credentialRepository;

    @Mock
    private IntegrationRepository integrationRepository;

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private ApiKeyEncryptionService apiKeyEncryptionService;

    private IntegrationCredentialService service;

    @BeforeEach
    void setUp() {
        service = new IntegrationCredentialService(
                credentialRepository,
                integrationRepository,
                apiKeyService,
                apiKeyEncryptionService
        );
    }

    @Test
    void shouldFindCredentialsByIntegrationId() {
        Integration integration =
                createIntegration(1L, "API_KEY");

        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(integration));

        when(credentialRepository.findByIntegrationId(1L))
                .thenReturn(List.of(credential));

        List<IntegrationCredential> result =
                service.findByIntegrationId(1L);

        assertEquals(1, result.size());
        assertEquals(
                "Sistema Tasy",
                result.getFirst().getName()
        );

        verify(credentialRepository)
                .findByIntegrationId(1L);
    }

    @Test
    void shouldThrowWhenIntegrationDoesNotExistWhileListing() {
        when(integrationRepository.findById(1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findByIntegrationId(1L)
                );

        assertEquals(
                "Integração não encontrada",
                exception.getMessage()
        );

        verifyNoInteractions(credentialRepository);
    }

    @Test
    void shouldFindCredentialById() {
        Integration integration =
                createIntegration(1L, "API_KEY");

        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(integration));

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        IntegrationCredential result =
                service.findById(1L, 10L);

        assertEquals(10L, result.getId());
        assertEquals(
                "Sistema Tasy",
                result.getName()
        );
    }

    @Test
    void shouldThrowWhenCredentialDoesNotExist() {
        Integration integration =
                createIntegration(1L, "API_KEY");

        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(integration));

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findById(1L, 10L)
                );

        assertEquals(
                "Credencial não encontrada",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenCredentialBelongsToAnotherIntegration() {
        Integration integration =
                createIntegration(1L, "API_KEY");

        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(integration));

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findById(1L, 10L)
                );

        assertEquals(
                "Credencial não encontrada",
                exception.getMessage()
        );
    }

    @Test
    void shouldCreateCredentialForApiKeyIntegration() {
        Integration integration =
                createIntegration(1L, "API_KEY");

        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(integration));

        when(apiKeyService.generateApiKey())
                .thenReturn("ihub_chave");

        when(apiKeyEncryptionService.encrypt("ihub_chave"))
                .thenReturn("chave-criptografada");

        when(credentialRepository.save(any()))
                .thenAnswer(invocation -> {
                    IntegrationCredential credential =
                            invocation.getArgument(0);

                    credential.setId(10L);

                    return credential;
                });

        IntegrationCredential result =
                service.create(
                        1L,
                        "Sistema Tasy",
                        "admin"
                );

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getIntegrationId());
        assertEquals(
                "Sistema Tasy",
                result.getName()
        );
        assertEquals(
                "chave-criptografada",
                result.getApiKeyEncrypted()
        );
        assertEquals("S", result.getActive());
        assertEquals(
                "admin",
                result.getCreatedBy()
        );

        verify(apiKeyService)
                .generateApiKey();

        verify(apiKeyEncryptionService)
                .encrypt("ihub_chave");

        verify(credentialRepository)
                .save(any(IntegrationCredential.class));
    }

    @Test
    void shouldNotCreateCredentialForNoneIntegration() {
        Integration integration =
                createIntegration(1L, "NONE");

        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(integration));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.create(
                                1L,
                                "Sistema Tasy",
                                "admin"
                        )
                );

        assertEquals(
                "A integração não está configurada para utilizar API Key",
                exception.getMessage()
        );

        verify(apiKeyService, never())
                .generateApiKey();

        verify(apiKeyEncryptionService, never())
                .encrypt(anyString());

        verify(credentialRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenIntegrationDoesNotExistWhileCreating() {
        when(integrationRepository.findById(1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                1L,
                                "Sistema Tasy",
                                "admin"
                        )
                );

        assertEquals(
                "Integração não encontrada",
                exception.getMessage()
        );

        verify(apiKeyService, never())
                .generateApiKey();

        verify(credentialRepository, never())
                .save(any());
    }

    @Test
    void shouldGetDecryptedApiKey() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        1L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(apiKeyEncryptionService.decrypt(
                "chave-criptografada"))
                .thenReturn("ihub_chave");

        String result =
                service.getApiKey(1L, 10L);

        assertEquals(
                "ihub_chave",
                result
        );

        verify(apiKeyEncryptionService)
                .decrypt("chave-criptografada");
    }

    @Test
    void shouldUpdateCredentialName() {
        IntegrationCredential current =
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

        updated.setUpdatedBy("admin");

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(current));

        when(credentialRepository.updateName(
                10L,
                "Sistema Tasy",
                "admin"))
                .thenReturn(updated);

        IntegrationCredential result =
                service.updateName(
                        1L,
                        10L,
                        "Sistema Tasy",
                        "admin"
                );

        assertEquals(
                "Sistema Tasy",
                result.getName()
        );

        assertEquals(
                "admin",
                result.getUpdatedBy()
        );

        verify(credentialRepository)
                .updateName(
                        10L,
                        "Sistema Tasy",
                        "admin"
                );

        verify(credentialRepository, never())
                .updateApiKey(
                        anyLong(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldDeactivateCredential() {
        IntegrationCredential current =
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

        updated.setUpdatedBy("admin");

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(current));

        when(credentialRepository.updateActive(
                10L,
                "N",
                "admin"))
                .thenReturn(updated);

        IntegrationCredential result =
                service.setActive(
                        1L,
                        10L,
                        false,
                        "admin"
                );

        assertEquals(
                "N",
                result.getActive()
        );

        assertEquals(
                "admin",
                result.getUpdatedBy()
        );

        verify(credentialRepository)
                .updateActive(
                        10L,
                        "N",
                        "admin"
                );

        verify(credentialRepository, never())
                .updateApiKey(
                        anyLong(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldActivateCredential() {
        IntegrationCredential current =
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

        updated.setUpdatedBy("admin");

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(current));

        when(credentialRepository.updateActive(
                10L,
                "S",
                "admin"))
                .thenReturn(updated);

        IntegrationCredential result =
                service.setActive(
                        1L,
                        10L,
                        true,
                        "admin"
                );

        assertEquals(
                "S",
                result.getActive()
        );

        verify(credentialRepository)
                .updateActive(
                        10L,
                        "S",
                        "admin"
                );
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

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(apiKeyService.generateApiKey())
                .thenReturn("ihub_nova_chave");

        when(apiKeyEncryptionService.encrypt(
                "ihub_nova_chave"))
                .thenReturn("nova-chave-criptografada");

        String result =
                service.regenerateApiKey(
                        1L,
                        10L,
                        "admin"
                );

        assertEquals(
                "ihub_nova_chave",
                result
        );

        verify(credentialRepository)
                .updateApiKey(
                        10L,
                        "nova-chave-criptografada",
                        "admin"
                );

        verify(credentialRepository, never())
                .updateName(
                        anyLong(),
                        anyString(),
                        anyString()
                );

        verify(credentialRepository, never())
                .updateActive(
                        anyLong(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldRejectGetApiKeyFromAnotherIntegration() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getApiKey(
                        1L,
                        10L)
        );

        verifyNoInteractions(
                apiKeyEncryptionService
        );
    }

    @Test
    void shouldRejectUpdateNameFromAnotherIntegration() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

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
                        anyLong(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldRejectSetActiveFromAnotherIntegration() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

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
                        anyLong(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldRejectRegenerateApiKeyFromAnotherIntegration() {
        IntegrationCredential credential =
                createCredential(
                        10L,
                        2L,
                        "Sistema Tasy",
                        "S"
                );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.regenerateApiKey(
                        1L,
                        10L,
                        "admin")
        );

        verify(apiKeyService, never())
                .generateApiKey();

        verify(apiKeyEncryptionService, never())
                .encrypt(anyString());

        verify(credentialRepository, never())
                .updateApiKey(
                        anyLong(),
                        anyString(),
                        anyString()
                );
    }

    private Integration createIntegration(
            Long id,
            String authType) {

        Integration integration =
                new Integration();

        integration.setId(id);
        integration.setAuthType(authType);

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
        credential.setName(name);
        credential.setActive(active);
        credential.setApiKeyEncrypted(
                "chave-criptografada"
        );

        return credential;
    }
}
