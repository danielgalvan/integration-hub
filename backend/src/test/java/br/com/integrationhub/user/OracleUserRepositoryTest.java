package br.com.integrationhub.user;

import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.repository.OracleUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleUserRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private OracleUserRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        repository = new OracleUserRepository(jdbcTemplate);
    }

    @Test
    void deveMapearUsuariosAoListarTodos() throws Exception {
        ArgumentCaptor<RowMapper<User>> mapperCaptor =
                createMapperCaptor();

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<User>>any()
        )).thenReturn(List.of());

        repository.findAll();

        verify(jdbcTemplate).query(
                anyString(),
                mapperCaptor.capture()
        );

        ResultSet resultSet = mock(ResultSet.class);
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 28, 10, 30);
        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 8, 28, 11, 45);

        when(resultSet.getLong("nr_sequencia"))
                .thenReturn(9L);
        when(resultSet.getString("nm_usuario"))
                .thenReturn("daniel");
        when(resultSet.getString("ds_usuario"))
                .thenReturn("Daniel Galvan");
        when(resultSet.getString("ds_email"))
                .thenReturn("daniel@example.com");
        when(resultSet.getString("ds_senha"))
                .thenReturn("hash");
        when(resultSet.getString("ie_situacao"))
                .thenReturn("A");
        when(resultSet.getString("ie_tipo_usuario"))
                .thenReturn("A");
        when(resultSet.getString("ie_trocar_senha"))
                .thenReturn("N");
        when(resultSet.getTimestamp("dt_criacao"))
                .thenReturn(Timestamp.valueOf(createdAt));
        when(resultSet.getTimestamp("dt_atualizacao"))
                .thenReturn(Timestamp.valueOf(updatedAt));

        User user = mapperCaptor.getValue()
                .mapRow(resultSet, 0);

        assertEquals(9L, user.id());
        assertEquals("daniel", user.username());
        assertEquals("Daniel Galvan", user.name());
        assertEquals("daniel@example.com", user.email());
        assertEquals("hash", user.password());
        assertEquals("A", user.status());
        assertEquals("A", user.type());
        assertEquals("N", user.passwordChangeRequired());
        assertEquals(createdAt, user.createdAt());
        assertEquals(updatedAt, user.updatedAt());
    }

    @Test
    void deveBuscarUsuarioPorIdComParametroNomeado() {
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class),
                ArgumentMatchers.<RowMapper<User>>any()
        )).thenReturn(List.of());

        Optional<User> result = repository.findById(15L);

        ArgumentCaptor<MapSqlParameterSource> parametersCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).query(
                anyString(),
                parametersCaptor.capture(),
                ArgumentMatchers.<RowMapper<User>>any()
        );

        assertTrue(result.isEmpty());
        assertEquals(
                15L,
                parametersCaptor.getValue().getValue("id")
        );
    }

    @Test
    void deveMapearAtualizacaoNulaQuandoUsuarioAindaNaoFoiAlterado()
            throws Exception {
        ArgumentCaptor<RowMapper<User>> mapperCaptor =
                createMapperCaptor();

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<User>>any()
        )).thenReturn(List.of());

        repository.findAll();

        verify(jdbcTemplate).query(
                anyString(),
                mapperCaptor.capture()
        );

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getTimestamp("dt_criacao"))
                .thenReturn(Timestamp.valueOf(
                        LocalDateTime.of(2026, 8, 29, 10, 0)
                ));
        when(resultSet.getTimestamp("dt_atualizacao"))
                .thenReturn(null);

        User user = mapperCaptor.getValue().mapRow(resultSet, 0);

        assertNull(user.updatedAt());
    }

    @Test
    void deveBuscarUsuarioPorUsernameSemDiferenciarMaiusculas() {
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class),
                ArgumentMatchers.<RowMapper<User>>any()
        )).thenReturn(List.of());

        Optional<User> result = repository.findByUsername("Daniel");

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parametersCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                parametersCaptor.capture(),
                ArgumentMatchers.<RowMapper<User>>any()
        );

        assertTrue(result.isEmpty());
        assertTrue(
                sqlCaptor.getValue()
                        .replaceAll("\\s+", " ")
                        .toLowerCase()
                        .contains("upper(nm_usuario) = upper(:username)")
        );
        assertEquals(
                "Daniel",
                parametersCaptor.getValue()
                        .getValue("username")
        );
    }

    @Test
    void deveSalvarUsuarioERecuperarRegistroCriado() {
        User user = createUser(null, "daniel", "S");
        User savedUser = createUser(18L, "daniel", "S");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(1);
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class),
                ArgumentMatchers.<RowMapper<User>>any()
        )).thenReturn(List.of(savedUser));

        User result = repository.save(user);

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parametersCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                parametersCaptor.capture()
        );

        assertTrue(
                sqlCaptor.getValue()
                        .replaceAll("\\s+", " ")
                        .toLowerCase()
                        .startsWith("insert into ih_users")
        );
        assertEquals(
                "daniel",
                parametersCaptor.getValue()
                        .getValue("username")
        );
        assertEquals("hash", parametersCaptor.getValue().getValue("password"));
        assertEquals("S", parametersCaptor.getValue()
                .getValue("passwordChangeRequired"));
        assertEquals(savedUser, result);
    }

    @Test
    void deveAtualizarUsuarioERecuperarRegistroAtualizado() {
        User user = createUser(18L, "daniel", "N");

        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(1);
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class),
                ArgumentMatchers.<RowMapper<User>>any()
        )).thenReturn(List.of(user));

        User result = repository.update(user);

        ArgumentCaptor<MapSqlParameterSource> parametersCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                anyString(),
                parametersCaptor.capture()
        );

        assertEquals(18L, parametersCaptor.getValue().getValue("id"));
        assertEquals("N", parametersCaptor.getValue()
                .getValue("passwordChangeRequired"));
        assertEquals(user, result);
    }

    @Test
    void deveRejeitarAtualizacaoDeUsuarioInexistente() {
        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.update(
                        createUser(18L, "daniel", "N")
                )
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void deveExcluirUsuarioPeloId() {
        when(jdbcTemplate.update(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class)
        )).thenReturn(1);

        repository.deleteById(18L);

        ArgumentCaptor<MapSqlParameterSource> parametersCaptor =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(jdbcTemplate).update(
                anyString(),
                parametersCaptor.capture()
        );

        assertEquals(18L, parametersCaptor.getValue().getValue("id"));
    }

    @Test
    void deveConsultarQuantidadeEExistenciaDeUsuarioEEmail() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                ArgumentMatchers.any(MapSqlParameterSource.class),
                ArgumentMatchers.<Class<Long>>any()
        )).thenReturn(2L, 1L, 0L);

        long count = repository.count();
        boolean usernameExists = repository.existsByUsername("daniel");
        boolean emailExists = repository.existsByEmail("daniel@example.com");

        assertEquals(2L, count);
        assertTrue(usernameExists);
        assertTrue(!emailExists);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<RowMapper<User>> createMapperCaptor() {
        return ArgumentCaptor.forClass(
                (Class<RowMapper<User>>) (Class<?>) RowMapper.class
        );
    }

    private User createUser(
            Long id,
            String username,
            String passwordChangeRequired
    ) {
        return new User(
                id,
                username,
                "Daniel Galvan",
                "daniel@example.com",
                "hash",
                "A",
                "A",
                passwordChangeRequired,
                LocalDateTime.of(2026, 8, 28, 10, 30),
                null
        );
    }
}
