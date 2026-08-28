package br.com.integrationhub.user.service;

import br.com.integrationhub.user.dto.UserCreateRequest;
import br.com.integrationhub.user.dto.UserCreateResponse;
import br.com.integrationhub.user.dto.UserResponse;
import br.com.integrationhub.user.dto.UserUpdateRequest;
import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final String TEMP_PASSWORD_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ"
                    + "abcdefghijkmnopqrstuvwxyz"
                    + "23456789";

    private static final int TEMP_PASSWORD_LENGTH = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public long count() {
        return userRepository.count();
    }

    /*
     * Utilizado pelo bootstrap inicial.
     *
     * Aqui a senha é fornecida pela configuração
     * da instalação.
     */
    public User create(
            String username,
            String name,
            String email,
            String rawPassword,
            String status,
            String type
    ) {

        validateUsername(username);
        validateEmail(email);

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        User user = new User(
                null,
                username.trim(),
                name.trim(),
                normalizeEmail(email),
                encodedPassword,
                status,
                type,
                "S",
                null,
                null
        );

        return userRepository.save(user);
    }

    /*
     * Utilizado pelo CRUD administrativo.
     *
     * O administrador não define a senha.
     * O sistema gera uma senha temporária.
     */
    public UserCreateResponse create(
            UserCreateRequest request
    ) {

        validateUsername(request.username());
        validateEmail(request.email());

        String temporaryPassword =
                generateTemporaryPassword();

        String encodedPassword =
                passwordEncoder.encode(
                        temporaryPassword
                );

        User user = new User(
                null,
                request.username().trim(),
                request.name().trim(),
                normalizeEmail(request.email()),
                encodedPassword,
                "A",
                request.type(),
                "S",
                null,
                null
        );

        User saved =
                userRepository.save(user);

        return new UserCreateResponse(
                toResponse(saved),
                temporaryPassword
        );
    }

    public UserResponse update(
            Long id,
            UserUpdateRequest request
    ) {

        User current = userRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        validateUsernameForUpdate(
                id,
                request.username()
        );

        validateEmailForUpdate(
                id,
                request.email()
        );

        User updated = new User(
                current.id(),
                request.username().trim(),
                request.name().trim(),
                normalizeEmail(request.email()),
                current.password(),
                request.status(),
                request.type(),
                current.passwordChangeRequired(),
                current.createdAt(),
                current.updatedAt()
        );

        return toResponse(
                userRepository.update(updated)
        );
    }

    public void delete(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        userRepository.deleteById(
                user.id()
        );
    }

    public User changePassword(
            Long userId,
            String newRawPassword
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        String encodedPassword =
                passwordEncoder.encode(
                        newRawPassword
                );

        User updatedUser = new User(
                user.id(),
                user.username(),
                user.name(),
                user.email(),
                encodedPassword,
                user.status(),
                user.type(),
                "N",
                user.createdAt(),
                user.updatedAt()
        );

        return userRepository.update(
                updatedUser
        );
    }

    /*
     * Reset administrativo.
     *
     * Gera uma nova senha temporária
     * e obriga nova troca no próximo login.
     */
    public String resetPassword(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        String temporaryPassword =
                generateTemporaryPassword();

        String encodedPassword =
                passwordEncoder.encode(
                        temporaryPassword
                );

        User updatedUser = new User(
                user.id(),
                user.username(),
                user.name(),
                user.email(),
                encodedPassword,
                user.status(),
                user.type(),
                "S",
                user.createdAt(),
                user.updatedAt()
        );

        userRepository.update(
                updatedUser
        );

        return temporaryPassword;
    }

    public boolean passwordMatches(
            String rawPassword,
            User user
    ) {

        return passwordEncoder.matches(
                rawPassword,
                user.password()
        );
    }

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.id(),
                user.username(),
                user.name(),
                user.email(),
                user.status(),
                user.type(),
                "S".equals(
                        user.passwordChangeRequired()
                ),
                user.createdAt(),
                user.updatedAt()
        );
    }

    private void validateUsername(
            String username
    ) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "Usuário é obrigatório"
            );
        }

        if (userRepository.existsByUsername(
                username.trim()
        )) {

            throw new IllegalArgumentException(
                    "Usuário já cadastrado"
            );
        }
    }

    private void validateEmail(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            return;
        }

        if (userRepository.existsByEmail(
                email.trim()
        )) {

            throw new IllegalArgumentException(
                    "E-mail já cadastrado"
            );
        }
    }

    private void validateUsernameForUpdate(
            Long id,
            String username
    ) {

        userRepository.findByUsername(
                        username.trim()
                )
                .filter(user ->
                        !user.id().equals(id)
                )
                .ifPresent(user -> {
                    throw new IllegalArgumentException(
                            "Usuário já cadastrado"
                    );
                });
    }

    private void validateEmailForUpdate(
            Long id,
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            return;
        }

        boolean emailUsedByAnotherUser =
                userRepository.findAll()
                        .stream()
                        .anyMatch(user ->
                                user.email() != null
                                        && user.email()
                                        .equalsIgnoreCase(
                                                email.trim()
                                        )
                                        && !user.id()
                                        .equals(id)
                        );

        if (emailUsedByAnotherUser) {
            throw new IllegalArgumentException(
                    "E-mail já cadastrado"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            return null;
        }

        return email.trim();
    }

    private String generateTemporaryPassword() {

        StringBuilder password =
                new StringBuilder(
                        TEMP_PASSWORD_LENGTH
                );

        for (
                int index = 0;
                index < TEMP_PASSWORD_LENGTH;
                index++
        ) {

            int position =
                    secureRandom.nextInt(
                            TEMP_PASSWORD_CHARACTERS.length()
                    );

            password.append(
                    TEMP_PASSWORD_CHARACTERS.charAt(
                            position
                    )
            );
        }

        return password.toString();
    }
}
