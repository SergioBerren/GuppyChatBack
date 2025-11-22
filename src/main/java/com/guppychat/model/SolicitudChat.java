package com.guppychat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_chat")
public class SolicitudChat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String emisorId;
    private String receptorId;
    
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado; // PENDIENTE, ACEPTADA, RECHAZADA
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    public SolicitudChat() {}
    
    // Constructor
    public SolicitudChat(String emisorId, String receptorId) {
        this.emisorId = emisorId;
        this.receptorId = receptorId;
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmisorId() { return emisorId; }
    public void setEmisorId(String emisorId) { this.emisorId = emisorId; }
    
    public String getReceptorId() { return receptorId; }
    public void setReceptorId(String receptorId) { this.receptorId = receptorId; }
    
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}