package com.guppychat.controller;

import com.guppychat.model.Bloqueo;
import com.guppychat.repository.BloqueoRepositorio;
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

    public BloqueoController(BloqueoRepositorio bloqueoRepo) {
        this.bloqueoRepo = bloqueoRepo;
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
    
    // Desbloquear usuario
    @DeleteMapping("/desbloquear/{bloqueadorId}/{bloqueadoId}")
    public ResponseEntity<?> desbloquearUsuario(@PathVariable String bloqueadorId, 
                                                @PathVariable String bloqueadoId) {
        Optional<Bloqueo> bloqueo = bloqueoRepo.findByBloqueadorIdAndBloqueadoId(bloqueadorId, bloqueadoId);
        
        if (bloqueo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        bloqueoRepo.delete(bloqueo.get());
        return ResponseEntity.ok(Map.of("mensaje", "Usuario desbloqueado"));
    }
    
    // Obtener lista de usuarios bloqueados
    @GetMapping("/lista/{usuarioId}")
    public ResponseEntity<List<Bloqueo>> obtenerBloqueados(@PathVariable String usuarioId) {
        List<Bloqueo> bloqueados = bloqueoRepo.findByBloqueadorId(usuarioId);
        return ResponseEntity.ok(bloqueados);
    }
}