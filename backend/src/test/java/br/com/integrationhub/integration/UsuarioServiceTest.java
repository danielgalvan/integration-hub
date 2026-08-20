package br.com.integrationhub.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void deveBuscarUsuario() {
        var resultadoEsperado = List.<Map<String, Object>>of(
                Map.of(
                        "NM_USUARIO", "usuario_teste",
                        "DS_USUARIO", "Usuário de Teste",
                        "IE_SITUACAO", "A"
                )
        );

        when(jdbcTemplate.queryForList(
                anyString(),
                any(SqlParameterSource.class)
        )).thenReturn(resultadoEsperado);

        var service = new UsuarioService(jdbcTemplate);

        var resultado = service.buscarUsuario("usuario_teste");

        assertEquals(1, resultado.size());
        assertEquals(
                "usuario_teste",
                resultado.get(0).get("NM_USUARIO")
        );
        assertEquals(
                "Usuário de Teste",
                resultado.get(0).get("DS_USUARIO")
        );
        assertEquals(
                "A",
                resultado.get(0).get("IE_SITUACAO")
        );
    }
}