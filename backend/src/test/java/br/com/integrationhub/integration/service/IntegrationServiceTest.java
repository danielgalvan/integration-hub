
package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.ApiKeyResponse;
import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.repository.EndpointRepository;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import br.com.integrationhub.security.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

        Integration integration = createIntegration("/api/pedidos");

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(integration);

        assertEquals("/api/pedidos", result.getBasePath());

        verify(integrationRepository).save(integration);
    }

    @Test
    void deveDefinirActiveComoSQuandoNaoInformado() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setActive(null);

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(integration);

        assertEquals("S", result.getActive());
    }

    @Test
    void deveDefinirAuthTypeComoNoneQuandoNaoInformado() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType(null);

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(integration);

        assertEquals("NONE", result.getAuthType());
    }

    @Test
    void deveDefinirCreatedByComoSystemQuandoNaoInformado() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setCreatedBy(null);

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(integration);

        assertEquals("SYSTEM", result.getCreatedBy());
    }

    @Test
    void deveDefinirLegacyApiKeyComoNParaNovaIntegration() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setLegacyApiKeyActive("S");

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(integration);

        assertEquals("N", result.getLegacyApiKeyActive());
    }

    @Test
    void deveRejeitarBasePathNulo() {

        Integration integration = createIntegration(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(integration));

        assertEquals("basePath é obrigatório", exception.getMessage());

        verify(integrationRepository, never()).save(any());
    }

    @Test
    void deveRejeitarBasePathVazio() {

        Integration integration = createIntegration("   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(integration));

        assertEquals("basePath é obrigatório", exception.getMessage());

        verify(integrationRepository, never()).save(any());
    }

    @Test
    void deveRejeitarBasePathSemApi() {

        Integration integration = createIntegration("/pedidos");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(integration));

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage());

        verify(integrationRepository, never()).save(any());
    }

    @Test
    void deveRejeitarBasePathSemBarraInicial() {

        Integration integration = createIntegration("api/pedidos");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(integration));

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage());

        verify(integrationRepository, never()).save(any());
    }

    @Test
    void deveRejeitarBasePathTerminandoComBarra() {

        Integration integration = createIntegration("/api/pedidos/");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(integration));

        assertEquals(
                "basePath não deve terminar com /",
                exception.getMessage());

        verify(integrationRepository, never()).save(any());
    }

    @Test
    void deveRejeitarBasePathComEspaco() {

        Integration integration = createIntegration("/api/meus pedidos");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.save(integration));

        assertEquals(
                "basePath não deve conter espaços",
                exception.getMessage());

        verify(integrationRepository, never()).save(any());
    }

    @Test
    void deveAceitarBasePathComMaisDeUmNivel() {

        Integration integration = createIntegration(
                "/api/pedidos/especiais");

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(integration);

        assertEquals(
                "/api/pedidos/especiais",
                result.getBasePath());

        verify(integrationRepository).save(integration);
    }

    @Test
    void deveAtualizarIntegrationExistente() {

        Integration current = createIntegration("/api/pedidos");
        Integration updated = createIntegration("/api/pedidos");

        updated.setName("Pedidos Atualizados");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(12L, updated))
                .thenReturn(updated);

        Integration result = integrationService.update(12L, updated);

        assertEquals("Pedidos Atualizados", result.getName());

        verify(integrationRepository).update(12L, updated);
    }

    @Test
    void deveDefinirUpdatedByComoSystemNoUpdate() {

        Integration current = createIntegration("/api/pedidos");
        Integration updated = createIntegration("/api/pedidos");

        updated.setUpdatedBy(null);

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(12L, updated))
                .thenReturn(updated);

        Integration result = integrationService.update(12L, updated);

        assertEquals("SYSTEM", result.getUpdatedBy());
    }

    @Test
    void deveManterActiveAtualQuandoNaoInformadoNoUpdate() {

        Integration current = createIntegration("/api/pedidos");
        current.setActive("N");

        Integration updated = createIntegration("/api/pedidos");
        updated.setActive(null);

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(12L, updated))
                .thenReturn(updated);

        Integration result = integrationService.update(12L, updated);

        assertEquals("N", result.getActive());
    }

    @Test
    void deveManterAuthTypeAtualQuandoNaoInformadoNoUpdate() {

        Integration current = createIntegration("/api/pedidos");
        current.setAuthType("API_KEY");

        Integration updated = createIntegration("/api/pedidos");
        updated.setAuthType(null);

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(12L, updated))
                .thenReturn(updated);

        Integration result = integrationService.update(12L, updated);

        assertEquals("API_KEY", result.getAuthType());
    }

    @Test
    void devePreservarLegacyApiKeyActiveNoUpdate() {

        Integration current = createIntegration("/api/pedidos");
        current.setLegacyApiKeyActive("S");

        Integration updated = createIntegration("/api/pedidos");
        updated.setLegacyApiKeyActive("N");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        when(integrationRepository.update(12L, updated))
                .thenReturn(updated);

        Integration result = integrationService.update(12L, updated);

        assertEquals("S", result.getLegacyApiKeyActive());

        verify(integrationRepository).update(12L, updated);
    }

    @Test
    void deveRejeitarUpdateQuandoIntegrationNaoExiste() {

        Integration integration = createIntegration("/api/pedidos");

        when(integrationRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.update(99L, integration));

        assertEquals(
                "Integração não encontrada: 99",
                exception.getMessage());

        verify(integrationRepository, never()).update(any(), any());
    }

    @Test
    void deveRejeitarBasePathInvalidoNoUpdate() {

        Integration current = createIntegration("/api/pedidos");
        Integration integration = createIntegration("/pedidos");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(current));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.update(12L, integration));

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage());

        verify(integrationRepository, never()).update(any(), any());
    }

    @Test
    void deveAtivarLegacyApiKey() {

        Integration integration = createIntegration("/api/pedidos");
        Integration updated = createIntegration("/api/pedidos");
        updated.setLegacyApiKeyActive("S");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        when(integrationRepository.updateLegacyApiKeyActive(12L, "S"))
                .thenReturn(updated);

        Integration result = integrationService.updateLegacyApiKeyActive(
                12L, "S");

        assertEquals("S", result.getLegacyApiKeyActive());

        verify(integrationRepository)
                .updateLegacyApiKeyActive(12L, "S");
    }

    @Test
    void deveDesativarLegacyApiKey() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setLegacyApiKeyActive("S");

        Integration updated = createIntegration("/api/pedidos");
        updated.setLegacyApiKeyActive("N");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        when(integrationRepository.updateLegacyApiKeyActive(12L, "N"))
                .thenReturn(updated);

        Integration result = integrationService.updateLegacyApiKeyActive(
                12L, "N");

        assertEquals("N", result.getLegacyApiKeyActive());

        verify(integrationRepository)
                .updateLegacyApiKeyActive(12L, "N");
    }

    @Test
    void deveRejeitarValorInvalidoParaLegacyApiKey() {

        Integration integration = createIntegration("/api/pedidos");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.updateLegacyApiKeyActive(
                        12L, "X"));

        assertEquals(
                "legacyApiKeyActive deve ser S ou N",
                exception.getMessage());

        verify(integrationRepository, never())
                .updateLegacyApiKeyActive(any(), any());
    }

    @Test
    void deveRejeitarValorNuloParaLegacyApiKey() {

        Integration integration = createIntegration("/api/pedidos");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.updateLegacyApiKeyActive(
                        12L, null));

        verify(integrationRepository, never())
                .updateLegacyApiKeyActive(any(), any());
    }

    @Test
    void deveRejeitarAlteracaoLegacyQuandoIntegrationNaoExiste() {

        when(integrationRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.updateLegacyApiKeyActive(
                        99L, "N"));

        assertEquals(
                "Integração não encontrada: 99",
                exception.getMessage());

        verify(integrationRepository, never())
                .updateLegacyApiKeyActive(any(), any());
    }

    @Test
    void deveExcluirIntegrationSemEndpoints() {

        Integration integration = createIntegration("/api/pedidos");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        when(endpointRepository.findByIntegrationId(12L))
                .thenReturn(List.of());

        integrationService.delete(12L);

        verify(integrationRepository).deleteById(12L);
    }

    @Test
    void deveRejeitarDeleteQuandoIntegrationNaoExiste() {

        when(integrationRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> integrationService.delete(99L));

        assertEquals(
                "Integração não encontrada: 99",
                exception.getMessage());

        verify(integrationRepository, never()).deleteById(any());
    }

    @Test
    void deveRejeitarDeleteQuandoPossuiEndpoints() {

        Integration integration = createIntegration("/api/pedidos");
        Endpoint endpoint = mock(Endpoint.class);

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        when(endpointRepository.findByIntegrationId(12L))
                .thenReturn(List.of(endpoint));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> integrationService.delete(12L));

        assertEquals(
                "A integração possui endpoints vinculados",
                exception.getMessage());

        verify(integrationRepository, never()).deleteById(12L);
    }

    @Test
    void deveGerarApiKeyParaIntegrationComApiKey() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("API_KEY");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        when(apiKeyService.generateApiKey())
                .thenReturn("ihub_chave_teste");

        when(apiKeyService.hashApiKey("ihub_chave_teste"))
                .thenReturn("hash_chave_teste");

        ApiKeyResponse response = integrationService.generateApiKey(12L);

        assertEquals("ihub_chave_teste", response.apiKey());

        verify(apiKeyService).generateApiKey();
        verify(apiKeyService).hashApiKey("ihub_chave_teste");

        verify(integrationRepository).updateApiKey(
                eq(12L),
                eq("hash_chave_teste"),
                any());

        verify(integrationRepository)
                .updateLegacyApiKeyActive(12L, "S");
    }

    @Test
    void deveRejeitarGeracaoDeApiKeyQuandoAuthTypeNaoForApiKey() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("NONE");

        when(integrationRepository.findById(12L))
                .thenReturn(Optional.of(integration));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> integrationService.generateApiKey(12L));

        assertEquals(
                "A integração não está configurada para utilizar API Key",
                exception.getMessage());

        verify(apiKeyService, never()).generateApiKey();

        verify(integrationRepository, never())
                .updateLegacyApiKeyActive(any(), any());
    }

    @Test
    void deveValidarApiKeyLegadaCorretaQuandoAtiva() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("API_KEY");
        integration.setLegacyApiKeyActive("S");
        integration.setApiKeyHash("hash_chave_teste");

        when(apiKeyService.matches(
                "ihub_chave_teste",
                "hash_chave_teste"))
                .thenReturn(true);

        boolean valid = integrationService.validateApiKey(
                integration, "ihub_chave_teste");

        assertTrue(valid);

        verify(apiKeyService).matches(
                "ihub_chave_teste",
                "hash_chave_teste");
    }

    @Test
    void deveRejeitarApiKeyLegadaInvalida() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("API_KEY");
        integration.setLegacyApiKeyActive("S");
        integration.setApiKeyHash("hash_chave_teste");

        when(apiKeyService.matches(
                "ihub_chave_invalida",
                "hash_chave_teste"))
                .thenReturn(false);

        boolean valid = integrationService.validateApiKey(
                integration, "ihub_chave_invalida");

        assertFalse(valid);
    }

    @Test
    void deveRejeitarApiKeyLegadaQuandoDesativada() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("API_KEY");
        integration.setLegacyApiKeyActive("N");
        integration.setApiKeyHash("hash_chave_teste");

        boolean valid = integrationService.validateApiKey(
                integration, "ihub_chave_teste");

        assertFalse(valid);

        verify(apiKeyService, never()).matches(any(), any());
    }

    @Test
    void deveRejeitarApiKeyLegadaQuandoIndicadorNulo() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("API_KEY");
        integration.setLegacyApiKeyActive(null);
        integration.setApiKeyHash("hash_chave_teste");

        boolean valid = integrationService.validateApiKey(
                integration, "ihub_chave_teste");

        assertFalse(valid);

        verify(apiKeyService, never()).matches(any(), any());
    }

    @Test
    void deveRejeitarApiKeyLegadaNulaOuVazia() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("API_KEY");
        integration.setLegacyApiKeyActive("S");
        integration.setApiKeyHash("hash_chave_teste");

        assertFalse(integrationService.validateApiKey(
                integration, null));

        assertFalse(integrationService.validateApiKey(
                integration, "  "));

        verify(apiKeyService, never()).matches(any(), any());
    }

    @Test
    void deveRejeitarValidacaoLegadaQuandoIntegrationNaoUsaApiKey() {

        Integration integration = createIntegration("/api/pedidos");
        integration.setAuthType("NONE");
        integration.setLegacyApiKeyActive("S");

        boolean valid = integrationService.validateApiKey(
                integration, "ihub_chave_teste");

        assertFalse(valid);

        verify(apiKeyService, never()).matches(any(), any());
    }

    @Test
    void deveRejeitarValidacaoLegadaQuandoIntegrationNula() {

        boolean valid = integrationService.validateApiKey(
                null, "ihub_chave_teste");

        assertFalse(valid);

        verify(apiKeyService, never()).matches(any(), any());
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
        integration.setLegacyApiKeyActive("N");
        integration.setCreatedBy("SYSTEM");

        return integration;
    }
}
