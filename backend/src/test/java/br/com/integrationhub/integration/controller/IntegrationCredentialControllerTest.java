package br.com.integrationhub.integration.controller;

import br.com.integrationhub.integration.model.IntegrationCredential;
import br.com.integrationhub.integration.service.IntegrationCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IntegrationCredentialControllerTest {

    private IntegrationCredentialService credentialService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        credentialService =
                mock(IntegrationCredentialService.class);

        IntegrationCredentialController controller =
                new IntegrationCredentialController(
                        credentialService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

    }

    @Test
    void deveListarCredenciaisSemExporApiKeyEncrypted()
            throws Exception {

        IntegrationCredential credential =
                createCredential();

        when(credentialService.findByIntegrationId(1L))
                .thenReturn(List.of(credential));

        mockMvc.perform(
                        get(
                                "/api/integrations/1/credentials"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(
                        jsonPath("$[0].id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$[0].integrationId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Sistema Tasy")
                )
                .andExpect(
                        jsonPath("$[0].active")
                                .value("S")
                )
                .andExpect(
                        jsonPath(
                                "$[0].apiKeyEncrypted"
                        ).doesNotExist()
                );

        verify(credentialService)
                .findByIntegrationId(1L);
    }

    @Test
    void deveBuscarCredencialSemExporApiKeyEncrypted()
            throws Exception {

        IntegrationCredential credential =
                createCredential();

        when(credentialService.findById(
                1L,
                10L
        )).thenReturn(credential);

        mockMvc.perform(
                        get(
                                "/api/integrations/1/credentials/10"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.integrationId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Sistema Tasy")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value("S")
                )
                .andExpect(
                        jsonPath("$.apiKeyEncrypted")
                                .doesNotExist()
                );

        verify(credentialService)
                .findById(
                        1L,
                        10L
                );
    }

    @Test
    void deveCriarCredencialEDevolverApiKey()
            throws Exception {

        IntegrationCredential credential =
                createCredential();

        when(credentialService.create(
                1L,
                "Sistema Tasy",
                "admin"
        )).thenReturn(credential);

        when(credentialService.getApiKey(
                1L,
                10L
        )).thenReturn(
                "ihub_chave-gerada"
        );

        String body = """
                {
                  "name": "Sistema Tasy"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/integrations/1/credentials"
                        )
                                .principal(
                                        authentication()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.apiKey")
                                .value(
                                        "ihub_chave-gerada"
                                )
                )
                .andExpect(
                        jsonPath("$.apiKeyEncrypted")
                                .doesNotExist()
                );

        verify(credentialService)
                .create(
                        1L,
                        "Sistema Tasy",
                        "admin"
                );

        verify(credentialService)
                .getApiKey(
                        1L,
                        10L
                );
    }

    @Test
    void deveRejeitarCriacaoSemNome()
            throws Exception {

        String body = """
                {
                  "name": ""
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/integrations/1/credentials"
                        )
                                .principal(
                                        authentication()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void deveAtualizarNomeDaCredencial()
            throws Exception {

        IntegrationCredential credential =
                createCredential();

        credential.setName(
                "Sistema Financeiro"
        );

        when(credentialService.updateName(
                1L,
                10L,
                "Sistema Financeiro",
                "admin"
        )).thenReturn(credential);

        String body = """
                {
                  "name": "Sistema Financeiro"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/integrations/1/credentials/10"
                        )
                                .principal(
                                        authentication()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(
                                        "Sistema Financeiro"
                                )
                )
                .andExpect(
                        jsonPath("$.apiKeyEncrypted")
                                .doesNotExist()
                );

        verify(credentialService)
                .updateName(
                        1L,
                        10L,
                        "Sistema Financeiro",
                        "admin"
                );
    }

    @Test
    void deveRejeitarAtualizacaoComNomeVazio()
            throws Exception {

        String body = """
                {
                  "name": ""
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/integrations/1/credentials/10"
                        )
                                .principal(
                                        authentication()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void deveDesativarCredencial()
            throws Exception {

        IntegrationCredential credential =
                createCredential();

        credential.setActive("N");

        when(credentialService.setActive(
                1L,
                10L,
                false,
                "admin"
        )).thenReturn(credential);

        String body = """
                {
                  "active": false
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/integrations/1/credentials/10/active"
                        )
                                .principal(
                                        authentication()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.active")
                                .value("N")
                )
                .andExpect(
                        jsonPath("$.apiKeyEncrypted")
                                .doesNotExist()
                );

        verify(credentialService)
                .setActive(
                        1L,
                        10L,
                        false,
                        "admin"
                );
    }

    @Test
    void deveAtivarCredencial()
            throws Exception {

        IntegrationCredential credential =
                createCredential();

        credential.setActive("S");

        when(credentialService.setActive(
                1L,
                10L,
                true,
                "admin"
        )).thenReturn(credential);

        String body = """
                {
                  "active": true
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/integrations/1/credentials/10/active"
                        )
                                .principal(
                                        authentication()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.active")
                                .value("S")
                );

        verify(credentialService)
                .setActive(
                        1L,
                        10L,
                        true,
                        "admin"
                );
    }

    @Test
    void deveRejeitarAlteracaoSemActive()
            throws Exception {

        String body = """
                {
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/integrations/1/credentials/10/active"
                        )
                                .principal(
                                        authentication()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void deveConsultarApiKey()
            throws Exception {

        when(credentialService.getApiKey(
                1L,
                10L
        )).thenReturn(
                "ihub_chave-original"
        );

        mockMvc.perform(
                        get(
                                "/api/integrations/1/credentials/10/api-key"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.apiKey")
                                .value(
                                        "ihub_chave-original"
                                )
                )
                .andExpect(
                        jsonPath("$.apiKeyEncrypted")
                                .doesNotExist()
                );

        verify(credentialService)
                .getApiKey(
                        1L,
                        10L
                );
    }

    @Test
    void deveRegenerarApiKey()
            throws Exception {

        when(credentialService.regenerateApiKey(
                1L,
                10L,
                "admin"
        )).thenReturn(
                "ihub_nova-chave"
        );

        mockMvc.perform(
                        post(
                                "/api/integrations/1/credentials/10/api-key"
                        )
                                .principal(
                                        authentication()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.apiKey")
                                .value(
                                        "ihub_nova-chave"
                                )
                )
                .andExpect(
                        jsonPath("$.apiKeyEncrypted")
                                .doesNotExist()
                );

        verify(credentialService)
                .regenerateApiKey(
                        1L,
                        10L,
                        "admin"
                );
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(
                "admin",
                null
        );
    }

    private IntegrationCredential createCredential() {
        IntegrationCredential credential =
                new IntegrationCredential();

        credential.setId(10L);
        credential.setIntegrationId(1L);
        credential.setName("Sistema Tasy");
        credential.setApiKeyEncrypted(
                "valor-criptografado-que-nao-pode-vazar"
        );
        credential.setActive("S");
        credential.setLastUsedAt(
                LocalDateTime.of(
                        2026,
                        9,
                        28,
                        14,
                        30
                )
        );
        credential.setCreatedBy("admin");
        credential.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        9,
                        28,
                        14,
                        0
                )
        );

        return credential;
    }
}
