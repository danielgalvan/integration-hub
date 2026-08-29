package br.com.integrationhub.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiKeyServiceTest {

    @Test
    void deveGerarApiKeyComPrefixoEFormatoSeguro() {
        ApiKeyService apiKeyService = new ApiKeyService(
                mock(PasswordEncoder.class));

        String apiKey = apiKeyService.generateApiKey();

        assertTrue(apiKey.startsWith("ihub_"));
        assertTrue(apiKey.matches("ihub_[A-Za-z0-9_-]{43}"));
    }

    @Test
    void deveDelegarHashEValidacaoAoPasswordEncoder() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApiKeyService apiKeyService = new ApiKeyService(passwordEncoder);

        when(passwordEncoder.encode("ihub_chave"))
                .thenReturn("hash-da-chave");
        when(passwordEncoder.matches("ihub_chave", "hash-da-chave"))
                .thenReturn(true);

        assertTrue("hash-da-chave".equals(
                apiKeyService.hashApiKey("ihub_chave")));
        assertTrue(apiKeyService.matches("ihub_chave", "hash-da-chave"));

        verify(passwordEncoder).encode("ihub_chave");
        verify(passwordEncoder).matches("ihub_chave", "hash-da-chave");
    }

    @Test
    void deveRejeitarChaveOuHashAusentes() {
        ApiKeyService apiKeyService = new ApiKeyService(
                mock(PasswordEncoder.class));

        assertFalse(apiKeyService.matches(null, "hash"));
        assertFalse(apiKeyService.matches("ihub_chave", null));
    }
}
