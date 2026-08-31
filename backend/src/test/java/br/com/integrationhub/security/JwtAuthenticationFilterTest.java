package br.com.integrationhub.security;

import br.com.integrationhub.config.DataSourceProperties;
import br.com.integrationhub.config.EnvironmentContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private DataSourceProperties dataSourceProperties;
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        dataSourceProperties = Mockito.mock(DataSourceProperties.class);
        filter = new JwtAuthenticationFilter(
                jwtService,
                dataSourceProperties);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.clearContext();
        EnvironmentContext.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        EnvironmentContext.clear();
    }

    @Test
    void deveSeguirSemAutenticacaoQuandoHeaderNaoExiste()
            throws Exception {

        filter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void deveAutenticarTokenValidoComoAdministrador()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-valido");

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("admin");

        when(jwtService.getRole("token-valido"))
                .thenReturn("A");

        mockConfiguredEnvironment();

        filter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertEquals(
                "admin",
                authentication.getPrincipal()
        );

        assertEquals(
                "ROLE_ADMIN",
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void deveAutenticarTokenValidoComoCriador()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-valido");

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("criador");

        when(jwtService.getRole("token-valido"))
                .thenReturn("C");

        mockConfiguredEnvironment();

        filter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertEquals(
                "criador",
                authentication.getPrincipal()
        );

        assertEquals(
                "ROLE_CREATOR",
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void deveAutenticarTokenValidoComoConsumidor()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-valido");

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("consumidor");

        when(jwtService.getRole("token-valido"))
                .thenReturn("U");

        mockConfiguredEnvironment();

        filter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertEquals(
                "consumidor",
                authentication.getPrincipal()
        );

        assertEquals(
                "ROLE_CONSUMER",
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarTokenInvalido()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-invalido");

        when(jwtService.isTokenValid("token-invalido"))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void deveIgnorarHeaderAuthorizationMalformado()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic credencial");

        filter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void deveRejeitarTipoUsuarioInvalido()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-valido");

        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);

        when(jwtService.getUsername("token-valido"))
                .thenReturn("usuario");

        when(jwtService.getRole("token-valido"))
                .thenReturn("X");

        mockConfiguredEnvironment();

        assertThrows(
                IllegalArgumentException.class,
                () -> filter.doFilter(
                        request,
                        response,
                        filterChain
                )
        );
    }

    @Test
    void naoDeveAutenticarTokenDeAmbienteNaoConfigurado()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-valido");
        when(jwtService.isTokenValid("token-valido"))
                .thenReturn(true);
        when(jwtService.getEnvironment("token-valido"))
                .thenReturn("inexistente");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(EnvironmentContext.get());
        verify(filterChain).doFilter(request, response);
    }

    private void mockConfiguredEnvironment() {
        when(jwtService.getEnvironment("token-valido"))
                .thenReturn("dev");
        when(dataSourceProperties.getConnection("dev"))
                .thenReturn(mock(DataSourceProperties.ConnectionProperties.class));
    }
}
