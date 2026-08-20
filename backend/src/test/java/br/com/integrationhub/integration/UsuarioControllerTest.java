package br.com.integrationhub.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void deveBuscarUsuario() throws Exception {
        var resultado = List.<Map<String, Object>>of(
                Map.of(
                        "NM_USUARIO", "usuario_teste",
                        "DS_USUARIO", "Usuário de Teste",
                        "IE_SITUACAO", "A"
                )
        );

        when(usuarioService.buscarUsuario("usuario_teste"))
                .thenReturn(resultado);

        mockMvc.perform(
                get("/api/integrations/usuario")
                        .param("nm_usuario", "usuario_teste")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].NM_USUARIO")
                        .value("usuario_teste"))
                .andExpect(jsonPath("$[0].DS_USUARIO")
                        .value("Usuário de Teste"))
                .andExpect(jsonPath("$[0].IE_SITUACAO")
                        .value("A"));
    }

    @Test
    void deveRetornarBadRequestQuandoNmUsuarioNaoForInformado()
            throws Exception {

        mockMvc.perform(
                get("/api/integrations/usuario")
        )
                .andExpect(status().isBadRequest());
    }
}