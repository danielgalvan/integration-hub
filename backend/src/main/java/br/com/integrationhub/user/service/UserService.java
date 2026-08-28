package br.com.integrationhub.user.service;

import br.com.integrationhub.user.model.User;
import br.com.integrationhub.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
                name != null ? name.trim() : null,
                email != null ? email.trim() : null,
                encodedPassword,
                status,
                type,
                null,
                null
        );

        return userRepository.save(user);
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

    private void validateUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Usuário é obrigatório"
            );
        }

        if (userRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException(
                    "Usuário já cadastrado"
            );
        }
    }

    private void validateEmail(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        if (userRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException(
                    "E-mail já cadastrado"
            );
        }
    }
}
