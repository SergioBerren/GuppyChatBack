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
        System.out.println("🎯 GET /usuarios - Listar todos");
        return usuarioServicio.listar();
    }
    
    // Listar usuarios excepto el actual
    @GetMapping("/excepto/{usuarioId}")
    public List<Usuario> listarUsuariosExcepto(@PathVariable("usuarioId") Long usuarioId) {
        System.out.println("🎯 GET /usuarios/excepto/" + usuarioId);
        return usuarioServicio.listar().stream()
                .filter(u -> !u.getId().equals(usuarioId))
                .collect(Collectors.toList());
    }
    
    // Registrar usuario
    @PostMapping
    public Usuario registrarUsuario(@RequestBody Usuario usuario) {
        System.out.println("🎯 POST /usuarios - Registrar");
        return usuarioServicio.registrar(usuario);
    }
    
    // Actualizar datos del usuario - MÉTODO CRÍTICO
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable("id") Long id, @RequestBody Map<String, String> cambios) {
        System.out.println("========================================");
        System.out.println("🎯 PATCH /usuarios/" + id + " - LLAMADO");
        System.out.println("📦 Cambios recibidos: " + cambios);
        System.out.println("========================================");
        
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isEmpty()) {
            System.out.println("❌ Usuario no encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = usuarioOpt.get();
        System.out.println("✅ Usuario encontrado: " + usuario.getNombreUsuario());
        
        // Actualizar solo los campos enviados
        if (cambios.containsKey("nombre")) {
            String nuevoNombre = cambios.get("nombre");
            usuario.setNombre(nuevoNombre == null || nuevoNombre.trim().isEmpty() ? null : nuevoNombre.trim());
            System.out.println("✅ Nombre actualizado: " + usuario.getNombre());
        }
        
        if (cambios.containsKey("apellido")) {
            String nuevoApellido = cambios.get("apellido");
            usuario.setApellido(nuevoApellido == null || nuevoApellido.trim().isEmpty() ? null : nuevoApellido.trim());
            System.out.println("✅ Apellido actualizado: " + usuario.getApellido());
        }
        
        if (cambios.containsKey("nombreUsuario")) {
            String nuevoNombreUsuario = cambios.get("nombreUsuario");
            if (nuevoNombreUsuario != null && !nuevoNombreUsuario.trim().isEmpty()) {
                usuario.setNombreUsuario(nuevoNombreUsuario.trim());
                System.out.println("✅ NombreUsuario actualizado: " + usuario.getNombreUsuario());
            }
        }
        
        if (cambios.containsKey("modoOscuro")) {
            usuario.setModoOscuro(Boolean.parseBoolean(cambios.get("modoOscuro")));
            System.out.println("✅ ModoOscuro actualizado: " + usuario.getModoOscuro());
        }
        
        Usuario actualizado = usuarioRepositorio.save(usuario);
        System.out.println("💾 Usuario guardado en BD correctamente");
        
        // No devolver la contraseña
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", actualizado.getId());
        respuesta.put("nombreUsuario", actualizado.getNombreUsuario());
        respuesta.put("nombre", actualizado.getNombre() != null ? actualizado.getNombre() : "");
        respuesta.put("apellido", actualizado.getApellido() != null ? actualizado.getApellido() : "");
        respuesta.put("correo", actualizado.getCorreo());
        respuesta.put("modoOscuro", actualizado.getModoOscuro() != null ? actualizado.getModoOscuro() : false);
        
        System.out.println("📤 Respuesta enviada al frontend");
        System.out.println("========================================");
        return ResponseEntity.ok(respuesta);
    }
    
    // Obtener usuario por nombre (debe ir DESPUÉS de las rutas específicas)
    @GetMapping("/{nombreUsuario}")
    public Usuario obtenerUsuario(@PathVariable("nombreUsuario") String nombreUsuario) {
        System.out.println("🎯 GET /usuarios/" + nombreUsuario);
        return usuarioServicio.obtenerPorNombre(nombreUsuario);
    }
}