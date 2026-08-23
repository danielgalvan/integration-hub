package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.repository.IntegrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationServiceTest {

    private IntegrationRepository integrationRepository;
    private IntegrationService integrationService;

    @BeforeEach
    void setUp() {
        integrationRepository = mock(IntegrationRepository.class);
        integrationService = new IntegrationService(
                integrationRepository
        );
    }

    @Test
    void deveSalvarIntegrationComBasePathValido() {

        Integration integration = createIntegration(
                "/api/pedidos"
        );

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration
        );

        assertEquals(
                "/api/pedidos",
                result.getBasePath()
        );

        verify(integrationRepository).save(
                integration
        );
    }

    @Test
    void deveDefinirActiveComoSQuandoNaoInformado() {

        Integration integration = createIntegration(
                "/api/pedidos"
        );

        integration.setActive(null);

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration
        );

        assertEquals(
                "S",
                result.getActive()
        );
    }

    @Test
    void deveDefinirCreatedByComoSystemQuandoNaoInformado() {

        Integration integration = createIntegration(
                "/api/pedidos"
        );

        integration.setCreatedBy(null);

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration
        );

        assertEquals(
                "SYSTEM",
                result.getCreatedBy()
        );
    }

    @Test
    void deveRejeitarBasePathNulo() {

        Integration integration = createIntegration(
                null
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> integrationService.save(
                                integration
                        )
                );

        assertEquals(
                "basePath é obrigatório",
                exception.getMessage()
        );

        verify(
                integrationRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarBasePathVazio() {

        Integration integration = createIntegration(
                "   "
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> integrationService.save(
                                integration
                        )
                );

        assertEquals(
                "basePath é obrigatório",
                exception.getMessage()
        );

        verify(
                integrationRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarBasePathSemApi() {

        Integration integration = createIntegration(
                "/pedidos"
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> integrationService.save(
                                integration
                        )
                );

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage()
        );

        verify(
                integrationRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarBasePathSemBarraInicial() {

        Integration integration = createIntegration(
                "api/pedidos"
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> integrationService.save(
                                integration
                        )
                );

        assertEquals(
                "basePath deve iniciar com /api/",
                exception.getMessage()
        );

        verify(
                integrationRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarBasePathTerminandoComBarra() {

        Integration integration = createIntegration(
                "/api/pedidos/"
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> integrationService.save(
                                integration
                        )
                );

        assertEquals(
                "basePath não deve terminar com /",
                exception.getMessage()
        );

        verify(
                integrationRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarBasePathComEspaco() {

        Integration integration = createIntegration(
                "/api/meus pedidos"
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> integrationService.save(
                                integration
                        )
                );

        assertEquals(
                "basePath não deve conter espaços",
                exception.getMessage()
        );

        verify(
                integrationRepository,
                never()
        ).save(any());
    }

    @Test
    void deveAceitarBasePathComMaisDeUmNivel() {

        Integration integration = createIntegration(
                "/api/pedidos/especiais"
        );

        when(integrationRepository.save(integration))
                .thenReturn(integration);

        Integration result = integrationService.save(
                integration
        );

        assertEquals(
                "/api/pedidos/especiais",
                result.getBasePath()
        );

        verify(integrationRepository).save(
                integration
        );
    }

    private Integration createIntegration(
            String basePath) {

        Integration integration = new Integration();

        integration.setName("Pedidos");
        integration.setDescription(
                "Integração para consulta de pedidos"
        );
        integration.setBasePath(basePath);
        integration.setActive("S");
        integration.setCreatedBy("SYSTEM");

        return integration;
    }
}