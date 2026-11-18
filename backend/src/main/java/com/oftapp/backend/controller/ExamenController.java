package com.oftapp.backend.controller;

import com.oftapp.backend.dto.ActualizarEstadoRequest;
import com.oftapp.backend.entity.ExamenEntity;
import com.oftapp.backend.exception.ResourceNotFoundException;
import com.oftapp.backend.repository.ExamenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Exámenes", description = "Endpoints para registro, consulta, validación y sincronización de exámenes oftalmológicos")
public class ExamenController {

    private final ExamenRepository examenRepository;

    public ExamenController(ExamenRepository examenRepository) {
        this.examenRepository = examenRepository;
    }

    @GetMapping("/examenes")
    @Operation(
            summary = "Listar exámenes clínicos",
            description = "Obtiene los exámenes oftalmológicos. Permite filtrar por pacienteId y por rol (si rol=PACIENTE, restringe los exámenes estrictamente al pacienteId)."
    )
    @ApiResponse(responseCode = "200", description = "Lista de exámenes obtenida",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExamenEntity.class))))
    public ResponseEntity<List<ExamenEntity>> listarExamenes(
            @Parameter(description = "ID del paciente para filtrar")
            @RequestParam(required = false) Long pacienteId,
            @Parameter(description = "Rol del usuario que consulta (MEDICO, TECNOLOGO, ADMIN, PACIENTE)")
            @RequestParam(required = false) String rol
    ) {
        // Regla de seguridad clínica: Si el rol es PACIENTE, requiere filtrar estrictamente por su pacienteId
        if (rol != null && "PACIENTE".equalsIgnoreCase(rol.trim())) {
            if (pacienteId == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(examenRepository.findByPacienteId(pacienteId));
        }

        if (pacienteId != null) {
            return ResponseEntity.ok(examenRepository.findByPacienteId(pacienteId));
        }

        return ResponseEntity.ok(examenRepository.findAll());
    }

    @GetMapping("/examenes/{id}")
    @Operation(summary = "Obtener detalle de un examen", description = "Retorna la información completa de un examen según su identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Examen encontrado",
                    content = @Content(schema = @Schema(implementation = ExamenEntity.class))),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    })
    public ResponseEntity<ExamenEntity> obtenerPorId(@PathVariable Long id) {
        ExamenEntity examen = examenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen clínico no encontrado con ID: " + id));
        return ResponseEntity.ok(examen);
    }

    @PostMapping("/examenes")
    @Operation(
            summary = "Registrar nuevo examen oftalmológico",
            description = "Crea un nuevo examen clínico. Los pacientes tienen prohibido registrar exámenes (retorna 403 Forbidden)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Examen registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = ExamenEntity.class))),
            @ApiResponse(responseCode = "403", description = "Permiso denegado: Pacientes no pueden registrar exámenes"),
            @ApiResponse(responseCode = "400", description = "Datos de examen incompletos o inválidos")
    })
    public ResponseEntity<?> registrarExamen(
            @Parameter(description = "Rol del usuario que emite la petición (ej. MEDICO o TECNOLOGO)")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody ExamenEntity examen
    ) {
        // Validación de permisos por rol
        if (userRole != null && "PACIENTE".equalsIgnoreCase(userRole.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", "Los pacientes no tienen permiso para registrar exámenes clínicos en OftAPP."
            ));
        }

        examen.setId(null);
        if (examen.getEstado() == null || examen.getEstado().isBlank()) {
            examen.setEstado("PENDIENTE");
        }

        ExamenEntity nuevo = examenRepository.save(examen);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/examenes/{id}/estado")
    @Operation(
            summary = "Actualizar estado y notas de un examen",
            description = "Permite a los profesionales actualizar el estado clínico a 'VALIDADO' y registrar las notas u observaciones."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de examen actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ExamenEntity.class))),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    })
    public ResponseEntity<ExamenEntity> actualizarEstado(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoRequest request
    ) {
        ExamenEntity examen = examenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen no encontrado con ID: " + id));

        String nuevoEstado = (request.getEstado() != null && !request.getEstado().isBlank())
                ? request.getEstado().trim().toUpperCase()
                : "VALIDADO";

        examen.setEstado(nuevoEstado);

        if (request.getNotas() != null && !request.getNotas().isBlank()) {
            examen.setNotas(request.getNotas().trim());
        }

        ExamenEntity guardado = examenRepository.save(examen);
        return ResponseEntity.ok(guardado);
    }

    @PostMapping({"/sync", "/examenes/sync"})
    @Operation(
            summary = "Sincronizar exámenes desde modo offline",
            description = "Recibe un lote de exámenes capturados localmente en la app Android (Room SQLite), los persiste en la base de datos central y los retorna con estado 'SINCRONIZADO'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exámenes sincronizados exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExamenEntity.class)))),
            @ApiResponse(responseCode = "400", description = "Lista de sincronización inválida")
    })
    public ResponseEntity<List<ExamenEntity>> sincronizarLote(
            @RequestBody List<ExamenEntity> examenesOffline
    ) {
        if (examenesOffline == null || examenesOffline.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        for (ExamenEntity examen : examenesOffline) {
            examen.setId(null); // Genera ID nuevo remoto
            examen.setEstado("SINCRONIZADO");
        }

        List<ExamenEntity> sincronizados = examenRepository.saveAll(examenesOffline);
        return ResponseEntity.ok(sincronizados);
    }
}
