package com.guppychat.controller;

import com.guppychat.model.Bloqueo;
import com.guppychat.model.SolicitudChat;
import com.guppychat.repository.BloqueoRepositorio;
import com.guppychat.repository.SolicitudChatRepositorio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bloqueos")
@CrossOrigin(origins = "http://localhost:5173")
public class BloqueoController {

    private final BloqueoRepositorio bloqueoRepo;
    private final SolicitudChatRepositorio solicitudRepo;

    public BloqueoController(BloqueoRepositorio bloqueoRepo, SolicitudChatRepositorio solicitudRepo) {
        this.bloqueoRepo = bloqueoRepo;
        this.solicitudRepo = solicitudRepo;
    }

    // Bloquear usuario
    @PostMapping("/bloquear")
    public ResponseEntity<?> bloquearUsuario(@RequestBody Map<String, String> datos) {
        String bloqueadorId = datos.get("bloqueadorId");
        String bloqueadoId = datos.get("bloqueadoId");

        // Verificar si ya está bloqueado
        Optional<Bloqueo> existente = bloqueoRepo.findByBloqueadorIdAndBloqueadoId(bloqueadorId, bloqueadoId);
        if (existente.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario ya está bloqueado"));
        }

        Bloqueo bloqueo = new Bloqueo(bloqueadorId, bloqueadoId);
        Bloqueo guardado = bloqueoRepo.save(bloqueo);

        return ResponseEntity.ok(guardado);
    }

    // Desbloquear usuario - MEJORADO: Eliminar solicitud rechazada
    @DeleteMapping("/desbloquear/{bloqueadorId}/{bloqueadoId}")
    public ResponseEntity<?> desbloquearUsuario(@PathVariable("bloqueadorId") String bloqueadorId,
                                                @PathVariable("bloqueadoId") String bloqueadoId) {
        System.out.println("🔓 Desbloqueando usuario: " + bloqueadorId + " -> " + bloqueadoId);
        
        Optional<Bloqueo> bloqueo = bloqueoRepo.findByBloqueadorIdAndBloqueadoId(bloqueadorId, bloqueadoId);

        if (bloqueo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Eliminar el bloqueo
        bloqueoRepo.delete(bloqueo.get());
        System.out.println("✅ Bloqueo eliminado");
        
        // ✅ NUEVO: Eliminar cualquier solicitud rechazada entre estos usuarios
        try {
            Optional<SolicitudChat> solicitudExistente = solicitudRepo.findEntreUsuarios(bloqueadorId, bloqueadoId);
            if (solicitudExistente.isPresent()) {
                SolicitudChat solicitud = solicitudExistente.get();
                System.out.println("📋 Solicitud encontrada con estado: " + solicitud.getEstado());
                
                // Solo eliminar si está rechazada (para permitir nuevas solicitudes)
                if (solicitud.getEstado().name().equals("RECHAZADA")) {
                    solicitudRepo.delete(solicitud);
                    System.out.println("🗑️ Solicitud rechazada eliminada - ahora pueden enviarse nuevas solicitudes");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al limpiar solicitud: " + e.getMessage());
            // No fallar el desbloqueo si hay error con la solicitud
        }
        
        return ResponseEntity.ok(Map.of(
            "mensaje", "Usuario desbloqueado correctamente",
            "info", "Ahora pueden enviarse nuevas solicitudes"
        ));
    }

    // Obtener lista de usuarios bloqueados
    @GetMapping("/lista/{usuarioId}")
    public ResponseEntity<List<Bloqueo>> obtenerBloqueados(@PathVariable("usuarioId") String usuarioId) {
        List<Bloqueo> bloqueados = bloqueoRepo.findByBloqueadorId(usuarioId);
        return ResponseEntity.ok(bloqueados);
    }
}