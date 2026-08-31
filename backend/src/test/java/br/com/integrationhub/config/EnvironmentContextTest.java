package br.com.integrationhub.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvironmentContextTest {

    @AfterEach
    void tearDown() {
        EnvironmentContext.clear();
    }

    @Test
    void deveArmazenarERemoverAmbienteDaThreadAtual() {
        EnvironmentContext.set("homolog");

        assertEquals("homolog", EnvironmentContext.get());

        EnvironmentContext.clear();

        assertNull(EnvironmentContext.get());
    }
}
