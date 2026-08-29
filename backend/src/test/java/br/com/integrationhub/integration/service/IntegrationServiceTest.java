package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.ApiKeyResponse;
import br.com.integrationhub.security.ApiKeyService;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.repository.EndpointRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationServiceTest {

    private IntegrationRepository integrationRepository;
    private EndpointRepository endpointRepository;
    private ApiKeyService apiKeyService;
    private IntegrationService integrationService;

    @BeforeEach
    void setUp() {
        integrationRepository = mock(IntegrationRepository.class);

        endpointRepository = mock(EndpointRepository.class);

        apiKeyService = mock(ApiKeyService.class);

        integrationService = new IntegrationService(
                integrationRepository,
                endpointRepository,
                apiKeyService);
    }

    @Test
    void deveSalvarIntegrationComBasePathValido() {

        Integration integration = createIntegration(
                "/api/pedidos");

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration);

        assertEquals(
                "/api/pedidos",
                result.getBasePath());

        verify(integrationRepository).save(
                integration);
    }

    @Test
    void deveDefinirActiveComoSQuandoNaoInformado() {

        Integration integration = createIntegration(
                "/api/pedidos");

        integration.setActive(null);

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration);

        assertEquals(
                "S",
                result.getActive());
    }

    @Test
    void deveDefinirCreatedByComoSystemQuandoNaoInformado() {

        Integration integration = createIntegration(
                "/api/pedidos");

        integration.setCreatedBy(null);

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration);

        assertEquals(
                "SYSTEM",
                result.getCreatedBy());
    }

    @Test
    void deveRejeitarBasePathNulo() {

        Integration integration = createIntegration(
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(
                        integration));

        assertEquals(
                "basePath é obrigatório",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).save(any());
    }

    @Test
    void deveRejeitarBasePathVazio() {

        Integration integration = createIntegration(
                "   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(
                        integration));

        assertEquals(
                "basePath é obrigatório",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).save(any());
    }

    @Test
    void deveRejeitarBasePathSemApi() {

        Integration integration = createIntegration(
                "/pedidos");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(
                        integration));

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).save(any());
    }

    @Test
    void deveRejeitarBasePathSemBarraInicial() {

        Integration integration = createIntegration(
                "api/pedidos");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(
                        integration));

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).save(any());
    }

    @Test
    void deveRejeitarBasePathTerminandoComBarra() {

        Integration integration = createIntegration(
                "/api/pedidos/");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(
                        integration));

        assertEquals(
                "basePath não deve terminar com /",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).save(any());
    }

    @Test
    void deveRejeitarBasePathComEspaco() {

        Integration integration = createIntegration(
                "/api/meus pedidos");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(
                        integration));

        assertEquals(
                "basePath não deve conter espaços",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).save(any());
    }

    @Test
    void deveAceitarBasePathComMaisDeUmNivel() {

        Integration integration = createIntegration(
                "/api/pedidos/especiais");

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration);

        assertEquals(
                "/api/pedidos/especiais",
                result.getBasePath());

        verify(integrationRepository).save(
                integration);
    }

    @Test
    void deveAtualizarIntegrationExistente() {

        Integration current = createIntegration(
                "/api/pedidos");

        Integration updated = createIntegration(
                "/api/pedidos");

        updated.setName("Pedidos Atualizados");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(
                12L,
                updated)).thenReturn(updated);

        Integration result = integrationService.update(
                12L,
                updated);

        assertEquals(
                "Pedidos Atualizados",
                result.getName());

        verify(integrationRepository).update(
                12L,
                updated);
    }

    @Test
    void deveDefinirUpdatedByComoSystemNoUpdate() {

        Integration current = createIntegration(
                "/api/pedidos");

        Integration updated = createIntegration(
                "/api/pedidos");

        updated.setUpdatedBy(null);

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(
                12L,
                updated)).thenReturn(updated);

        Integration result = integrationService.update(
                12L,
                updated);

        assertEquals(
                "SYSTEM",
                result.getUpdatedBy());
    }

    @Test
    void deveManterActiveAtualQuandoNaoInformadoNoUpdate() {

        Integration current = createIntegration(
                "/api/pedidos");

        current.setActive("N");

        Integration updated = createIntegration(
                "/api/pedidos");

        updated.setActive(null);

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(
                12L,
                updated)).thenReturn(updated);

        Integration result = integrationService.update(
                12L,
                updated);

        assertEquals(
                "N",
                result.getActive());
    }

    @Test
    void deveRejeitarUpdateQuandoIntegrationNaoExiste() {

        Integration integration = createIntegration(
                "/api/pedidos");

        when(integrationRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.update(
                        99L,
                        integration));

        assertEquals(
                "Integração não encontrada: 99",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).update(any(), any());
    }

    @Test
    void deveRejeitarBasePathInvalidoNoUpdate() {

        Integration current = createIntegration(
                "/api/pedidos");

        Integration integration = createIntegration(
                "/pedidos");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.update(
                        12L,
                        integration));

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).update(any(), any());
    }

    @Test
    void deveExcluirIntegrationSemEndpoints() {

        Integration integration = createIntegration(
                "/api/pedidos");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        when(endpointRepository.findByIntegrationId(12L))
                .thenReturn(List.of());

        integrationService.delete(12L);

        verify(integrationRepository).deleteById(
                12L);
    }

    @Test
    void deveRejeitarDeleteQuandoIntegrationNaoExiste() {

        when(integrationRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.delete(
                        99L));

        assertEquals(
                "Integração não encontrada: 99",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).deleteById(any());
    }

    @Test
    void deveRejeitarDeleteQuandoPossuiEndpoints() {

        Integration integration = createIntegration(
                "/api/pedidos");

        Endpoint endpoint = mock(Endpoint.class);

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        when(endpointRepository.findByIntegrationId(12L))
                .thenReturn(List.of(endpoint));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> integrationService.delete(
                        12L));

        assertEquals(
                "A integração possui endpoints vinculados",
                exception.getMessage());

        verify(
                integrationRepository,
                never()).deleteById(12L);
    }

    @Test
    void deveGerarApiKeyParaIntegrationComApiKey() {

        Integration integration = createIntegration(
                "/api/pedidos");

        integration.setAuthType("API_KEY");

        when(integrationRepository.findById(12L))
                .thenReturn(
                        Optional.of(integration));

        when(apiKeyService.generateApiKey())
                .thenReturn(
                        "ihub_chave_teste");

        when(apiKeyService.hashApiKey(
                "ihub_chave_teste")).thenReturn(
                        "hash_chave_teste");

        ApiKeyResponse response = integrationService.generateApiKey(
                12L);

        assertEquals(
                "ihub_chave_teste",
                response.apiKey());

        verify(apiKeyService)
                .generateApiKey();

        verify(apiKeyService)
                .hashApiKey(
                        "ihub_chave_teste");

        verify(integrationRepository)
                .updateApiKey(
                        org.mockito.ArgumentMatchers.eq(12L),
                        org.mockito.ArgumentMatchers.eq(
                                "hash_chave_teste"),
                        any());
    }

    @Test
    void deveRejeitarGeracaoDeApiKeyQuandoAuthTypeNaoForApiKey() {

        Integration integration = createIntegration(
                "/api/pedidos");

        integration.setAuthType("NONE");

        when(integrationRepository.findById(12L))
                .thenReturn(
                        Optional.of(integration));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> integrationService
                        .generateApiKey(
                                12L));

        assertEquals(
                "A integração não está configurada para utilizar API Key",
                exception.getMessage());

        verify(
                apiKeyService,
                never()).generateApiKey();
    }

    @Test
    void deveValidarApiKeyCorreta() {

        Integration integration = createIntegration(
                "/api/pedidos");

        integration.setAuthType("API_KEY");
        integration.setApiKeyHash(
                "hash_chave_teste");

        when(apiKeyService.matches(
                "ihub_chave_teste",
                "hash_chave_teste")).thenReturn(true);

        boolean valid = integrationService.validateApiKey(
                integration,
                "ihub_chave_teste");

        assertEquals(
                true,
                valid);
    }

    @Test
    void devePermitirQuandoIntegrationNaoUsaApiKey() {

        Integration integration = createIntegration(
                "/api/pedidos");

        integration.setAuthType("NONE");

        boolean valid = integrationService.validateApiKey(
                integration,
                null);

        assertEquals(
                true,
                valid);

        verify(
                apiKeyService,
                never()).matches(
                        any(),
                        any());
    }

    private Integration createIntegration(
            String basePath) {

        Integration integration = new Integration();

        integration.setName("Pedidos");

        integration.setDescription(
                "Integração para consulta de pedidos");

        integration.setBasePath(basePath);
        integration.setActive("S");
        integration.setAuthType("NONE");
        integration.setCreatedBy("SYSTEM");

        return integration;
    }
}
