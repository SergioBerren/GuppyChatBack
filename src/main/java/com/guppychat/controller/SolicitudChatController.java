package com.guppychat.controller;

import com.guppychat.model.Bloqueo;
import com.guppychat.model.SolicitudChat;
import com.guppychat.model.EstadoSolicitud;
import com.guppychat.repository.BloqueoRepositorio;
import com.guppychat.repository.SolicitudChatRepositorio;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        
        System.out.println("📨 Enviando solicitud de " + emisorId + " a " + receptorId);
        
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
        
        System.out.println("✅ Solicitud creada con ID: " + guardada.getId());
        
        // Notificar al receptor por WebSocket
        plantillaMensajes.convertAndSend("/topic/solicitudes/" + receptorId, guardada);
        
        return ResponseEntity.ok(guardada);
    }
    
    // Aceptar solicitud
    @PostMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(@PathVariable("id") Long id) {
        System.out.println("✅ Aceptando solicitud: " + id);
        
        Optional<SolicitudChat> opt = solicitudRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SolicitudChat solicitud = opt.get();
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitudRepo.save(solicitud);
        
        System.out.println("✅ Solicitud aceptada correctamente");
        
        // Notificar al emisor
        plantillaMensajes.convertAndSend("/topic/solicitudes/" + solicitud.getEmisorId(), solicitud);
        
        return ResponseEntity.ok(solicitud);
    }
    
    // Rechazar solicitud (bloquear automáticamente)
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarSolicitud(@PathVariable("id") Long id) {
        System.out.println("❌ Rechazando solicitud: " + id);
        
        Optional<SolicitudChat> opt = solicitudRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SolicitudChat solicitud = opt.get();
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitudRepo.save(solicitud);
        
        // Crear bloqueo automático
        Bloqueo bloqueo = new Bloqueo(solicitud.getReceptorId(), solicitud.getEmisorId());
        bloqueoRepo.save(bloqueo);
        
        System.out.println("❌ Solicitud rechazada y usuario bloqueado");
        
        // Notificar al emisor
        plantillaMensajes.convertAndSend("/topic/solicitudes/" + solicitud.getEmisorId(), solicitud);
        
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada y usuario bloqueado"));
    }
    
    // Obtener solicitudes pendientes del usuario - CORREGIDO
    @GetMapping("/pendientes/{usuarioId}")
    public ResponseEntity<List<SolicitudChat>> obtenerPendientes(@PathVariable("usuarioId") String usuarioId) {
        System.out.println("🔍 Buscando solicitudes pendientes para usuario: " + usuarioId);
        
        try {
            // OPCIÓN 1: Obtener todas las solicitudes y filtrar por estado PENDIENTE
            List<SolicitudChat> todasLasSolicitudes = solicitudRepo.findAll();
            List<SolicitudChat> pendientes = todasLasSolicitudes.stream()
                .filter(s -> s.getReceptorId().equals(usuarioId))
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE)
                .collect(Collectors.toList());
            
            System.out.println("✅ Solicitudes pendientes encontradas: " + pendientes.size());
            return ResponseEntity.ok(pendientes);
            
        } catch (Exception e) {
            System.err.println("❌ Error al buscar solicitudes: " + e.getMessage());
            e.printStackTrace();
            // Devolver lista vacía en caso de error
            return ResponseEntity.ok(List.of());
        }
    }
    
    // Verificar si puede chatear con alguien
    @GetMapping("/puede-chatear/{usuario1}/{usuario2}")
    public ResponseEntity<?> puedeChatear(@PathVariable("usuario1") String usuario1, 
                                          @PathVariable("usuario2") String usuario2) {
        System.out.println("🔍 Verificando si " + usuario1 + " puede chatear con " + usuario2);
        
        // Verificar bloqueo
        Optional<Bloqueo> bloqueo = bloqueoRepo.existeBloqueoEntreUsuarios(usuario1, usuario2);
        if (bloqueo.isPresent()) {
            System.out.println("❌ Existe bloqueo entre usuarios");
            return ResponseEntity.ok(Map.of("puede", false, "razon", "bloqueado"));
        }
        
        // Verificar solicitud
        Optional<SolicitudChat> solicitud = solicitudRepo.findEntreUsuarios(usuario1, usuario2);
        if (solicitud.isEmpty()) {
            System.out.println("⚠️ No existe solicitud entre usuarios");
            return ResponseEntity.ok(Map.of("puede", false, "razon", "sin_solicitud"));
        }
        
        SolicitudChat sol = solicitud.get();
        if (sol.getEstado() == EstadoSolicitud.ACEPTADA) {
            System.out.println("✅ Solicitud aceptada, pueden chatear");
            return ResponseEntity.ok(Map.of("puede", true));
        }
        
        System.out.println("⏳ Solicitud en estado: " + sol.getEstado());
        return ResponseEntity.ok(Map.of("puede", false, "razon", "pendiente"));
    }
}