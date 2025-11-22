package com.guppychat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacto")
public class Contacto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String usuarioId; // Usuario que guarda el contacto
    private String contactoId; // Usuario guardado como contacto
    private String nombrePersonalizado; // Nombre con el que lo guarda
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    public Contacto() {}
    
    public Contacto(String usuarioId, String contactoId, String nombrePersonalizado) {
        this.usuarioId = usuarioId;
        this.contactoId = contactoId;
        this.nombrePersonalizado = nombrePersonalizado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    
    public String getContactoId() { return contactoId; }
    public void setContactoId(String contactoId) { this.contactoId = contactoId; }
    
    public String getNombrePersonalizado() { return nombrePersonalizado; }
    public void setNombrePersonalizado(String nombrePersonalizado) { 
        this.nombrePersonalizado = nombrePersonalizado; 
    }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }
}