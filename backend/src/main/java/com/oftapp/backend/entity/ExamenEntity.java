package com.oftapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad JPA para el registro y trazabilidad de exámenes oftalmológicos.
 */
@Entity
@Table(name = "examenes")
public class ExamenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El pacienteId es requerido")
    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @NotBlank(message = "El nombre del paciente es requerido")
    @Column(name = "paciente_nombre", nullable = false)
    private String pacienteNombre;

    @NotBlank(message = "El tipo de examen es requerido")
    @Column(name = "tipo_examen", nullable = false)
    private String tipoExamen;

    @NotBlank(message = "El ojo evaluado es requerido")
    @Column(name = "ojo_evaluado", nullable = false)
    private String ojoEvaluado; // "OD", "OI", "Bifocal"

    @NotBlank(message = "La fecha es requerida")
    @Column(nullable = false)
    private String fecha; // Formato ISO 8601 (ej. "2026-11-10T10:00:00Z")

    @NotBlank(message = "El estado es requerido")
    @Column(nullable = false)
    private String estado; // "PENDIENTE", "VALIDADO", "SINCRONIZADO"

    @NotBlank(message = "El profesional/doctor es requerido")
    @Column(nullable = false)
    private String doctor;

    @Column(length = 2000)
    private String notas;

    @Column(name = "documento_url", length = 1000)
    private String documentoUrl; // URL del informe en PDF o imagen clínica (opcional)

    public ExamenEntity() {
    }

    public ExamenEntity(Long id, Long pacienteId, String pacienteNombre, String tipoExamen,
                        String ojoEvaluado, String fecha, String estado, String doctor,
                        String notas, String documentoUrl) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.pacienteNombre = pacienteNombre;
        this.tipoExamen = tipoExamen;
        this.ojoEvaluado = ojoEvaluado;
        this.fecha = fecha;
        this.estado = estado;
        this.doctor = doctor;
        this.notas = notas;
        this.documentoUrl = documentoUrl;
    }

    public ExamenEntity(Long pacienteId, String pacienteNombre, String tipoExamen,
                        String ojoEvaluado, String fecha, String estado, String doctor,
                        String notas, String documentoUrl) {
        this.pacienteId = pacienteId;
        this.pacienteNombre = pacienteNombre;
        this.tipoExamen = tipoExamen;
        this.ojoEvaluado = ojoEvaluado;
        this.fecha = fecha;
        this.estado = estado;
        this.doctor = doctor;
        this.notas = notas;
        this.documentoUrl = documentoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public String getTipoExamen() {
        return tipoExamen;
    }

    public void setTipoExamen(String tipoExamen) {
        this.tipoExamen = tipoExamen;
    }

    public String getOjoEvaluado() {
        return ojoEvaluado;
    }

    public void setOjoEvaluado(String ojoEvaluado) {
        this.ojoEvaluado = ojoEvaluado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getDocumentoUrl() {
        return documentoUrl;
    }

    public void setDocumentoUrl(String documentoUrl) {
        this.documentoUrl = documentoUrl;
    }
}
