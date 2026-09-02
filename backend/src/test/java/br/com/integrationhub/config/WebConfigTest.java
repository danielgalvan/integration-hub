package br.com.integrationhub.config;

import br.com.integrationhub.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebConfigTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "integration-hub.cors.allowed-origins=http://localhost:5175"
})
class WebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void devePermitirCorsParaFrontendLocal() throws Exception {
        mockMvc.perform(
                        options("/test")
                                .header(
                                        "Origin",
                                        "http://localhost:5175"
                                )
                                .header(
                                        "Access-Control-Request-Method",
                                        "GET"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                "Access-Control-Allow-Origin",
                                "http://localhost:5175"
                        )
                );
    }

    @RestController
    static class TestController {

        @GetMapping("/test")
        String test() {
            return "OK";
        }
    }
}
