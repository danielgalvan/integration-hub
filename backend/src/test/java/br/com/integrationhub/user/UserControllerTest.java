package br.com.integrationhub.user;

import br.com.integrationhub.config.SecurityConfig;
import br.com.integrationhub.config.DataSourceProperties;
import br.com.integrationhub.security.JwtService;
import br.com.integrationhub.user.controller.UserController;
import br.com.integrationhub.user.dto.UserCreateRequest;
import br.com.integrationhub.user.dto.UserCreateResponse;
import br.com.integrationhub.user.dto.UserResponse;
import br.com.integrationhub.user.dto.UserUpdateRequest;
import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private DataSourceProperties dataSourceProperties;

    @Test
    void deveListarUsuarios() throws Exception {

        User user = createUser();

        when(userService.findAll())
                .thenReturn(List.of(user));

        when(userService.toResponse(user))
                .thenReturn(createResponse());

        mockAdminToken();

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].username")
                                .value("admin")
                )
                .andExpect(
                        jsonPath("$[0].type")
                                .value("A")
                )
                .andExpect(
                        jsonPath("$[0].passwordChangeRequired")
                                .value(false)
                );
    }

    @Test
    void deveBuscarUsuarioPorId() throws Exception {

        User user = createUser();

        when(userService.findById(1L))
                .thenReturn(Optional.of(user));

        when(userService.toResponse(user))
                .thenReturn(createResponse());

        mockAdminToken();

        mockMvc.perform(
                        get("/api/users/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("admin")
                );
    }

    @Test
    void deveRetornarNotFoundParaUsuarioInexistente()
            throws Exception {

        when(userService.findById(99L))
                .thenReturn(Optional.empty());

        mockAdminToken();

        mockMvc.perform(
                        get("/api/users/99")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void deveCriarUsuario() throws Exception {

        UserCreateResponse response =
                new UserCreateResponse(
                        createResponse(),
                        "Abc123Xyz"
                );

        when(
                userService.create(
                        any(UserCreateRequest.class)
                )
        ).thenReturn(response);

        mockAdminToken();

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "joao",
                                          "name": "João",
                                          "email": "joao@email.com",
                                          "type": "C"
                                        }
                                        """)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.user.username")
                                .value("admin")
                )
                .andExpect(
                        jsonPath("$.temporaryPassword")
                                .value("Abc123Xyz")
                );
    }

    @Test
    void deveRejeitarCriacaoComUsernameEmBranco()
            throws Exception {

        mockAdminToken();

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "   ",
                                          "name": "João",
                                          "email": "joao@email.com",
                                          "type": "C"
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void deveRejeitarCriacaoComEmailETipoInvalidos()
            throws Exception {

        mockAdminToken();

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "joao",
                                          "name": "João",
                                          "email": "email-invalido",
                                          "type": "X"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarUsuario() throws Exception {

        when(
                userService.update(
                        eq(1L),
                        any(UserUpdateRequest.class)
                )
        ).thenReturn(
                new UserResponse(
                        1L,
                        "joao",
                        "João Atualizado",
                        "joao@email.com",
                        "A",
                        "C",
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        mockAdminToken();

        mockMvc.perform(
                        put("/api/users/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "joao",
                                          "name": "João Atualizado",
                                          "email": "joao@email.com",
                                          "status": "A",
                                          "type": "C"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.name")
                                .value("João Atualizado")
                )
                .andExpect(
                        jsonPath("$.type")
                                .value("C")
                );
    }

    @Test
    void deveRejeitarAtualizacaoComSituacaoOuTipoInvalidos()
            throws Exception {

        mockAdminToken();

        mockMvc.perform(
                        put("/api/users/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "joao",
                                          "name": "João",
                                          "email": "joao@email.com",
                                          "status": "X",
                                          "type": "X"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExcluirUsuario() throws Exception {

        mockAdminToken();

        mockMvc.perform(
                        delete("/api/users/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(userService)
                .delete(1L);
    }

    @Test
    void deveResetarSenha() throws Exception {

        when(userService.resetPassword(1L))
                .thenReturn("Temp987654");

        mockAdminToken();

        mockMvc.perform(
                        post("/api/users/1/reset-password")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.temporaryPassword")
                                .value("Temp987654")
                );

        verify(userService)
                .resetPassword(1L);
    }

    private void mockAdminToken() {

        when(jwtService.isTokenValid("token-admin"))
                .thenReturn(true);

        when(jwtService.getUsername("token-admin"))
                .thenReturn("admin");

        when(jwtService.getRole("token-admin"))
                .thenReturn("A");

        when(jwtService.getEnvironment("token-admin"))
                .thenReturn("test");

        when(dataSourceProperties.getConnection("test"))
                .thenReturn(
                        new DataSourceProperties.ConnectionProperties());
    }

    private User createUser() {

        return new User(
                1L,
                "admin",
                "Administrador",
                "admin@email.com",
                "$hash",
                "A",
                "A",
                "N",
                LocalDateTime.now(),
                null
        );
    }

    private UserResponse createResponse() {

        return new UserResponse(
                1L,
                "admin",
                "Administrador",
                "admin@email.com",
                "A",
                "A",
                false,
                LocalDateTime.now(),
                null
        );
    }
}
