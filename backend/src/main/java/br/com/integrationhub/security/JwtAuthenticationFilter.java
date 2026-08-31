package br.com.integrationhub.security;

import br.com.integrationhub.config.DataSourceProperties;
import br.com.integrationhub.config.EnvironmentContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final DataSourceProperties dataSourceProperties;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            DataSourceProperties dataSourceProperties) {

        this.jwtService = jwtService;
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authorizationHeader =
                    request.getHeader(
                            "Authorization"
                    );

            if (authorizationHeader == null
                    || !authorizationHeader
                    .startsWith("Bearer ")) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            String token =
                    authorizationHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            String username =
                    jwtService.getUsername(token);

            String role =
                    jwtService.getRole(token);

            String environment =
                    jwtService.getEnvironment(token);

            /*
             * O ambiente armazenado no JWT precisa
             * continuar existindo na configuração.
             */
            if (dataSourceProperties
                    .getConnection(environment) == null) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * Define o datasource que será utilizado
             * durante esta requisição.
             */
            EnvironmentContext.set(
                    environment
            );

            String authority =
                    mapAuthority(role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            authority
                                    )
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

            filterChain.doFilter(
                    request,
                    response
            );

        } finally {

            /*
             * Como o servidor reutiliza threads,
             * o contexto precisa sempre ser removido
             * ao final da requisição.
             */
            EnvironmentContext.clear();
        }
    }

    private String mapAuthority(
            String role) {

        return switch (role) {
            case "A" -> "ROLE_ADMIN";
            case "C" -> "ROLE_CREATOR";
            case "U" -> "ROLE_CONSUMER";

            default ->
                    throw new IllegalArgumentException(
                            "Tipo de usuário inválido"
                    );
        };
    }
}
