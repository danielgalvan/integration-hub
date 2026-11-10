
package br.com.integrationhub.config;

import br.com.integrationhub.auth.AuthController;
import br.com.integrationhub.auth.AuthService;
import br.com.integrationhub.auth.LoginResponse;
import br.com.integrationhub.controller.HealthController;
import br.com.integrationhub.integration.controller.DynamicEndpointController;
import br.com.integrationhub.integration.controller.EndpointController;
import br.com.integrationhub.integration.controller.ExecutionAdminController;
import br.com.integrationhub.integration.controller.IntegrationController;
import br.com.integrationhub.integration.model.Execution;
import br.com.integrationhub.integration.service.DynamicEndpointService;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.ExecutionAuditService;
import br.com.integrationhub.integration.service.ExecutionQueryService;
import br.com.integrationhub.integration.service.IntegrationCredentialService;
import br.com.integrationhub.integration.service.IntegrationService;
import br.com.integrationhub.security.JwtService;
import br.com.integrationhub.service.DatabaseHealthService;
import br.com.integrationhub.user.controller.UserController;
import br.com.integrationhub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        IntegrationController.class,
        EndpointController.class,
        DynamicEndpointController.class,
        ExecutionAdminController.class,
        HealthController.class,
        EnvironmentController.class,
        AuthController.class,
        UserController.class
})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegrationService integrationService;

    @MockitoBean
    private IntegrationCredentialService integrationCredentialService;

    @MockitoBean
    private EndpointService endpointService;

    @MockitoBean
    private DynamicEndpointService dynamicEndpointService;

    @MockitoBean
    private ExecutionQueryService executionQueryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private DataSourceProperties dataSourceProperties;

    @MockitoBean
    private DatabaseHealthService databaseHealthService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ExecutionAuditService executionAuditService;

    @BeforeEach
    void setUp() {

        when(dataSourceProperties.getConnection("dev"))
                .thenReturn(
                        new DataSourceProperties.ConnectionProperties()
                );
    }

    /*
     * Rotas públicas.
     */

    @Test
    void devePermitirHealthSemToken() throws Exception {

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirLoginSemToken() throws Exception {

        when(authService.login(any()))
                .thenReturn(
                        new LoginResponse(
                                "token",
                                "Bearer",
                                3600,
                                false
                        )
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "senha",
                                          "environment": "dev"
                                        }
                                        """)
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirListagemDeAmbientesSemToken()
            throws Exception {

        mockMvc.perform(get("/api/environments"))
                .andExpect(status().isOk());
    }

    /*
     * Autenticação e troca de senha.
     */

    @Test
    void deveBloquearTrocaDeSenhaSemToken()
            throws Exception {

        mockMvc.perform(
                        put("/api/auth/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAdministradorTrocarSenha()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void devePermitirCriadorTrocarSenha()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void devePermitirConsumidorTrocarSenha()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        put("/api/auth/password")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "newPassword": "novaSenha"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deveExigirTokenParaConsultarUsuarioAtual()
            throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirPerfilConsumidorConsultarUsuarioAtual()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearTokenInvalido() throws Exception {

        when(jwtService.isTokenValid("token-invalido"))
                .thenReturn(false);

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-invalido"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    /*
     * Administração de usuários.
     */

    @Test
    void deveBloquearUsuariosSemToken()
            throws Exception {

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAdministradorConsultarUsuarios()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearCriadorAoConsultarUsuarios()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearConsumidorAoConsultarUsuarios()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isForbidden());
    }

    /*
     * Integrações.
     */

    @Test
    void deveBloquearIntegrationsSemToken()
            throws Exception {

        mockMvc.perform(get("/api/integrations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAdministradorConsultarIntegrations()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirCriadorConsultarIntegrations()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirConsumidorConsultarIntegrations()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        get("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirAdministradorExcluirIntegration()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        mockMvc.perform(
                        delete("/api/integrations/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void devePermitirCriadorExcluirIntegration()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        delete("/api/integrations/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deveBloquearConsumidorAoExcluirIntegration()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        delete("/api/integrations/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdministradorECriadorCriarIntegration()
            throws Exception {

        mockToken("token-admin", "admin", "A");
        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        post("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Clientes",
                                          "basePath": "/api/clientes"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Pedidos",
                                          "basePath": "/api/pedidos"
                                        }
                                        """)
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearConsumidorAoCriarIntegration()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        post("/api/integrations")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden());
    }

    /*
     * Endpoints administrativos.
     */

    @Test
    void deveBloquearEndpointsSemToken()
            throws Exception {

        mockMvc.perform(get("/api/endpoints"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAdministradorConsultarEndpoints()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        mockMvc.perform(
                        get("/api/endpoints")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirCriadorConsultarEndpoints()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        get("/api/endpoints")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirConsumidorConsultarEndpoints()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        get("/api/endpoints")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirAdministradorExcluirEndpoint()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        mockMvc.perform(
                        delete("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void devePermitirCriadorExcluirEndpoint()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        delete("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deveBloquearConsumidorAoExcluirEndpoint()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        delete("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirCriadorAtualizarEndpointEConsumidorNao()
            throws Exception {

        mockToken("token-criador", "criador", "C");
        mockToken("token-consumidor", "consumidor", "U");

        String endpoint = """
                {
                  "integrationId": 1,
                  "name": "Buscar cliente",
                  "path": "/buscar",
                  "method": "GET",
                  "sqlText": "select id from cliente"
                }
                """;

        mockMvc.perform(
                        put("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(endpoint)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/api/endpoints/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(endpoint)
                )
                .andExpect(status().isForbidden());
    }

    /*
     * Endpoints dinâmicos.
     */

    @Test
    void devePermitirEndpointDinamicoSemToken()
            throws Exception {

        when(integrationService.findBestMatchByRequestPath(any()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clientes/buscar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void devePermitirTodosOsPerfisNoEndpointDinamico()
            throws Exception {

        mockToken("token-admin", "admin", "A");
        mockToken("token-criador", "criador", "C");
        mockToken("token-consumidor", "consumidor", "U");

        when(integrationService.findBestMatchByRequestPath(any()))
                .thenReturn(Optional.empty());

        for (String token : new String[]{
                "token-admin",
                "token-criador",
                "token-consumidor"
        }) {

            mockMvc.perform(
                            get("/api/clientes/buscar")
                                    .header(
                                            "Authorization",
                                            "Bearer " + token
                                    )
                    )
                    .andExpect(status().isNotFound());
        }
    }

    /*
     * Consulta administrativa de execuções.
     *
     * Somente ADMIN pode acessar.
     */

    @Test
    void deveBloquearListagemDeExecucoesSemToken()
            throws Exception {

        mockMvc.perform(get("/api/admin/executions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBloquearDetalhamentoDeExecucaoSemToken()
            throws Exception {

        mockMvc.perform(get("/api/admin/executions/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAdministradorListarExecucoes()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        when(executionQueryService.findAll(
                null, null, null, null, null, 0, 20
        )).thenReturn(List.of());

        when(executionQueryService.count(
                null, null, null, null, null
        )).thenReturn(0L);

        mockMvc.perform(
                        get("/api/admin/executions")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void devePermitirAdministradorConsultarExecucaoPorId()
            throws Exception {

        mockToken("token-admin", "admin", "A");

        Execution execution = new Execution();
        execution.setId(1L);
        execution.setIntegrationId(10L);
        execution.setEndpointId(20L);
        execution.setExecutionStatus("SUCCESS");
        execution.setHttpStatus(200);

        when(executionQueryService.findById(1L))
                .thenReturn(Optional.of(execution));

        mockMvc.perform(
                        get("/api/admin/executions/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.integrationId").value(10))
                .andExpect(jsonPath("$.executionStatus").value("SUCCESS"));
    }

    @Test
    void deveBloquearCriadorAoListarExecucoes()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        get("/api/admin/executions")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearCriadorAoConsultarExecucaoPorId()
            throws Exception {

        mockToken("token-criador", "criador", "C");

        mockMvc.perform(
                        get("/api/admin/executions/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-criador"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearConsumidorAoListarExecucoes()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        get("/api/admin/executions")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearConsumidorAoConsultarExecucaoPorId()
            throws Exception {

        mockToken("token-consumidor", "consumidor", "U");

        mockMvc.perform(
                        get("/api/admin/executions/1")
                                .header(
                                        "Authorization",
                                        "Bearer token-consumidor"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearTokenInvalidoNaConsultaDeExecucoes()
            throws Exception {

        when(jwtService.isTokenValid("token-invalido"))
                .thenReturn(false);

        mockMvc.perform(
                        get("/api/admin/executions")
                                .header(
                                        "Authorization",
                                        "Bearer token-invalido"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    /*
     * Helper de autenticação JWT.
     */

    private void mockToken(
            String token,
            String username,
            String role) {

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.getUsername(token))
                .thenReturn(username);

        when(jwtService.getRole(token))
                .thenReturn(role);

        when(jwtService.getEnvironment(token))
                .thenReturn("dev");

        when(dataSourceProperties.getConnection("dev"))
                .thenReturn(
                        new DataSourceProperties.ConnectionProperties()
                );
    }
}
