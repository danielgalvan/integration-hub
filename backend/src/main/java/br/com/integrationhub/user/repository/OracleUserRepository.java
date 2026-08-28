package br.com.integrationhub.user.repository;

import br.com.integrationhub.user.model.User;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class OracleUserRepository implements UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OracleUserRepository(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {

        String sql = """
                select
                    nr_sequencia,
                    nm_usuario,
                    ds_usuario,
                    ds_email,
                    ds_senha,
                    ie_situacao,
                    ie_tipo_usuario,
                    ie_trocar_senha,
                    dt_criacao,
                    dt_atualizacao
                from ih_users
                order by nm_usuario
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapUser(rs)
        );
    }

    @Override
    public Optional<User> findById(Long id) {

        String sql = """
                select
                    nr_sequencia,
                    nm_usuario,
                    ds_usuario,
                    ds_email,
                    ds_senha,
                    ie_situacao,
                    ie_tipo_usuario,
                    ie_trocar_senha,
                    dt_criacao,
                    dt_atualizacao
                from ih_users
                where nr_sequencia = :id
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("id", id);

        List<User> users = jdbcTemplate.query(
                sql,
                parameters,
                (rs, rowNum) -> mapUser(rs)
        );

        return users.stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {

        String sql = """
                select
                    nr_sequencia,
                    nm_usuario,
                    ds_usuario,
                    ds_email,
                    ds_senha,
                    ie_situacao,
                    ie_tipo_usuario,
                    ie_trocar_senha,
                    dt_criacao,
                    dt_atualizacao
                from ih_users
                where upper(nm_usuario) = upper(:username)
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("username", username);

        List<User> users = jdbcTemplate.query(
                sql,
                parameters,
                (rs, rowNum) -> mapUser(rs)
        );

        return users.stream().findFirst();
    }

    @Override
    public User save(User user) {

        String sql = """
                insert into ih_users (
                    nm_usuario,
                    ds_usuario,
                    ds_email,
                    ds_senha,
                    ie_situacao,
                    ie_tipo_usuario,
                    ie_trocar_senha
                ) values (
                    :username,
                    :name,
                    :email,
                    :password,
                    :status,
                    :type,
                    :passwordChangeRequired
                )
                """;

        MapSqlParameterSource parameters =
                createParameters(user);

        jdbcTemplate.update(
                sql,
                parameters
        );

        return findByUsername(user.username())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Usuário não encontrado após inclusão"
                        )
                );
    }

    @Override
    public User update(User user) {

        String sql = """
                update ih_users
                   set nm_usuario = :username,
                       ds_usuario = :name,
                       ds_email = :email,
                       ds_senha = :password,
                       ie_situacao = :status,
                       ie_tipo_usuario = :type,
                       ie_trocar_senha = :passwordChangeRequired,
                       dt_atualizacao = current_timestamp
                 where nr_sequencia = :id
                """;

        MapSqlParameterSource parameters =
                createParameters(user)
                        .addValue("id", user.id());

        int rows = jdbcTemplate.update(
                sql,
                parameters
        );

        if (rows == 0) {
            throw new IllegalArgumentException(
                    "Usuário não encontrado"
            );
        }

        return findById(user.id())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Usuário não encontrado após atualização"
                        )
                );
    }

    @Override
    public void deleteById(Long id) {

        String sql = """
                delete from ih_users
                where nr_sequencia = :id
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("id", id);

        jdbcTemplate.update(
                sql,
                parameters
        );
    }

    @Override
    public long count() {

        String sql = """
                select count(*)
                from ih_users
                """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource(),
                Long.class
        );

        return count != null ? count : 0;
    }

    @Override
    public boolean existsByUsername(String username) {

        String sql = """
                select count(*)
                from ih_users
                where upper(nm_usuario) = upper(:username)
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("username", username);

        Long count = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {

        String sql = """
                select count(*)
                from ih_users
                where upper(ds_email) = upper(:email)
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("email", email);

        Long count = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return count != null && count > 0;
    }

    private MapSqlParameterSource createParameters(
            User user
    ) {

        return new MapSqlParameterSource()
                .addValue("username", user.username())
                .addValue("name", user.name())
                .addValue("email", user.email())
                .addValue("password", user.password())
                .addValue("status", user.status())
                .addValue("type", user.type())
                .addValue(
                        "passwordChangeRequired",
                        user.passwordChangeRequired()
                );
    }

    private User mapUser(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        Timestamp updatedAt =
                rs.getTimestamp("dt_atualizacao");

        return new User(
                rs.getLong("nr_sequencia"),
                rs.getString("nm_usuario"),
                rs.getString("ds_usuario"),
                rs.getString("ds_email"),
                rs.getString("ds_senha"),
                rs.getString("ie_situacao"),
                rs.getString("ie_tipo_usuario"),
                rs.getString("ie_trocar_senha"),
                rs.getTimestamp("dt_criacao")
                        .toLocalDateTime(),
                updatedAt != null
                        ? updatedAt.toLocalDateTime()
                        : null
        );
    }
}
