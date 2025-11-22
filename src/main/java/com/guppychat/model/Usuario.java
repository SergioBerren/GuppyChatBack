package com.guppychat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String password;
    private String clavePublica;
    private Boolean modoOscuro = false;

    public Usuario() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getClavePublica() { return clavePublica; }
    public void setClavePublica(String clavePublica) { this.clavePublica = clavePublica; }

    public Boolean getModoOscuro() { return modoOscuro; }
    public void setModoOscuro(Boolean modoOscuro) { this.modoOscuro = modoOscuro; }
}