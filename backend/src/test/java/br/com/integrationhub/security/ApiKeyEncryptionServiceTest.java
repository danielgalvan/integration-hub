package br.com.integrationhub.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyEncryptionServiceTest {

    private static final String ENCRYPTION_KEY =
            Base64.getEncoder().encodeToString(new byte[32]);

    private final ApiKeyEncryptionService service =
            new ApiKeyEncryptionService(ENCRYPTION_KEY);

    @Test
    void shouldEncryptAndDecryptApiKey() {
        String apiKey = "ihub_abc123456789";

        String encrypted = service.encrypt(apiKey);
        String decrypted = service.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(apiKey, encrypted);
        assertEquals(apiKey, decrypted);
    }

    @Test
    void shouldGenerateDifferentEncryptedValuesForSameApiKey() {
        String apiKey = "ihub_abc123456789";

        String encrypted1 = service.encrypt(apiKey);
        String encrypted2 = service.encrypt(apiKey);

        assertNotEquals(encrypted1, encrypted2);

        assertEquals(apiKey, service.decrypt(encrypted1));
        assertEquals(apiKey, service.decrypt(encrypted2));
    }

    @Test
    void shouldFailWhenEncryptionKeyIsNot256Bits() {
        String invalidKey =
                Base64.getEncoder().encodeToString(new byte[16]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ApiKeyEncryptionService(invalidKey)
        );

        assertEquals(
                "A chave de criptografia da API Key deve possuir 256 bits",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenEncryptionKeyIsNotValidBase64() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ApiKeyEncryptionService("chave-invalida")
        );
    }

    @Test
    void shouldFailWhenEncryptedValueIsInvalid() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.decrypt("valor-invalido")
        );

        assertEquals(
                "Erro ao descriptografar API Key",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenEncryptedValueWasTampered() {
        String apiKey = "ihub_abc123456789";

        String encrypted = service.encrypt(apiKey);

        byte[] bytes = Base64.getDecoder().decode(encrypted);
        bytes[bytes.length - 1] ^= 1;

        String tampered = Base64.getEncoder().encodeToString(bytes);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.decrypt(tampered)
        );

        assertEquals(
                "Erro ao descriptografar API Key",
                exception.getMessage()
        );
    }
}
