package com.oftapp.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación exitosa")
public class LoginResponse {

    @Schema(description = "Token de sesión simulado", example = "jwt-simulado-oftapp-a1b2c3d4-e5f6-7890")
    private String token;

    @Schema(description = "Identificador del usuario", example = "1")
    private Long userId;

    @Schema(description = "Nombre completo del usuario", example = "Dra. Carla Ruiz")
    private String nombre;

    @Schema(description = "Rol del usuario en el sistema", example = "MEDICO")
    private String rol;

    @Schema(description = "RUT del usuario", example = "11.111.111-1")
    private String rut;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String nombre, String rol, String rut) {
        this.token = token;
        this.userId = userId;
        this.nombre = nombre;
        this.rol = rol;
        this.rut = rut;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }
}
