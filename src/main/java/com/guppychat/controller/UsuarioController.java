package com.guppychat.controller;

import com.guppychat.model.Usuario;
import com.guppychat.service.UsuarioServicio;
import com.guppychat.repository.UsuarioRepositorio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    private final UsuarioServicio usuarioServicio;
    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioController(UsuarioServicio usuarioServicio, UsuarioRepositorio usuarioRepositorio) {
        this.usuarioServicio = usuarioServicio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // Listar todos los usuarios
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioServicio.listar();
    }

    // Listar usuarios excepto el actual
    @GetMapping("/excepto/{usuarioId}")
    public List<Usuario> listarUsuariosExcepto(@PathVariable Long usuarioId) {
        return usuarioServicio.listar().stream()
                .filter(u -> !u.getId().equals(usuarioId))
                .collect(Collectors.toList());
    }

    // Registrar usuario
    @PostMapping
    public Usuario registrarUsuario(@RequestBody Usuario usuario) {
        return usuarioServicio.registrar(usuario);
    }

    // Obtener usuario por nombre
    @GetMapping("/{nombreUsuario}")
    public Usuario obtenerUsuario(@PathVariable String nombreUsuario) {
        return usuarioServicio.obtenerPorNombre(nombreUsuario);
    }

    // Actualizar datos del usuario
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Map<String, String> cambios) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();

        // Actualizar solo los campos enviados
        if (cambios.containsKey("nombre")) {
            usuario.setNombre(cambios.get("nombre"));
        }
        if (cambios.containsKey("apellido")) {
            usuario.setApellido(cambios.get("apellido"));
        }
        if (cambios.containsKey("nombreUsuario")) {
            usuario.setNombreUsuario(cambios.get("nombreUsuario"));
        }
        if (cambios.containsKey("modoOscuro")) {
            usuario.setModoOscuro(Boolean.parseBoolean(cambios.get("modoOscuro")));
        }

        Usuario actualizado = usuarioRepositorio.save(usuario);

        // No devolver la contraseña
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", actualizado.getId());
        respuesta.put("nombreUsuario", actualizado.getNombreUsuario());
        respuesta.put("nombre", actualizado.getNombre() != null ? actualizado.getNombre() : "");
        respuesta.put("apellido", actualizado.getApellido() != null ? actualizado.getApellido() : "");
        respuesta.put("correo", actualizado.getCorreo());
        respuesta.put("modoOscuro", actualizado.getModoOscuro() != null ? actualizado.getModoOscuro() : false);

        return ResponseEntity.ok(respuesta);
    }
}