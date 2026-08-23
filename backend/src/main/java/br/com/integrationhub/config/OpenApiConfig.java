package br.com.integrationhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI integrationHubOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Integration Hub API")
                                .description(
                                        "API para criação, gerenciamento "
                                                + "e execução dinâmica de integrações"
                                )
                                .version("v1")
                );
    }
}