package com.oftapp.backend.controller;

import com.oftapp.backend.dto.LoginRequest;
import com.oftapp.backend.dto.LoginResponse;
import com.oftapp.backend.entity.UsuarioEntity;
import com.oftapp.backend.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints de login y emisión de credenciales seguras para OftAPP")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión en OftAPP", description = "Valida credenciales contra la base de datos y retorna los datos del usuario con token de sesión.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o correo no registrado",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Petición con campos vacíos o formato inválido")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", HttpStatus.UNAUTHORIZED.value(),
                    "error", "Unauthorized",
                    "message", "Credenciales incorrectas: Correo no registrado."
            ));
        }

        UsuarioEntity usuario = usuarioOpt.get();

        if (!usuario.getPassword().equals(request.getPassword().trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", HttpStatus.UNAUTHORIZED.value(),
                    "error", "Unauthorized",
                    "message", "Credenciales incorrectas: Contraseña no válida."
            ));
        }

        String simulatedToken = "jwt-simulado-oftapp-" + UUID.randomUUID();

        LoginResponse response = new LoginResponse(
                simulatedToken,
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getRut()
        );

        return ResponseEntity.ok(response);
    }
}
