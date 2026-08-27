package br.com.integrationhub.security;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        filter = new JwtAuthenticationFilter(jwtService);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveSeguirSemAutenticacaoQuandoHeaderNaoExiste()
            throws Exception {

        filter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
        verify(filterChain).doFilter(request, response);
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

        filter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        assertEquals("admin", authentication.getPrincipal());
        assertEquals(
                "ROLE_ADMIN",
                authentication.getAuthorities().iterator().next()
                        .getAuthority()
        );
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarTokenInvalido() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-invalido");
        when(jwtService.isTokenValid("token-invalido"))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
        verify(filterChain).doFilter(request, response);
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
        verify(filterChain).doFilter(request, response);
    }
}
