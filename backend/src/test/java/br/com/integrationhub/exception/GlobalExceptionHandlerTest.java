package br.com.integrationhub.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void deveRetornarBadRequestComMensagemEPathDaRequisicao() {
        MockHttpServletRequest request = request("/api/endpoints");

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("SQL inválido"),
                        request
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SQL inválido", response.getBody().message());
        assertEquals("/api/endpoints", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void deveOcultarDetalhesDoBancoEmErroInterno() {
        MockHttpServletRequest request = request("/api/pedidos/buscar");

        ResponseEntity<ApiError> response =
                handler.handleDataAccessException(
                        new DataAccessResourceFailureException(
                                "Falha de conexão Oracle"
                        ),
                        request
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(
                "Erro ao executar o endpoint configurado",
                response.getBody().message()
        );
        assertEquals("/api/pedidos/buscar", response.getBody().path());
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);

        return request;
    }
}
