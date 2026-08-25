package br.com.integrationhub.integration.service;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.repository.EndpointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EndpointServiceTest {

    private EndpointRepository endpointRepository;
    private EndpointService endpointService;

    @BeforeEach
    void setUp() {
        endpointRepository = mock(EndpointRepository.class);

        endpointService = new EndpointService(
                endpointRepository
        );
    }

    @Test
    void deveListarTodosOsEndpoints() {

        Endpoint endpoint1 = createEndpoint(
                1L,
                "/buscar"
        );

        Endpoint endpoint2 = createEndpoint(
                1L,
                "/listar"
        );

        when(endpointRepository.findAll())
                .thenReturn(
                        List.of(
                                endpoint1,
                                endpoint2
                        )
                );

        List<Endpoint> result =
                endpointService.findAll();

        assertEquals(2, result.size());
        assertEquals(
                endpoint1,
                result.get(0)
        );
        assertEquals(
                endpoint2,
                result.get(1)
        );

        verify(endpointRepository).findAll();
    }

    @Test
    void deveBuscarEndpointPorId() {

        Endpoint endpoint = createEndpoint(
                1L,
                "/buscar"
        );

        endpoint.setId(10L);

        when(endpointRepository.findById(10L))
                .thenReturn(Optional.of(endpoint));

        Optional<Endpoint> result =
                endpointService.findById(10L);

        assertTrue(result.isPresent());
        assertEquals(
                endpoint,
                result.get()
        );

        verify(endpointRepository)
                .findById(10L);
    }

    @Test
    void deveRetornarOptionalVazioQuandoEndpointNaoExistir() {

        when(endpointRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Endpoint> result =
                endpointService.findById(99L);

        assertFalse(result.isPresent());

        verify(endpointRepository)
                .findById(99L);
    }

    @Test
    void deveListarEndpointsPorIntegrationId() {

        Endpoint endpoint1 = createEndpoint(
                8L,
                "/buscar"
        );

        Endpoint endpoint2 = createEndpoint(
                8L,
                "/listar"
        );

        when(
                endpointRepository.findByIntegrationId(
                        8L
                )
        ).thenReturn(
                List.of(
                        endpoint1,
                        endpoint2
                )
        );

        List<Endpoint> result =
                endpointService.findByIntegrationId(
                        8L
                );

        assertEquals(2, result.size());

        verify(endpointRepository)
                .findByIntegrationId(8L);
    }

    @Test
    void deveBuscarEndpointPorIntegracaoPathEMethod() {

        Endpoint endpoint = createEndpoint(
                8L,
                "/buscar"
        );

        when(
                endpointRepository
                        .findByIntegrationIdAndPathAndMethod(
                                8L,
                                "/buscar",
                                "GET"
                        )
        ).thenReturn(Optional.of(endpoint));

        Optional<Endpoint> result =
                endpointService
                        .findByIntegrationIdAndPathAndMethod(
                                8L,
                                "/buscar",
                                "GET"
                        );

        assertTrue(result.isPresent());
        assertEquals(
                endpoint,
                result.get()
        );

        verify(endpointRepository)
                .findByIntegrationIdAndPathAndMethod(
                        8L,
                        "/buscar",
                        "GET"
                );
    }

    @Test
    void deveSalvarEndpoint() {

        Endpoint endpoint = createEndpoint(
                8L,
                "/buscar"
        );

        when(endpointRepository.save(endpoint))
                .thenReturn(endpoint);

        Endpoint result =
                endpointService.save(endpoint);

        assertEquals(
                endpoint,
                result
        );

        verify(endpointRepository)
                .save(endpoint);
    }

    @Test
    void deveDefinirActiveComoSQuandoNaoInformado() {

        Endpoint endpoint = createEndpoint(
                8L,
                "/buscar"
        );

        endpoint.setActive(null);

        when(endpointRepository.save(endpoint))
                .thenReturn(endpoint);

        Endpoint result =
                endpointService.save(endpoint);

        assertEquals(
                "S",
                result.getActive()
        );

        verify(endpointRepository)
                .save(endpoint);
    }

    @Test
    void deveDefinirCreatedByComoSystemQuandoNaoInformado() {

        Endpoint endpoint = createEndpoint(
                8L,
                "/buscar"
        );

        endpoint.setCreatedBy(null);

        when(endpointRepository.save(endpoint))
                .thenReturn(endpoint);

        Endpoint result =
                endpointService.save(endpoint);

        assertEquals(
                "SYSTEM",
                result.getCreatedBy()
        );

        verify(endpointRepository)
                .save(endpoint);
    }

    @Test
    void deveManterActiveQuandoInformado() {

        Endpoint endpoint = createEndpoint(
                8L,
                "/buscar"
        );

        endpoint.setActive("N");

        when(endpointRepository.save(endpoint))
                .thenReturn(endpoint);

        Endpoint result =
                endpointService.save(endpoint);

        assertEquals(
                "N",
                result.getActive()
        );
    }

    @Test
    void deveManterCreatedByQuandoInformado() {

        Endpoint endpoint = createEndpoint(
                8L,
                "/buscar"
        );

        endpoint.setCreatedBy("TEST");

        when(endpointRepository.save(endpoint))
                .thenReturn(endpoint);

        Endpoint result =
                endpointService.save(endpoint);

        assertEquals(
                "TEST",
                result.getCreatedBy()
        );
    }

    @Test
    void deveExcluirEndpointExistente() {

        Endpoint endpoint = createEndpoint(
                8L,
                "/buscar"
        );

        endpoint.setId(12L);

        when(endpointRepository.findById(12L))
                .thenReturn(Optional.of(endpoint));

        endpointService.delete(12L);

        verify(endpointRepository)
                .findById(12L);

        verify(endpointRepository)
                .deleteById(12L);
    }

    @Test
    void deveRetornarNotFoundAoExcluirEndpointInexistente() {

        when(endpointRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> endpointService.delete(99L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Endpoint não encontrado",
                exception.getReason()
        );

        verify(endpointRepository)
                .findById(99L);

        verify(
                endpointRepository,
                never()
        ).deleteById(99L);
    }

    private Endpoint createEndpoint(
            Long integrationId,
            String path) {

        Endpoint endpoint = new Endpoint();

        endpoint.setIntegrationId(integrationId);
        endpoint.setName("Endpoint de teste");
        endpoint.setDescription(
                "Endpoint utilizado nos testes"
        );
        endpoint.setPath(path);
        endpoint.setMethod("GET");
        endpoint.setSqlText(
                "select id from pedido where id = :id"
        );
        endpoint.setActive("S");
        endpoint.setCreatedBy("SYSTEM");

        return endpoint;
    }
}