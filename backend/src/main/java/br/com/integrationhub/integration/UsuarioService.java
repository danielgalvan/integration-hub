package br.com.integrationhub.integration;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final String SQL = """
            select
                nm_usuario,
                ds_usuario,
                ie_situacao
            from
                usuario
            where
                nm_usuario = :nm_usuario
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UsuarioService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> buscarUsuario(String nmUsuario) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("nm_usuario", nmUsuario);

        return jdbcTemplate.queryForList(SQL, parameters);
    }
}