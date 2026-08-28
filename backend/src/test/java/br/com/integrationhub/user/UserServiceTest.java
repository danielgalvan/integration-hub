package br.com.integrationhub.user;

import br.com.integrationhub.user.dto.UserCreateRequest;
import br.com.integrationhub.user.dto.UserCreateResponse;
import br.com.integrationhub.user.dto.UserResponse;
import br.com.integrationhub.user.dto.UserUpdateRequest;
import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.repository.UserRepository;
import br.com.integrationhub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {

        userService = new UserService(
                userRepository,
                passwordEncoder
        );
    }

    @Test
    void deveRetornarTodosOsUsuarios() {

        when(userRepository.findAll())
                .thenReturn(
                        List.of(
                                createUser(
                                        1L,
                                        "admin",
                                        "Administrador",
                                        "admin@email.com",
                                        "$hash",
                                        "A",
                                        "A",
                                        "N"
                                )
                        )
                );

        List<User> users =
                userService.findAll();

        assertEquals(
                1,
                users.size()
        );

        assertEquals(
                "admin",
                users.getFirst().username()
        );
    }

    @Test
    void deveBuscarUsuarioPorId() {

        User user = createUser(
                1L,
                "admin",
                "Administrador",
                "admin@email.com",
                "$hash",
                "A",
                "A",
                "N"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                userService.findById(1L);

        assertTrue(result.isPresent());

        assertEquals(
                "admin",
                result.get().username()
        );
    }

    @Test
    void deveBuscarUsuarioPorUsername() {

        User user = createUser(
                1L,
                "admin",
                "Administrador",
                "admin@email.com",
                "$hash",
                "A",
                "A",
                "N"
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                userService.findByUsername("admin");

        assertTrue(result.isPresent());

        assertEquals(
                1L,
                result.get().id()
        );
    }

    @Test
    void deveRetornarQuantidadeDeUsuarios() {

        when(userRepository.count())
                .thenReturn(3L);

        assertEquals(
                3L,
                userService.count()
        );
    }

    @Test
    void deveCriarUsuarioParaBootstrap() {

        when(userRepository.existsByUsername("admin"))
                .thenReturn(false);

        when(userRepository.existsByEmail("admin@email.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("admin"))
                .thenReturn("$hash");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User user =
                            invocation.getArgument(0);

                    return new User(
                            1L,
                            user.username(),
                            user.name(),
                            user.email(),
                            user.password(),
                            user.status(),
                            user.type(),
                            user.passwordChangeRequired(),
                            LocalDateTime.now(),
                            null
                    );
                });

        User result = userService.create(
                " admin ",
                " Administrador ",
                " admin@email.com ",
                "admin",
                "A",
                "A"
        );

        assertEquals(
                "admin",
                result.username()
        );

        assertEquals(
                "Administrador",
                result.name()
        );

        assertEquals(
                "admin@email.com",
                result.email()
        );

        assertEquals(
                "$hash",
                result.password()
        );

        assertEquals(
                "S",
                result.passwordChangeRequired()
        );
    }

    @Test
    void deveCriarUsuarioAdministrativoComSenhaTemporaria() {

        UserCreateRequest request =
                new UserCreateRequest(
                        "joao",
                        "João da Silva",
                        "joao@email.com",
                        "C"
                );

        when(userRepository.existsByUsername("joao"))
                .thenReturn(false);

        when(userRepository.existsByEmail("joao@email.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(any()))
                .thenReturn("$hash-temporario");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User user =
                            invocation.getArgument(0);

                    return new User(
                            2L,
                            user.username(),
                            user.name(),
                            user.email(),
                            user.password(),
                            user.status(),
                            user.type(),
                            user.passwordChangeRequired(),
                            LocalDateTime.now(),
                            null
                    );
                });

        UserCreateResponse response =
                userService.create(request);

        assertNotNull(
                response.temporaryPassword()
        );

        assertEquals(
                10,
                response.temporaryPassword().length()
        );

        assertEquals(
                "joao",
                response.user().username()
        );

        assertEquals(
                "A",
                response.user().status()
        );

        assertEquals(
                "C",
                response.user().type()
        );

        assertTrue(
                response.user()
                        .passwordChangeRequired()
        );

        verify(passwordEncoder)
                .encode(
                        response.temporaryPassword()
                );
    }

    @Test
    void deveGerarSenhasTemporariasDiferentes() {

        UserCreateRequest request1 =
                new UserCreateRequest(
                        "user1",
                        "Usuário 1",
                        null,
                        "U"
                );

        UserCreateRequest request2 =
                new UserCreateRequest(
                        "user2",
                        "Usuário 2",
                        null,
                        "U"
                );

        when(userRepository.existsByUsername(any()))
                .thenReturn(false);

        when(passwordEncoder.encode(any()))
                .thenReturn("$hash");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User user =
                            invocation.getArgument(0);

                    return new User(
                            1L,
                            user.username(),
                            user.name(),
                            user.email(),
                            user.password(),
                            user.status(),
                            user.type(),
                            user.passwordChangeRequired(),
                            LocalDateTime.now(),
                            null
                    );
                });

        UserCreateResponse first =
                userService.create(request1);

        UserCreateResponse second =
                userService.create(request2);

        assertNotEquals(
                first.temporaryPassword(),
                second.temporaryPassword()
        );
    }

    @Test
    void deveRejeitarUsernameDuplicadoNaCriacao() {

        when(userRepository.existsByUsername("admin"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.create(
                                new UserCreateRequest(
                                        "admin",
                                        "Administrador",
                                        null,
                                        "A"
                                )
                        )
                );

        assertEquals(
                "Usuário já cadastrado",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarEmailDuplicadoNaCriacao() {

        when(userRepository.existsByUsername("joao"))
                .thenReturn(false);

        when(userRepository.existsByEmail("joao@email.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.create(
                                new UserCreateRequest(
                                        "joao",
                                        "João",
                                        "joao@email.com",
                                        "U"
                                )
                        )
                );

        assertEquals(
                "E-mail já cadastrado",
                exception.getMessage()
        );
    }

    @Test
    void deveAtualizarUsuario() {

        User current = createUser(
                1L,
                "joao",
                "João",
                "joao@email.com",
                "$hash",
                "A",
                "U",
                "N"
        );

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "joao.novo",
                        "João Novo",
                        "novo@email.com",
                        "I",
                        "C"
                );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(current)
                );

        when(
                userRepository.findByUsername(
                        "joao.novo"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(userRepository.findAll())
                .thenReturn(
                        List.of(current)
                );

        when(userRepository.update(any(User.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        UserResponse response =
                userService.update(
                        1L,
                        request
                );

        assertEquals(
                "joao.novo",
                response.username()
        );

        assertEquals(
                "João Novo",
                response.name()
        );

        assertEquals(
                "novo@email.com",
                response.email()
        );

        assertEquals(
                "I",
                response.status()
        );

        assertEquals(
                "C",
                response.type()
        );

        assertFalse(
                response.passwordChangeRequired()
        );
    }

    @Test
    void deveManterSenhaNaAtualizacaoDoUsuario() {

        User current = createUser(
                1L,
                "joao",
                "João",
                null,
                "$hash-original",
                "A",
                "U",
                "S"
        );

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "joao",
                        "João Atualizado",
                        null,
                        "A",
                        "C"
                );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(current)
                );

        when(
                userRepository.findByUsername(
                        "joao"
                )
        ).thenReturn(
                Optional.of(current)
        );

        when(userRepository.update(any(User.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        userService.update(
                1L,
                request
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(
                        User.class
                );

        verify(userRepository)
                .update(
                        captor.capture()
                );

        assertEquals(
                "$hash-original",
                captor.getValue().password()
        );

        assertEquals(
                "S",
                captor.getValue()
                        .passwordChangeRequired()
        );
    }

    @Test
    void deveRejeitarAtualizacaoDeUsuarioInexistente() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.update(
                                99L,
                                new UserUpdateRequest(
                                        "usuario",
                                        "Usuário",
                                        null,
                                        "A",
                                        "U"
                                )
                        )
                );

        assertEquals(
                "Usuário não encontrado",
                exception.getMessage()
        );
    }

    @Test
    void deveRejeitarUsernameDeOutroUsuarioNaAtualizacao() {

        User current = createUser(
                1L,
                "joao",
                "João",
                null,
                "$hash",
                "A",
                "U",
                "N"
        );

        User another = createUser(
                2L,
                "maria",
                "Maria",
                null,
                "$hash",
                "A",
                "U",
                "N"
        );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(current)
                );

        when(
                userRepository.findByUsername(
                        "maria"
                )
        ).thenReturn(
                Optional.of(another)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.update(
                                1L,
                                new UserUpdateRequest(
                                        "maria",
                                        "João",
                                        null,
                                        "A",
                                        "U"
                                )
                        )
                );

        assertEquals(
                "Usuário já cadastrado",
                exception.getMessage()
        );
    }

    @Test
    void deveRejeitarEmailDeOutroUsuarioNaAtualizacao() {

        User current = createUser(
                1L,
                "joao",
                "João",
                "joao@email.com",
                "$hash",
                "A",
                "U",
                "N"
        );

        User another = createUser(
                2L,
                "maria",
                "Maria",
                "maria@email.com",
                "$hash",
                "A",
                "U",
                "N"
        );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(current)
                );

        when(
                userRepository.findByUsername(
                        "joao"
                )
        ).thenReturn(
                Optional.of(current)
        );

        when(userRepository.findAll())
                .thenReturn(
                        List.of(
                                current,
                                another
                        )
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.update(
                                1L,
                                new UserUpdateRequest(
                                        "joao",
                                        "João",
                                        "maria@email.com",
                                        "A",
                                        "U"
                                )
                        )
                );

        assertEquals(
                "E-mail já cadastrado",
                exception.getMessage()
        );
    }

    @Test
    void deveExcluirUsuario() {

        User user = createUser(
                1L,
                "joao",
                "João",
                null,
                "$hash",
                "A",
                "U",
                "N"
        );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(user)
                );

        userService.delete(1L);

        verify(userRepository)
                .deleteById(1L);
    }

    @Test
    void deveRejeitarExclusaoDeUsuarioInexistente() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.delete(99L)
                );

        assertEquals(
                "Usuário não encontrado",
                exception.getMessage()
        );
    }

    @Test
    void deveAlterarSenhaEFinalizarTrocaObrigatoria() {

        User user = createUser(
                1L,
                "joao",
                "João",
                null,
                "$hash-antigo",
                "A",
                "U",
                "S"
        );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(user)
                );

        when(
                passwordEncoder.encode(
                        "novaSenha"
                )
        ).thenReturn(
                "$hash-novo"
        );

        when(userRepository.update(any(User.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        User updated =
                userService.changePassword(
                        1L,
                        "novaSenha"
                );

        assertEquals(
                "$hash-novo",
                updated.password()
        );

        assertEquals(
                "N",
                updated.passwordChangeRequired()
        );
    }

    @Test
    void deveResetarSenhaEExigirNovaTroca() {

        User user = createUser(
                1L,
                "joao",
                "João",
                null,
                "$hash-antigo",
                "A",
                "U",
                "N"
        );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(user)
                );

        when(passwordEncoder.encode(any()))
                .thenReturn(
                        "$hash-temporario"
                );

        when(userRepository.update(any(User.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        String temporaryPassword =
                userService.resetPassword(1L);

        assertNotNull(
                temporaryPassword
        );

        assertEquals(
                10,
                temporaryPassword.length()
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(
                        User.class
                );

        verify(userRepository)
                .update(
                        captor.capture()
                );

        assertEquals(
                "$hash-temporario",
                captor.getValue().password()
        );

        assertEquals(
                "S",
                captor.getValue()
                        .passwordChangeRequired()
        );

        verify(passwordEncoder)
                .encode(
                        temporaryPassword
                );
    }

    @Test
    void deveValidarSenhaDoUsuario() {

        User user = createUser(
                1L,
                "admin",
                "Administrador",
                null,
                "$hash",
                "A",
                "A",
                "N"
        );

        when(
                passwordEncoder.matches(
                        "senha",
                        "$hash"
                )
        ).thenReturn(true);

        assertTrue(
                userService.passwordMatches(
                        "senha",
                        user
                )
        );
    }

    @Test
    void deveConverterUsuarioParaResponseSemSenha() {

        User user = createUser(
                1L,
                "admin",
                "Administrador",
                "admin@email.com",
                "$hash-super-secreto",
                "A",
                "A",
                "S"
        );

        UserResponse response =
                userService.toResponse(user);

        assertEquals(
                1L,
                response.id()
        );

        assertEquals(
                "admin",
                response.username()
        );

        assertTrue(
                response.passwordChangeRequired()
        );
    }

    private User createUser(
            Long id,
            String username,
            String name,
            String email,
            String password,
            String status,
            String type,
            String passwordChangeRequired
    ) {

        return new User(
                id,
                username,
                name,
                email,
                password,
                status,
                type,
                passwordChangeRequired,
                LocalDateTime.now(),
                null
        );
    }
}
