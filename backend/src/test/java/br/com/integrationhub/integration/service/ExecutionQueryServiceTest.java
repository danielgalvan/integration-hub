
package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.repository.ExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionQueryServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @InjectMocks
    private ExecutionQueryService executionQueryService;

    private Execution execution;

    @BeforeEach
    void setUp() {
        execution = new Execution();
        execution.setId(1L);
    }

    @Test
    void deveListarExecucoesComFiltros() {

        LocalDateTime startedFrom = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime startedTo = LocalDateTime.of(2026, 11, 10, 23, 59);

        when(executionRepository.findAll(
                10L,
                20L,
                "SUCCESS",
                startedFrom,
                startedTo,
                1,
                10
        )).thenReturn(List.of(execution));

        List<Execution> result = executionQueryService.findAll(
                10L,
                20L,
                "SUCCESS",
                startedFrom,
                startedTo,
                1,
                10
        );

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(executionRepository).findAll(
                10L,
                20L,
                "SUCCESS",
                startedFrom,
                startedTo,
                1,
                10
        );
    }

    @Test
    void deveUtilizarPaginacaoPadrao() {

        when(executionRepository.findAll(
                null, null, null, null, null, 0, 20
        )).thenReturn(List.of(execution));

        List<Execution> result = executionQueryService.findAll(
                null, null, null, null, null, null, null
        );

        assertEquals(1, result.size());

        verify(executionRepository).findAll(
                null, null, null, null, null, 0, 20
        );
    }

    @Test
    void deveNormalizarStatusDaExecucao() {

        when(executionRepository.findAll(
                null, null, "SUCCESS", null, null, 0, 20
        )).thenReturn(List.of(execution));

        List<Execution> result = executionQueryService.findAll(
                null, null, " success ", null, null, 0, 20
        );

        assertEquals(1, result.size());

        verify(executionRepository).findAll(
                null, null, "SUCCESS", null, null, 0, 20
        );
    }

    @Test
    void deveIgnorarStatusEmBranco() {

        when(executionRepository.findAll(
                null, null, null, null, null, 0, 20
        )).thenReturn(List.of());

        List<Execution> result = executionQueryService.findAll(
                null, null, "   ", null, null, 0, 20
        );

        assertTrue(result.isEmpty());

        verify(executionRepository).findAll(
                null, null, null, null, null, 0, 20
        );
    }

    @Test
    void deveContarExecucoesComFiltros() {

        LocalDateTime startedFrom = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime startedTo = LocalDateTime.of(2026, 11, 10, 23, 59);

        when(executionRepository.count(
                10L,
                20L,
                "ERROR",
                startedFrom,
                startedTo
        )).thenReturn(15L);

        long result = executionQueryService.count(
                10L,
                20L,
                " error ",
                startedFrom,
                startedTo
        );

        assertEquals(15L, result);

        verify(executionRepository).count(
                10L,
                20L,
                "ERROR",
                startedFrom,
                startedTo
        );
    }

    @Test
    void deveConsultarExecucaoPorId() {

        when(executionRepository.findById(1L))
                .thenReturn(Optional.of(execution));

        Optional<Execution> result = executionQueryService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());

        verify(executionRepository).findById(1L);
    }

    @Test
    void deveRetornarOptionalVazioQuandoExecucaoNaoExistir() {

        when(executionRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Execution> result = executionQueryService.findById(999L);

        assertTrue(result.isEmpty());

        verify(executionRepository).findById(999L);
    }

    @Test
    void deveRejeitarIdDaExecucaoNulo() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findById(null)
        );

        assertEquals("ID da execução inválido", exception.getMessage());

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveRejeitarIdDaExecucaoInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findById(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findById(-1L)
        );

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveRejeitarIdDaIntegracaoInvalido() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findAll(
                        0L, null, null, null, null, 0, 20
                )
        );

        assertEquals("ID da integração inválido", exception.getMessage());

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveRejeitarIdDoEndpointInvalido() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findAll(
                        null, -1L, null, null, null, 0, 20
                )
        );

        assertEquals("ID do endpoint inválido", exception.getMessage());

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveRejeitarStatusInvalido() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findAll(
                        null, null, "INVALID", null, null, 0, 20
                )
        );

        assertEquals("Status da execução inválido", exception.getMessage());

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveAceitarTodosOsStatusValidos() {

        List<String> statuses = List.of(
                "SUCCESS",
                "ERROR",
                "UNAUTHORIZED",
                "NOT_FOUND"
        );

        for (String status : statuses) {

            executionQueryService.findAll(
                    null, null, status, null, null, 0, 20
            );

            verify(executionRepository).findAll(
                    null, null, status, null, null, 0, 20
            );
        }
    }

    @Test
    void deveRejeitarPeriodoInvalido() {

        LocalDateTime startedFrom = LocalDateTime.of(2026, 11, 10, 0, 0);
        LocalDateTime startedTo = LocalDateTime.of(2026, 11, 1, 0, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findAll(
                        null, null, null, startedFrom, startedTo, 0, 20
                )
        );

        assertEquals(
                "Data inicial não pode ser posterior à data final",
                exception.getMessage()
        );

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveAceitarPeriodoComDatasIguais() {

        LocalDateTime date = LocalDateTime.of(2026, 11, 10, 10, 0);

        executionQueryService.findAll(
                null, null, null, date, date, 0, 20
        );

        verify(executionRepository).findAll(
                null, null, null, date, date, 0, 20
        );
    }

    @Test
    void deveRejeitarPaginaNegativa() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findAll(
                        null, null, null, null, null, -1, 20
                )
        );

        assertEquals("Número da página inválido", exception.getMessage());

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveRejeitarTamanhoDePaginaZero() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findAll(
                        null, null, null, null, null, 0, 0
                )
        );

        assertEquals(
                "Tamanho da página deve estar entre 1 e 100",
                exception.getMessage()
        );

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveRejeitarTamanhoDePaginaAcimaDoLimite() {

        assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.findAll(
                        null, null, null, null, null, 0, 101
                )
        );

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveAceitarTamanhoMaximoDePagina() {

        executionQueryService.findAll(
                null, null, null, null, null, 0, 100
        );

        verify(executionRepository).findAll(
                null, null, null, null, null, 0, 100
        );
    }

    @Test
    void deveRejeitarFiltrosInvalidosNaContagem() {

        assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.count(
                        -1L, null, null, null, null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> executionQueryService.count(
                        null, null, "INVALID", null, null
                )
        );

        verifyNoInteractions(executionRepository);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremExecucoes() {

        when(executionRepository.findAll(
                null, null, null, null, null, 0, 20
        )).thenReturn(List.of());

        List<Execution> result = executionQueryService.findAll(
                null, null, null, null, null, 0, 20
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deveRetornarZeroQuandoNaoExistiremExecucoes() {

        when(executionRepository.count(
                null, null, null, null, null
        )).thenReturn(0L);

        long result = executionQueryService.count(
                null, null, null, null, null
        );

        assertEquals(0L, result);
    }
}
