package br.com.integrationhub.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ApiKeyService {

    private static final String PREFIX = "ihub_";
    private static final int KEY_SIZE = 32;

    private final SecureRandom secureRandom;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyService(
            PasswordEncoder passwordEncoder) {
        this.secureRandom = new SecureRandom();
        this.passwordEncoder = passwordEncoder;
    }

    public String generateApiKey() {

        byte[] randomBytes = new byte[KEY_SIZE];

        secureRandom.nextBytes(randomBytes);

        String key = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        return PREFIX + key;
    }

    public String hashApiKey(
            String apiKey) {
        return passwordEncoder.encode(apiKey);
    }

    public boolean matches(
            String apiKey,
            String apiKeyHash) {
        if (apiKey == null ||
                apiKeyHash == null) {
            return false;
        }

        return passwordEncoder.matches(
                apiKey,
                apiKeyHash);
    }
}
