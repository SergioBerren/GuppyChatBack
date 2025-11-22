package com.guppychat.controller;

import com.guppychat.model.Usuario;
import com.guppychat.repository.UsuarioRepositorio;
import com.guppychat.config.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepositorio usuarioRepositorio, 
                         PasswordEncoder passwordEncoder, 
                         JwtUtil jwtUtil) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        if (usuario.getCorreo() == null || usuario.getPassword() == null || usuario.getNombreUsuario() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan campos"));
        }
        
        if (usuarioRepositorio.findByCorreo(usuario.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
        }
        
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario nuevo = usuarioRepositorio.save(usuario);
        
        // No devolver la contraseña
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", nuevo.getId());
        respuesta.put("nombreUsuario", nuevo.getNombreUsuario());
        respuesta.put("nombre", nuevo.getNombre() != null ? nuevo.getNombre() : "");
        respuesta.put("apellido", nuevo.getApellido() != null ? nuevo.getApellido() : "");
        respuesta.put("correo", nuevo.getCorreo());
        respuesta.put("modoOscuro", nuevo.getModoOscuro() != null ? nuevo.getModoOscuro() : false);
        
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos) {
        String correo = datos.get("email");
        String password = datos.get("password");
        
        Optional<Usuario> opt = usuarioRepositorio.findByCorreo(correo);
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
        }
        
        Usuario usuario = opt.get();
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Contraseña incorrecta"));
        }
        
        String token = jwtUtil.generarToken(usuario.getCorreo());
        
        // No devolver la contraseña
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("token", token);
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", usuario.getId());
        userData.put("nombreUsuario", usuario.getNombreUsuario());
        userData.put("nombre", usuario.getNombre() != null ? usuario.getNombre() : "");
        userData.put("apellido", usuario.getApellido() != null ? usuario.getApellido() : "");
        userData.put("correo", usuario.getCorreo());
        
        respuesta.put("user", userData);
        
        return ResponseEntity.ok(respuesta);
    }
}