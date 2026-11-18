package com.oftapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidad JPA para los usuarios del sistema (médicos, tecnólogos, administradores y pacientes).
 */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "El nombre completo es requerido")
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @NotBlank(message = "El RUT es requerido")
    @Column(nullable = false)
    private String rut;

    @NotBlank(message = "El rol es requerido")
    @Column(nullable = false)
    private String rol; // MEDICO, TECNOLOGO, ADMIN, PACIENTE

    public UsuarioEntity() {
    }

    public UsuarioEntity(Long id, String email, String password, String nombreCompleto, String rut, String rol) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.rut = rut;
        this.rol = rol;
    }

    public UsuarioEntity(String email, String password, String nombreCompleto, String rut, String rol) {
        this.email = email;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.rut = rut;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
