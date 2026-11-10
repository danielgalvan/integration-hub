
package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.service.ExecutionQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExecutionAdminControllerTest {

    @Mock
    private ExecutionQueryService executionQueryService;

    private MockMvc mockMvc;

    private Execution execution;

    @BeforeEach
    void setUp() {

        ExecutionAdminController controller =
                new ExecutionAdminController(executionQueryService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        execution = new Execution();
        execution.setId(1L);
        execution.setIntegrationId(10L);
        execution.setEndpointId(20L);
        execution.setExecutionStatus("SUCCESS");
        execution.setHttpStatus(200);
    }

    @Test
    void deveListarExecucoesComPaginacaoPadrao() throws Exception {

        when(executionQueryService.findAll(
                null, null, null, null, null, 0, 20
        )).thenReturn(List.of(execution));

        when(executionQueryService.count(
                null, null, null, null, null
        )).thenReturn(1L);

        mockMvc.perform(get("/api/admin/executions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].integrationId").value(10))
                .andExpect(jsonPath("$.content[0].endpointId").value(20))
                .andExpect(jsonPath("$.content[0].executionStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(executionQueryService).findAll(
                null, null, null, null, null, 0, 20
        );

        verify(executionQueryService).count(
                null, null, null, null, null
        );
    }

    @Test
    void deveListarExecucoesComFiltros() throws Exception {

        LocalDateTime startedFrom =
                LocalDateTime.of(2026, 11, 1, 0, 0);

        LocalDateTime startedTo =
                LocalDateTime.of(2026, 11, 10, 23, 59);

        when(executionQueryService.findAll(
                10L,
                20L,
                "SUCCESS",
                startedFrom,
                startedTo,
                1,
                10
        )).thenReturn(List.of(execution));

        when(executionQueryService.count(
                10L,
                20L,
                "SUCCESS",
                startedFrom,
                startedTo
        )).thenReturn(15L);

        mockMvc.perform(get("/api/admin/executions")
                        .param("integrationId", "10")
                        .param("endpointId", "20")
                        .param("executionStatus", "SUCCESS")
                        .param("startedFrom", "2026-11-01T00:00:00")
                        .param("startedTo", "2026-11-10T23:59:00")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(executionQueryService).findAll(
                10L,
                20L,
                "SUCCESS",
                startedFrom,
                startedTo,
                1,
                10
        );

        verify(executionQueryService).count(
                10L,
                20L,
                "SUCCESS",
                startedFrom,
                startedTo
        );
    }

    @Test
    void deveCalcularTotalDePaginasCorretamente() throws Exception {

        when(executionQueryService.findAll(
                null, null, null, null, null, 0, 20
        )).thenReturn(List.of());

        when(executionQueryService.count(
                null, null, null, null, null
        )).thenReturn(41L);

        mockMvc.perform(get("/api/admin/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(41))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremExecucoes() throws Exception {

        when(executionQueryService.findAll(
                null, null, null, null, null, 0, 20
        )).thenReturn(List.of());

        when(executionQueryService.count(
                null, null, null, null, null
        )).thenReturn(0L);

        mockMvc.perform(get("/api/admin/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void deveConsultarExecucaoPorId() throws Exception {

        when(executionQueryService.findById(1L))
                .thenReturn(Optional.of(execution));

        mockMvc.perform(get("/api/admin/executions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.integrationId").value(10))
                .andExpect(jsonPath("$.endpointId").value(20))
                .andExpect(jsonPath("$.executionStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200));

        verify(executionQueryService).findById(1L);
    }

    @Test
    void deveRetornarNotFoundQuandoExecucaoNaoExistir() throws Exception {

        when(executionQueryService.findById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/executions/999"))
                .andExpect(status().isNotFound());

        verify(executionQueryService).findById(999L);
    }

    @Test
    void deveRetornarBadRequestParaIdNaoNumerico() throws Exception {

        mockMvc.perform(get("/api/admin/executions/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(executionQueryService);
    }

    @Test
    void deveRetornarBadRequestParaPaginaNaoNumerica() throws Exception {

        mockMvc.perform(get("/api/admin/executions")
                        .param("page", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(executionQueryService);
    }

    @Test
    void deveRetornarBadRequestParaTamanhoNaoNumerico() throws Exception {

        mockMvc.perform(get("/api/admin/executions")
                        .param("size", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(executionQueryService);
    }

    @Test
    void deveRetornarBadRequestParaIntegrationIdNaoNumerico() throws Exception {

        mockMvc.perform(get("/api/admin/executions")
                        .param("integrationId", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(executionQueryService);
    }

    @Test
    void deveRetornarBadRequestParaEndpointIdNaoNumerico() throws Exception {

        mockMvc.perform(get("/api/admin/executions")
                        .param("endpointId", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(executionQueryService);
    }

    @Test
    void deveRetornarBadRequestParaDataInvalida() throws Exception {

        mockMvc.perform(get("/api/admin/executions")
                        .param("startedFrom", "data-invalida"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(executionQueryService);
    }

    @Test
    void deveAceitarPaginacaoPersonalizada() throws Exception {

        when(executionQueryService.findAll(
                null, null, null, null, null, 2, 50
        )).thenReturn(List.of(execution));

        when(executionQueryService.count(
                null, null, null, null, null
        )).thenReturn(120L);

        mockMvc.perform(get("/api/admin/executions")
                        .param("page", "2")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").value(120))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void deveAceitarFiltroSomentePorStatus() throws Exception {

        when(executionQueryService.findAll(
                null, null, "ERROR", null, null, 0, 20
        )).thenReturn(List.of());

        when(executionQueryService.count(
                null, null, "ERROR", null, null
        )).thenReturn(0L);

        mockMvc.perform(get("/api/admin/executions")
                        .param("executionStatus", "ERROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(executionQueryService).findAll(
                null, null, "ERROR", null, null, 0, 20
        );
    }

    @Test
    void deveConsultarUltimaPaginaComMenosRegistros() throws Exception {

        when(executionQueryService.findAll(
                null, null, null, null, null, 2, 10
        )).thenReturn(List.of(execution));

        when(executionQueryService.count(
                null, null, null, null, null
        )).thenReturn(21L);

        mockMvc.perform(get("/api/admin/executions")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(21))
                .andExpect(jsonPath("$.totalPages").value(3));
    }
}
