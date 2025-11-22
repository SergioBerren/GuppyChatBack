package com.guppychat.repository;

import com.guppychat.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepositorio extends JpaRepository<Chat, Long> {
    
    // Buscar todos los chats donde el usuario es emisor O receptor
    List<Chat> findByEmisorIdOrReceptorId(String emisorId, String receptorId);
    
    // NUEVO: Buscar conversación entre dos usuarios específicos
    @Query("SELECT c FROM Chat c WHERE " +
           "(c.emisorId = :usuario1 AND c.receptorId = :usuario2) OR " +
           "(c.emisorId = :usuario2 AND c.receptorId = :usuario1) " +
           "ORDER BY c.fechaHora ASC")
    List<Chat> findConversacion(@Param("usuario1") String usuario1, 
                                @Param("usuario2") String usuario2);
}