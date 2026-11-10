
package br.com.integrationhub.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    /*
     * Validação de argumentos.
     */

    @Test
    void deveRetornarBadRequestComMensagemEPathDaRequisicao() {

        MockHttpServletRequest request =
                request("/api/endpoints");

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("SQL inválido"),
                        request
                );

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                "SQL inválido",
                "/api/endpoints"
        );
    }

    @Test
    void deveRetornarBadRequestParaPaginaNegativa() {

        MockHttpServletRequest request =
                request("/api/admin/executions");

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException(
                                "Página não pode ser negativa"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                "Página não pode ser negativa",
                "/api/admin/executions"
        );
    }

    @Test
    void deveRetornarBadRequestParaTamanhoDePaginaInvalido() {

        MockHttpServletRequest request =
                request("/api/admin/executions");

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException(
                                "Tamanho da página inválido"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                "Tamanho da página inválido",
                "/api/admin/executions"
        );
    }

    @Test
    void deveRetornarBadRequestParaStatusDeExecucaoInvalido() {

        MockHttpServletRequest request =
                request("/api/admin/executions");

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException(
                                "Status de execução inválido"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                "Status de execução inválido",
                "/api/admin/executions"
        );
    }

    @Test
    void deveRetornarBadRequestParaIntervaloDeDatasInvalido() {

        MockHttpServletRequest request =
                request("/api/admin/executions");

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException(
                                "Intervalo de datas inválido"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                "Intervalo de datas inválido",
                "/api/admin/executions"
        );
    }

    /*
     * Conversão de parâmetros HTTP.
     */

    @Test
    void deveRetornarBadRequestParaPaginaNaoNumerica() {

        assertTypeMismatch(
                "page",
                "abc",
                Integer.class
        );
    }

    @Test
    void deveRetornarBadRequestParaTamanhoNaoNumerico() {

        assertTypeMismatch(
                "size",
                "xyz",
                Integer.class
        );
    }

    @Test
    void deveRetornarBadRequestParaIntegrationIdNaoNumerico() {

        assertTypeMismatch(
                "integrationId",
                "texto",
                Long.class
        );
    }

    @Test
    void deveRetornarBadRequestParaEndpointIdNaoNumerico() {

        assertTypeMismatch(
                "endpointId",
                "invalid",
                Long.class
        );
    }

    @Test
    void deveRetornarBadRequestParaDataInvalida() {

        assertTypeMismatch(
                "startedFrom",
                "invalid",
                java.time.LocalDateTime.class
        );
    }

    /*
     * Parâmetros obrigatórios.
     */

    @Test
    void deveRetornarBadRequestParaParametroObrigatorioAusente() {

        MockHttpServletRequest request =
                request("/api/endpoints");

        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException(
                        "integrationId",
                        "Long"
                );

        ResponseEntity<ApiError> response =
                handler.handleMissingServletRequestParameterException(
                        exception,
                        request
                );

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório não informado: integrationId",
                "/api/endpoints"
        );
    }

    /*
     * Conflitos de estado.
     */

    @Test
    void deveRetornarConflictParaEstadoInvalido() {

        MockHttpServletRequest request =
                request("/api/integrations/1");

        ResponseEntity<ApiError> response =
                handler.handleIllegalStateException(
                        new IllegalStateException(
                                "Integração não pode ser alterada"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.CONFLICT,
                "Integração não pode ser alterada",
                "/api/integrations/1"
        );
    }

    /*
     * Exceções com status definido.
     */

    @Test
    void devePreservarStatusEMensagemDeResponseStatusException() {

        MockHttpServletRequest request =
                request("/api/admin/executions/999");

        ResponseEntity<ApiError> response =
                handler.handleResponseStatusException(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Execução não encontrada"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.NOT_FOUND,
                "Execução não encontrada",
                "/api/admin/executions/999"
        );
    }

    @Test
    void deveUtilizarMensagemPadraoQuandoReasonNaoInformado() {

        MockHttpServletRequest request =
                request("/api/admin/executions/999");

        ResponseEntity<ApiError> response =
                handler.handleResponseStatusException(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.NOT_FOUND,
                "Not Found",
                "/api/admin/executions/999"
        );
    }

    /*
     * Método HTTP não permitido.
     */

    @Test
    void deveRetornarMethodNotAllowedParaMetodoNaoSuportado() {

        MockHttpServletRequest request =
                request("/api/admin/executions");

        ResponseEntity<ApiError> response =
                handler.handleHttpRequestMethodNotSupportedException(
                        new HttpRequestMethodNotSupportedException(
                                "DELETE"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP não suportado",
                "/api/admin/executions"
        );
    }

    /*
     * Erros internos.
     */

    @Test
    void deveOcultarDetalhesDoBancoEmErroInterno() {

        MockHttpServletRequest request =
                request("/api/pedidos/buscar");

        ResponseEntity<ApiError> response =
                handler.handleDataAccessException(
                        new DataAccessResourceFailureException(
                                "Falha de conexão Oracle"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao executar o endpoint configurado",
                "/api/pedidos/buscar"
        );
    }

    @Test
    void deveOcultarDetalhesDeErroInesperado() {

        MockHttpServletRequest request =
                request("/api/admin/executions");

        ResponseEntity<ApiError> response =
                handler.handleException(
                        new RuntimeException(
                                "Detalhes internos do servidor"
                        ),
                        request
                );

        assertError(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor",
                "/api/admin/executions"
        );
    }

    /*
     * Helpers.
     */

    private void assertTypeMismatch(
            String parameterName,
            String value,
            Class<?> requiredType) {

        MockHttpServletRequest request =
                request("/api/admin/executions");

        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(
                        value,
                        requiredType,
                        parameterName,
                        null,
                        new IllegalArgumentException(
                                "Falha na conversão"
                        )
                );

        ResponseEntity<ApiError> response =
                handler.handleMethodArgumentTypeMismatchException(
                        exception,
                        request
                );

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro: "
                        + parameterName,
                "/api/admin/executions"
        );
    }

    private void assertError(
            ResponseEntity<ApiError> response,
            HttpStatus expectedStatus,
            String expectedMessage,
            String expectedPath) {

        assertEquals(
                expectedStatus,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                expectedStatus.value(),
                response.getBody().status()
        );

        assertEquals(
                expectedStatus.getReasonPhrase(),
                response.getBody().error()
        );

        assertEquals(
                expectedMessage,
                response.getBody().message()
        );

        assertEquals(
                expectedPath,
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    private MockHttpServletRequest request(String path) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(path);

        return request;
    }
}
