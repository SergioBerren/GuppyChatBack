package com.guppychat.controller;

import com.guppychat.model.Bloqueo;
import com.guppychat.model.SolicitudChat;
import com.guppychat.repository.BloqueoRepositorio;
import com.guppychat.repository.SolicitudChatRepositorio;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "http://localhost:5173")
public class SolicitudChatController {
    
    private final SolicitudChatRepositorio solicitudRepo;
    private final BloqueoRepositorio bloqueoRepo;
    private final SimpMessagingTemplate plantillaMensajes;

    public SolicitudChatController(SolicitudChatRepositorio solicitudRepo,
                                   BloqueoRepositorio bloqueoRepo,
                                   SimpMessagingTemplate plantillaMensajes) {
        this.solicitudRepo = solicitudRepo;
        this.bloqueoRepo = bloqueoRepo;
        this.plantillaMensajes = plantillaMensajes;
    }

    // Enviar solicitud de chat
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarSolicitud(@RequestBody Map<String, String> datos) {
        String emisorId = datos.get("emisorId");
        String receptorId = datos.get("receptorId");
        
        // Verificar si hay un bloqueo
        Optional<Bloqueo> bloqueo = bloqueoRepo.existeBloqueoEntreUsuarios(emisorId, receptorId);
        if (bloqueo.isPresent()) {
            return ResponseEntity.status(403).body(Map.of("error", "No se puede enviar solicitud. Usuario bloqueado."));
        }
        
        // Verificar si ya existe una solicitud
        Optional<SolicitudChat> existente = solicitudRepo.findEntreUsuarios(emisorId, receptorId);
        if (existente.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una solicitud entre estos usuarios"));
        }
        
        // Crear nueva solicitud
        SolicitudChat solicitud = new SolicitudChat(emisorId, receptorId);
        SolicitudChat guardada = solicitudRepo.save(solicitud);
        
        // Notificar al receptor por WebSocket
        plantillaMensajes.convertAndSend("/topic/solicitudes/" + receptorId, guardada);
        
        return ResponseEntity.ok(guardada);
    }
    
    // Aceptar solicitud
    @PostMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(@PathVariable Long id) {
        Optional<SolicitudChat> opt = solicitudRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SolicitudChat solicitud = opt.get();
//        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitudRepo.save(solicitud);
        
        // Notificar al emisor
        plantillaMensajes.convertAndSend("/topic/solicitudes/" + solicitud.getEmisorId(), solicitud);
        
        return ResponseEntity.ok(solicitud);
    }
    
    // Rechazar solicitud (bloquear automáticamente)
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarSolicitud(@PathVariable Long id) {
        Optional<SolicitudChat> opt = solicitudRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SolicitudChat solicitud = opt.get();
//        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitudRepo.save(solicitud);
        
        // Crear bloqueo automático
        Bloqueo bloqueo = new Bloqueo(solicitud.getReceptorId(), solicitud.getEmisorId());
        bloqueoRepo.save(bloqueo);
        
        // Notificar al emisor
        plantillaMensajes.convertAndSend("/topic/solicitudes/" + solicitud.getEmisorId(), solicitud);
        
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada y usuario bloqueado"));
    }
    
    // Obtener solicitudes pendientes del usuario
    @GetMapping("/pendientes/{usuarioId}")
    public ResponseEntity<List<SolicitudChat>> obtenerPendientes(@PathVariable String usuarioId) {
        List<SolicitudChat> pendientes = solicitudRepo.findByReceptorIdAndEstado(usuarioId, "PENDIENTE");
        return ResponseEntity.ok(pendientes);
    }
    
    // Verificar si puede chatear con alguien
    @GetMapping("/puede-chatear/{usuario1}/{usuario2}")
    public ResponseEntity<?> puedeChatear(@PathVariable String usuario1, @PathVariable String usuario2) {
        // Verificar bloqueo
        Optional<Bloqueo> bloqueo = bloqueoRepo.existeBloqueoEntreUsuarios(usuario1, usuario2);
        if (bloqueo.isPresent()) {
            return ResponseEntity.ok(Map.of("puede", false, "razon", "bloqueado"));
        }
        
        // Verificar solicitud
        Optional<SolicitudChat> solicitud = solicitudRepo.findEntreUsuarios(usuario1, usuario2);
        if (solicitud.isEmpty()) {
            return ResponseEntity.ok(Map.of("puede", false, "razon", "sin_solicitud"));
        }
        
        SolicitudChat sol = solicitud.get();
        if (sol.getEstado().toString().equals("ACEPTADA")) {
            return ResponseEntity.ok(Map.of("puede", true));
        }
        
        return ResponseEntity.ok(Map.of("puede", false, "razon", "pendiente"));
    }
}

// Enum (debe coincidir con el de SolicitudChat)
enum EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA
}