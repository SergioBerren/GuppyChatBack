package com.guppychat.repository;

import com.guppychat.model.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BloqueoRepositorio extends JpaRepository<Bloqueo, Long> {
    
    // Verificar si un usuario ha bloqueado a otro
    Optional<Bloqueo> findByBloqueadorIdAndBloqueadoId(String bloqueadorId, String bloqueadoId);
    
    // Verificar si existe un bloqueo entre dos usuarios (en cualquier dirección)
    @Query("SELECT b FROM Bloqueo b WHERE " +
           "(b.bloqueadorId = :usuario1 AND b.bloqueadoId = :usuario2) OR " +
           "(b.bloqueadorId = :usuario2 AND b.bloqueadoId = :usuario1)")
    Optional<Bloqueo> existeBloqueoEntreUsuarios(@Param("usuario1") String usuario1, 
                                                  @Param("usuario2") String usuario2);
    
    // Obtener todos los usuarios bloqueados por un usuario
    List<Bloqueo> findByBloqueadorId(String bloqueadorId);
}