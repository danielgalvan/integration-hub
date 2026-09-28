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
        credentialRepository = mock(IntegrationCredentialRepository.class);
        integrationRepository = mock(IntegrationRepository.class);
        apiKeyService = mock(ApiKeyService.class);
        apiKeyEncryptionService = mock(ApiKeyEncryptionService.class);

        service = new IntegrationCredentialService(
                credentialRepository,
                integrationRepository,
                apiKeyService,
                apiKeyEncryptionService
        );
    }

    @Test
    void deveListarCredenciaisDaIntegracao() {
        Integration integration = createIntegration(1L);

        IntegrationCredential credential = createCredential(
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
        assertEquals(10L, result.getFirst().getId());
        assertEquals("Sistema Tasy", result.getFirst().getName());

        verify(credentialRepository)
                .findByIntegrationId(1L);
    }

    @Test
    void deveFalharAoListarCredenciaisDeIntegracaoInexistente() {
        when(integrationRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.findByIntegrationId(999L)
        );

        assertEquals(
                "Integração não encontrada",
                exception.getMessage()
        );

        verify(
                credentialRepository,
                never()
        ).findByIntegrationId(any());
    }

    @Test
    void deveBuscarCredencialPorId() {
        Integration integration = createIntegration(1L);

        IntegrationCredential credential = createCredential(
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
        assertEquals(1L, result.getIntegrationId());
        assertEquals("Sistema Tasy", result.getName());
    }

    @Test
    void deveFalharAoBuscarCredencialInexistente() {
        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(createIntegration(1L)));

        when(credentialRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.findById(1L, 999L)
        );

        assertEquals(
                "Credencial não encontrada",
                exception.getMessage()
        );
    }

    @Test
    void naoDevePermitirCredencialDeOutraIntegracao() {
        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(createIntegration(1L)));

        IntegrationCredential credential = createCredential(
                10L,
                2L,
                "Sistema Tasy",
                "S"
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.findById(1L, 10L)
        );

        assertEquals(
                "Credencial não encontrada",
                exception.getMessage()
        );
    }

    @Test
    void deveCriarCredencialAtivaComApiKeyCriptografada() {
        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(createIntegration(1L)));

        when(apiKeyService.generateApiKey())
                .thenReturn("ihub_chave-gerada");

        when(apiKeyEncryptionService.encrypt(
                "ihub_chave-gerada"
        )).thenReturn("chave-criptografada");

        when(credentialRepository.save(any(
                IntegrationCredential.class
        ))).thenAnswer(invocation -> {
            IntegrationCredential credential =
                    invocation.getArgument(0);

            credential.setId(10L);

            return credential;
        });

        IntegrationCredential result = service.create(
                1L,
                "Sistema Tasy",
                "admin"
        );

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getIntegrationId());
        assertEquals("Sistema Tasy", result.getName());
        assertEquals("S", result.getActive());
        assertEquals(
                "chave-criptografada",
                result.getApiKeyEncrypted()
        );
        assertEquals("admin", result.getCreatedBy());

        verify(apiKeyService).generateApiKey();

        verify(apiKeyEncryptionService).encrypt(
                "ihub_chave-gerada"
        );

        verify(credentialRepository).save(
                any(IntegrationCredential.class)
        );
    }

    @Test
    void naoDeveCriarCredencialParaIntegracaoInexistente() {
        when(integrationRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        999L,
                        "Sistema Tasy",
                        "admin"
                )
        );

        assertEquals(
                "Integração não encontrada",
                exception.getMessage()
        );

        verify(
                apiKeyService,
                never()
        ).generateApiKey();

        verify(
                credentialRepository,
                never()
        ).save(any());
    }

    @Test
    void deveConsultarApiKeyDaCredencial() {
        IntegrationCredential credential = createCredential(
                10L,
                1L,
                "Sistema Tasy",
                "S"
        );

        credential.setApiKeyEncrypted(
                "chave-criptografada"
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(apiKeyEncryptionService.decrypt(
                "chave-criptografada"
        )).thenReturn("ihub_chave-original");

        String result =
                service.getApiKey(1L, 10L);

        assertEquals(
                "ihub_chave-original",
                result
        );

        verify(apiKeyEncryptionService).decrypt(
                "chave-criptografada"
        );
    }

    @Test
    void deveAtualizarNomeDaCredencial() {
        IntegrationCredential credential = createCredential(
                10L,
                1L,
                "Sistema Tasy",
                "S"
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(credentialRepository.update(
                10L,
                credential
        )).thenReturn(credential);

        IntegrationCredential result =
                service.updateName(
                        1L,
                        10L,
                        "Sistema Financeiro",
                        "admin"
                );

        assertEquals(
                "Sistema Financeiro",
                result.getName()
        );

        assertEquals(
                "admin",
                result.getUpdatedBy()
        );

        verify(credentialRepository).update(
                10L,
                credential
        );
    }

    @Test
    void deveDesativarCredencial() {
        IntegrationCredential credential = createCredential(
                10L,
                1L,
                "Sistema Tasy",
                "S"
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(credentialRepository.update(
                10L,
                credential
        )).thenReturn(credential);

        IntegrationCredential result =
                service.setActive(
                        1L,
                        10L,
                        false,
                        "admin"
                );

        assertEquals("N", result.getActive());
        assertEquals("admin", result.getUpdatedBy());

        verify(credentialRepository).update(
                10L,
                credential
        );
    }

    @Test
    void deveAtivarCredencial() {
        IntegrationCredential credential = createCredential(
                10L,
                1L,
                "Sistema Tasy",
                "N"
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(credentialRepository.update(
                10L,
                credential
        )).thenReturn(credential);

        IntegrationCredential result =
                service.setActive(
                        1L,
                        10L,
                        true,
                        "admin"
                );

        assertEquals("S", result.getActive());
        assertEquals("admin", result.getUpdatedBy());
    }

    @Test
    void deveRegenerarApiKey() {
        IntegrationCredential credential = createCredential(
                10L,
                1L,
                "Sistema Tasy",
                "S"
        );

        credential.setApiKeyEncrypted(
                "chave-antiga-criptografada"
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(apiKeyService.generateApiKey())
                .thenReturn("ihub_nova-chave");

        when(apiKeyEncryptionService.encrypt(
                "ihub_nova-chave"
        )).thenReturn("nova-chave-criptografada");

        when(credentialRepository.update(
                10L,
                credential
        )).thenReturn(credential);

        String result =
                service.regenerateApiKey(
                        1L,
                        10L,
                        "admin"
                );

        assertEquals(
                "ihub_nova-chave",
                result
        );

        assertEquals(
                "nova-chave-criptografada",
                credential.getApiKeyEncrypted()
        );

        assertEquals(
                "admin",
                credential.getUpdatedBy()
        );

        verify(apiKeyService).generateApiKey();

        verify(apiKeyEncryptionService).encrypt(
                "ihub_nova-chave"
        );

        verify(credentialRepository).update(
                10L,
                credential
        );
    }

    @Test
    void naoDeveConsultarApiKeyDeCredencialDeOutraIntegracao() {
        IntegrationCredential credential = createCredential(
                10L,
                2L,
                "Sistema Tasy",
                "S"
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getApiKey(1L, 10L)
        );

        assertEquals(
                "Credencial não encontrada",
                exception.getMessage()
        );

        verify(
                apiKeyEncryptionService,
                never()
        ).decrypt(any());
    }

    private Integration createIntegration(Long id) {
        Integration integration = new Integration();

        integration.setId(id);
        integration.setName("Integração " + id);
        integration.setBasePath("/api/integracao-" + id);
        integration.setActive("S");

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
        credential.setIntegrationId(integrationId);
        credential.setName(name);
        credential.setActive(active);
        credential.setApiKeyEncrypted(
                "chave-criptografada"
        );

        return credential;
    }
}
