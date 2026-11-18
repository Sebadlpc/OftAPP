package com.oftapp.backend.controller;

import com.oftapp.backend.entity.PacienteEntity;
import com.oftapp.backend.repository.PacienteRepository;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pacientes")
@Tag(name = "Pacientes", description = "Endpoints para la gestión y consulta de fichas de pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;

    public PacienteController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @GetMapping
    @Operation(summary = "Listar pacientes", description = "Retorna la lista completa de pacientes registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PacienteEntity.class))))
    public ResponseEntity<List<PacienteEntity>> listarTodos() {
        return ResponseEntity.ok(pacienteRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo paciente", description = "Registra una nueva ficha de paciente en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paciente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PacienteEntity.class))),
            @ApiResponse(responseCode = "400", description = "Datos de paciente inválidos o RUT duplicado")
    })
    public ResponseEntity<?> crearPaciente(@Valid @RequestBody PacienteEntity paciente) {
        if (pacienteRepository.existsByRut(paciente.getRut().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "error", "Bad Request",
                    "message", "Ya existe un paciente registrado con el RUT: " + paciente.getRut()
            ));
        }

        paciente.setId(null);
        PacienteEntity nuevo = pacienteRepository.save(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }
}
