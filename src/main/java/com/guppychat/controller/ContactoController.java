package com.guppychat.controller;

import com.guppychat.model.Contacto;
import com.guppychat.model.Usuario;
import com.guppychat.model.SolicitudChat;
import com.guppychat.repository.ContactoRepositorio;
import com.guppychat.repository.UsuarioRepositorio;
import com.guppychat.repository.SolicitudChatRepositorio;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contactos")
@CrossOrigin(origins = "http://localhost:5173")
public class ContactoController {
    
    private final ContactoRepositorio contactoRepo;
    private final UsuarioRepositorio usuarioRepo;
    private final SolicitudChatRepositorio solicitudRepo;
    private final SimpMessagingTemplate plantillaMensajes;

    public ContactoController(ContactoRepositorio contactoRepo,
                             UsuarioRepositorio usuarioRepo,
                             SolicitudChatRepositorio solicitudRepo,
                             SimpMessagingTemplate plantillaMensajes) {
        this.contactoRepo = contactoRepo;
        this.usuarioRepo = usuarioRepo;
        this.solicitudRepo = solicitudRepo;
        this.plantillaMensajes = plantillaMensajes;
    }

    // Agregar contacto por correo y enviar solicitud automáticamente
    @PostMapping("/agregar")
    public ResponseEntity<?> agregarContacto(@RequestBody Map<String, String> datos) {
        String usuarioId = datos.get("usuarioId");
        String correoContacto = datos.get("correo");
        String nombrePersonalizado = datos.get("nombrePersonalizado");
        
        // Buscar usuario por correo
        Optional<Usuario> usuarioOpt = usuarioRepo.findByCorreo(correoContacto);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("error", "No existe un usuario con ese correo"));
        }
        
        Usuario usuarioDestino = usuarioOpt.get();
        String contactoId = String.valueOf(usuarioDestino.getId());
        
        // Verificar que no se agregue a sí mismo
        if (usuarioId.equals(contactoId)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "No puedes agregarte a ti mismo como contacto"));
        }
        
        // Verificar si ya existe el contacto
        Optional<Contacto> existente = contactoRepo.findByUsuarioIdAndContactoId(usuarioId, contactoId);
        if (existente.isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Este contacto ya existe en tu lista"));
        }
        
        // Crear contacto
        Contacto contacto = new Contacto(usuarioId, contactoId, nombrePersonalizado);
        Contacto guardado = contactoRepo.save(contacto);
        
        // Crear solicitud de chat automáticamente
        Optional<SolicitudChat> solicitudExistente = solicitudRepo.findEntreUsuarios(usuarioId, contactoId);
        if (solicitudExistente.isEmpty()) {
            SolicitudChat solicitud = new SolicitudChat(usuarioId, contactoId);
            SolicitudChat solicitudGuardada = solicitudRepo.save(solicitud);
            
            // Notificar al receptor por WebSocket
            plantillaMensajes.convertAndSend("/topic/solicitudes/" + contactoId, solicitudGuardada);
        }
        
        // Crear respuesta con información del contacto
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", guardado.getId());
        respuesta.put("usuarioId", guardado.getUsuarioId());
        respuesta.put("contactoId", guardado.getContactoId());
        respuesta.put("nombrePersonalizado", guardado.getNombrePersonalizado());
        respuesta.put("correo", usuarioDestino.getCorreo());
        respuesta.put("nombreUsuario", usuarioDestino.getNombreUsuario());
        
        return ResponseEntity.ok(respuesta);
    }
    
    // Obtener todos los contactos de un usuario con información completa
    @GetMapping("/lista/{usuarioId}")
    public ResponseEntity<?> obtenerContactos(@PathVariable String usuarioId) {
        List<Contacto> contactos = contactoRepo.findByUsuarioId(usuarioId);
        
        List<Map<String, Object>> contactosConInfo = contactos.stream().map(contacto -> {
            Optional<Usuario> usuarioOpt = usuarioRepo.findById(Long.parseLong(contacto.getContactoId()));
            
            Map<String, Object> info = new HashMap<>();
            info.put("id", contacto.getId());
            info.put("contactoId", contacto.getContactoId());
            info.put("nombrePersonalizado", contacto.getNombrePersonalizado());
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                info.put("correo", usuario.getCorreo());
                info.put("nombreUsuario", usuario.getNombreUsuario());
            }
            
            return info;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(contactosConInfo);
    }
    
    // Buscar contactos por nombre personalizado
    @GetMapping("/buscar/{usuarioId}")
    public ResponseEntity<?> buscarContactos(@PathVariable String usuarioId, 
                                            @RequestParam String query) {
        List<Contacto> contactos = contactoRepo.buscarPorNombre(usuarioId, query);
        
        List<Map<String, Object>> contactosConInfo = contactos.stream().map(contacto -> {
            Optional<Usuario> usuarioOpt = usuarioRepo.findById(Long.parseLong(contacto.getContactoId()));
            
            Map<String, Object> info = new HashMap<>();
            info.put("id", contacto.getId());
            info.put("contactoId", contacto.getContactoId());
            info.put("nombrePersonalizado", contacto.getNombrePersonalizado());
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                info.put("correo", usuario.getCorreo());
                info.put("nombreUsuario", usuario.getNombreUsuario());
            }
            
            return info;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(contactosConInfo);
    }
    
    // Eliminar contacto
    @DeleteMapping("/{contactoId}")
    public ResponseEntity<?> eliminarContacto(@PathVariable Long contactoId) {
        Optional<Contacto> contacto = contactoRepo.findById(contactoId);
        if (contacto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        contactoRepo.delete(contacto.get());
        return ResponseEntity.ok(Map.of("mensaje", "Contacto eliminado"));
    }
}