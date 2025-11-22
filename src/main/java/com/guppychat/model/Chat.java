package com.guppychat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emisorId;
    private String receptorId;

    @Column(length = 4000)
    private String mensajeCifrado;

    private LocalDateTime fechaHora = LocalDateTime.now();

    public Chat() {}

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmisorId() { return emisorId; }
    public void setEmisorId(String emisorId) { this.emisorId = emisorId; }

    public String getReceptorId() { return receptorId; }
    public void setReceptorId(String receptorId) { this.receptorId = receptorId; }

    public String getMensajeCifrado() { return mensajeCifrado; }
    public void setMensajeCifrado(String mensajeCifrado) { this.mensajeCifrado = mensajeCifrado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
