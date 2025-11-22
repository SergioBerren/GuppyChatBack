package com.guppychat.repository;

import com.guppychat.model.SolicitudChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitudChatRepositorio extends JpaRepository<SolicitudChat, Long> {
    
    // Verificar si existe una solicitud entre dos usuarios (en cualquier dirección)
    @Query("SELECT s FROM SolicitudChat s WHERE " +
           "(s.emisorId = :usuario1 AND s.receptorId = :usuario2) OR " +
           "(s.emisorId = :usuario2 AND s.receptorId = :usuario1)")
    Optional<SolicitudChat> findEntreUsuarios(@Param("usuario1") String usuario1, 
                                              @Param("usuario2") String usuario2);
    
    // Obtener solicitudes pendientes recibidas por un usuario
    List<SolicitudChat> findByReceptorIdAndEstado(String receptorId, String estado);
    
    // Obtener solicitudes enviadas por un usuario
    List<SolicitudChat> findByEmisorId(String emisorId);
}