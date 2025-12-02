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
    
    @Column(length = 10000, columnDefinition = "TEXT")
    private String mensajeCifrado;
    
    // ✅ NUEVO: Campos para archivos
    @Column(length = 50)
    private String tipoMensaje; // "texto", "imagen", "archivo"
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String archivoBase64; // Contenido del archivo en Base64
    
    @Column(length = 255)
    private String nombreArchivo; // Nombre original del archivo
    
    @Column(length = 100)
    private String tipoArchivo; // MIME type (image/png, application/pdf, etc.)
    
    private Long tamanoArchivo; // Tamaño en bytes
    
    private LocalDateTime fechaHora = LocalDateTime.now();
    
    public Chat() {}
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmisorId() { return emisorId; }
    public void setEmisorId(String emisorId) { this.emisorId = emisorId; }
    
    public String getReceptorId() { return receptorId; }
    public void setReceptorId(String receptorId) { this.receptorId = receptorId; }
    
    public String getMensajeCifrado() { return mensajeCifrado; }
    public void setMensajeCifrado(String mensajeCifrado) { this.mensajeCifrado = mensajeCifrado; }
    
    public String getTipoMensaje() { return tipoMensaje; }
    public void setTipoMensaje(String tipoMensaje) { this.tipoMensaje = tipoMensaje; }
    
    public String getArchivoBase64() { return archivoBase64; }
    public void setArchivoBase64(String archivoBase64) { this.archivoBase64 = archivoBase64; }
    
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    
    public Long getTamanoArchivo() { return tamanoArchivo; }
    public void setTamanoArchivo(Long tamanoArchivo) { this.tamanoArchivo = tamanoArchivo; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}