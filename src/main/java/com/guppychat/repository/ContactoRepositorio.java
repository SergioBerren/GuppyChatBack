package com.guppychat.repository;

import com.guppychat.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactoRepositorio extends JpaRepository<Contacto, Long> {
    
    // Obtener todos los contactos de un usuario
    List<Contacto> findByUsuarioId(String usuarioId);
    
    // Buscar contacto por nombre personalizado
    @Query("SELECT c FROM Contacto c WHERE c.usuarioId = :usuarioId AND " +
           "LOWER(c.nombrePersonalizado) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Contacto> buscarPorNombre(@Param("usuarioId") String usuarioId, 
                                    @Param("busqueda") String busqueda);
    
    // Verificar si un contacto ya existe
    Optional<Contacto> findByUsuarioIdAndContactoId(String usuarioId, String contactoId);
}