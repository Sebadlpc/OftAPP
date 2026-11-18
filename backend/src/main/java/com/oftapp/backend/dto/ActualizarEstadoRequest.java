package com.oftapp.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Petición para actualizar el estado y observaciones de un examen")
public class ActualizarEstadoRequest {

    @Schema(description = "Nuevo estado del examen", example = "VALIDADO", defaultValue = "VALIDADO")
    private String estado;

    @Schema(description = "Observaciones o notas clínicas actualizadas", example = "Examen validado conforme a protocolo clínico.")
    private String notas;

    public ActualizarEstadoRequest() {
        this.estado = "VALIDADO";
    }

    public ActualizarEstadoRequest(String estado, String notas) {
        this.estado = estado != null ? estado : "VALIDADO";
        this.notas = notas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
