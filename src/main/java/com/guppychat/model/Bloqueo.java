package com.guppychat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bloqueo")
public class Bloqueo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bloqueadorId; // Usuario que bloquea
    private String bloqueadoId;  // Usuario bloqueado
    
    private LocalDateTime fechaBloqueo = LocalDateTime.now();
    
    public Bloqueo() {}
    
    // Constructor
    public Bloqueo(String bloqueadorId, String bloqueadoId) {
        this.bloqueadorId = bloqueadorId;
        this.bloqueadoId = bloqueadoId;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBloqueadorId() { return bloqueadorId; }
    public void setBloqueadorId(String bloqueadorId) { this.bloqueadorId = bloqueadorId; }
    
    public String getBloqueadoId() { return bloqueadoId; }
    public void setBloqueadoId(String bloqueadoId) { this.bloqueadoId = bloqueadoId; }
    
    public LocalDateTime getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(LocalDateTime fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }
}