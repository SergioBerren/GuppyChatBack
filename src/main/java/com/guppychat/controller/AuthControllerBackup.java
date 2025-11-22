package com.guppychat.controller;

import com.guppychat.model.Usuario;
import com.guppychat.repository.UsuarioRepositorio;
import com.guppychat.config.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

//@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:5173")
public class AuthControllerBackup {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthControllerBackup(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        if (usuario.getCorreo() == null || usuario.getPassword() == null || usuario.getNombreUsuario() == null) {
            return ResponseEntity.badRequest().body("Faltan campos");
        }
        if (usuarioRepositorio.findByCorreo(usuario.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario nuevo = usuarioRepositorio.save(usuario);
        return ResponseEntity.ok(nuevo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos) {
        String correo = datos.get("email");
        String password = datos.get("password");
        Optional<Usuario> opt = usuarioRepositorio.findByCorreo(correo);
        if (opt.isEmpty()) return ResponseEntity.status(401).body("Usuario no encontrado");
        Usuario usuario = opt.get();
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }
        String token = jwtUtil.generarToken(usuario.getCorreo());
        return ResponseEntity.ok(Map.of("token", token, "user", usuario));
    }
}
