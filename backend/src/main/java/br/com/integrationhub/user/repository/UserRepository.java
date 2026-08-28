package br.com.integrationhub.user.repository;

import br.com.integrationhub.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    User save(User user);

    User update(User user);

    void deleteById(Long id);

    long count();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
