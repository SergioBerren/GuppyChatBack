package com.guppychat.controller;

import com.guppychat.model.Usuario;
import com.guppychat.model.Contacto;
import com.guppychat.model.Chat;
import com.guppychat.model.Bloqueo;
import com.guppychat.model.SolicitudChat;
import com.guppychat.repository.UsuarioRepositorio;
import com.guppychat.repository.ContactoRepositorio;
import com.guppychat.repository.ChatRepositorio;
import com.guppychat.repository.BloqueoRepositorio;
import com.guppychat.repository.SolicitudChatRepositorio;
import com.guppychat.config.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ContactoRepositorio contactoRepo;
    private final ChatRepositorio chatRepo;
    private final BloqueoRepositorio bloqueoRepo;
    private final SolicitudChatRepositorio solicitudRepo;
    
    public AuthController(UsuarioRepositorio usuarioRepositorio, 
                         PasswordEncoder passwordEncoder, 
                         JwtUtil jwtUtil,
                         ContactoRepositorio contactoRepo,
                         ChatRepositorio chatRepo,
                         BloqueoRepositorio bloqueoRepo,
                         SolicitudChatRepositorio solicitudRepo) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.contactoRepo = contactoRepo;
        this.chatRepo = chatRepo;
        this.bloqueoRepo = bloqueoRepo;
        this.solicitudRepo = solicitudRepo;
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
    
    // ✅ Cambiar contraseña
    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> datos) {
        String correo = datos.get("correo");
        String passwordActual = datos.get("passwordActual");
        String passwordNueva = datos.get("passwordNueva");
        
        if (correo == null || passwordActual == null || passwordNueva == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan campos obligatorios"));
        }
        
        Optional<Usuario> opt = usuarioRepositorio.findByCorreo(correo);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        
        Usuario usuario = opt.get();
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "La contraseña actual es incorrecta"));
        }
        
        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepositorio.save(usuario);
        
        System.out.println("✅ Contraseña cambiada para: " + correo);
        
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }
    
    // ✅ NUEVO: Eliminar cuenta completamente
    @DeleteMapping("/eliminar-cuenta")
    public ResponseEntity<?> eliminarCuenta(@RequestBody Map<String, String> datos) {
        String correo = datos.get("correo");
        String password = datos.get("password");
        
        if (correo == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan campos obligatorios"));
        }
        
        Optional<Usuario> opt = usuarioRepositorio.findByCorreo(correo);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        
        Usuario usuario = opt.get();
        String usuarioId = String.valueOf(usuario.getId());
        
        // Verificar contraseña
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Contraseña incorrecta"));
        }
        
        try {
            System.out.println("🗑️ Iniciando eliminación completa de usuario: " + correo);
            
            // 1. Eliminar contactos (como usuario y como contacto)
            List<Contacto> contactosComoUsuario = contactoRepo.findByUsuarioId(usuarioId);
            contactoRepo.deleteAll(contactosComoUsuario);
            System.out.println("   ✓ Contactos como usuario eliminados: " + contactosComoUsuario.size());
            
            // Contactos donde este usuario es el contactoId
            List<Contacto> contactosComoContacto = contactoRepo.findAll().stream()
                .filter(c -> c.getContactoId().equals(usuarioId))
                .collect(Collectors.toList());
            contactoRepo.deleteAll(contactosComoContacto);
            System.out.println("   ✓ Contactos donde es contactoId eliminados: " + contactosComoContacto.size());
            
            // 2. Eliminar chats (como emisor y receptor)
            List<Chat> chats = chatRepo.findByEmisorIdOrReceptorId(usuarioId, usuarioId);
            chatRepo.deleteAll(chats);
            System.out.println("   ✓ Chats eliminados: " + chats.size());
            
            // 3. Eliminar bloqueos (como bloqueador y bloqueado)
            List<Bloqueo> bloqueosComoBloquedor = bloqueoRepo.findByBloqueadorId(usuarioId);
            bloqueoRepo.deleteAll(bloqueosComoBloquedor);
            System.out.println("   ✓ Bloqueos como bloqueador eliminados: " + bloqueosComoBloquedor.size());
            
            List<Bloqueo> bloqueosComoBloqueado = bloqueoRepo.findAll().stream()
                .filter(b -> b.getBloqueadoId().equals(usuarioId))
                .collect(Collectors.toList());
            bloqueoRepo.deleteAll(bloqueosComoBloqueado);
            System.out.println("   ✓ Bloqueos como bloqueado eliminados: " + bloqueosComoBloqueado.size());
            
            // 4. Eliminar solicitudes (como emisor y receptor)
            List<SolicitudChat> solicitudesComoEmisor = solicitudRepo.findByEmisorId(usuarioId);
            solicitudRepo.deleteAll(solicitudesComoEmisor);
            System.out.println("   ✓ Solicitudes como emisor eliminadas: " + solicitudesComoEmisor.size());
            
            List<SolicitudChat> solicitudesComoReceptor = solicitudRepo.findAll().stream()
                .filter(s -> s.getReceptorId().equals(usuarioId))
                .collect(Collectors.toList());
            solicitudRepo.deleteAll(solicitudesComoReceptor);
            System.out.println("   ✓ Solicitudes como receptor eliminadas: " + solicitudesComoReceptor.size());
            
            // 5. Finalmente, eliminar el usuario
            usuarioRepositorio.delete(usuario);
            System.out.println("   ✓ Usuario eliminado");
            
            System.out.println("✅ Cuenta eliminada completamente: " + correo);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Cuenta eliminada correctamente"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar cuenta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error al eliminar la cuenta"));
        }
    }
}