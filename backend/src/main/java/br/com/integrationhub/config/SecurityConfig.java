package br.com.integrationhub.config;

import br.com.integrationhub.security.JwtAuthenticationFilter;
import br.com.integrationhub.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService);

        return http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_FORBIDDEN
                                        )
                        )
                )

                .authorizeHttpRequests(authorize -> authorize

                        /*
                         * CORS / preflight
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        /*
                         * Login público
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        )
                        .permitAll()

                        /*
                         * Troca de senha
                         *
                         * Qualquer usuário autenticado.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/auth/password"
                        )
                        .authenticated()

                        /*
                         * Health público
                         */
                        .requestMatchers("/api/health")
                        .permitAll()

                        /*
                         * Documentação
                         */
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()

                        /*
                         * Usuários
                         *
                         * Somente administrador.
                         */
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        /*
                         * Integrações
                         *
                         * ADMIN / CREATOR:
                         * CRUD completo
                         *
                         * CONSUMER:
                         * somente leitura
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/integrations/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR",
                                "CONSUMER"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/integrations/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/integrations/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/integrations/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR"
                        )

                        /*
                         * Endpoints
                         *
                         * ADMIN / CREATOR:
                         * CRUD completo
                         *
                         * CONSUMER:
                         * somente leitura
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/endpoints/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR",
                                "CONSUMER"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/endpoints/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/endpoints/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/endpoints/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR"
                        )

                        /*
                         * Endpoints dinâmicos.
                         *
                         * Todos os perfis autenticados
                         * podem executar/testar.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CREATOR",
                                "CONSUMER"
                        )

                        /*
                         * Qualquer outra rota exige
                         * autenticação.
                         */
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}
