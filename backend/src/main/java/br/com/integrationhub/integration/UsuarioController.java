package br.com.integrationhub.backend.integration;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integrations")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuario")
    public List<Map<String, Object>> buscarUsuario(
            @RequestParam(name = "nm_usuario") String nmUsuario) {

        return usuarioService.buscarUsuario(nmUsuario);
    }
}