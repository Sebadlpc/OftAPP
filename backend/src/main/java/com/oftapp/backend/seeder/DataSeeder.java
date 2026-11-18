package com.oftapp.backend.seeder;

import com.oftapp.backend.entity.ExamenEntity;
import com.oftapp.backend.entity.PacienteEntity;
import com.oftapp.backend.entity.UsuarioEntity;
import com.oftapp.backend.repository.ExamenRepository;
import com.oftapp.backend.repository.PacienteRepository;
import com.oftapp.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Inicializador de datos semilla (Seed Data) al arrancar el contexto de Spring Boot.
 * Carga usuarios clínicos, pacientes de muestra y exámenes oftalmológicos.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ExamenRepository examenRepository;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      PacienteRepository pacienteRepository,
                      ExamenRepository examenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.examenRepository = examenRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Inicializando datos semilla en OftAPP Backend...");

        // 1. Usuarios del sistema
        if (usuarioRepository.count() == 0) {
            List<UsuarioEntity> usuarios = Arrays.asList(
                    new UsuarioEntity("medico@oftapp.cl", "OftApp2026!", "Dra. Carla Ruiz", "11.111.111-1", "MEDICO"),
                    new UsuarioEntity("tecnologo@oftapp.cl", "OftApp2026!", "Lic. Andrés Gómez", "22.222.222-2", "TECNOLOGO"),
                    new UsuarioEntity("admin@oftapp.cl", "OftApp2026!", "Carlos Vega", "33.333.333-3", "ADMIN"),
                    new UsuarioEntity("paciente@oftapp.cl", "OftApp2026!", "Juan Pérez", "12.345.678-9", "PACIENTE")
            );
            usuarioRepository.saveAll(usuarios);
            log.info("Se cargaron {} usuarios clínicos de prueba.", usuarios.size());
        }

        // 2. Pacientes simulados
        List<PacienteEntity> pacientesGuardados;
        if (pacienteRepository.count() == 0) {
            List<PacienteEntity> pacientes = Arrays.asList(
                    new PacienteEntity("12.345.678-9", "Juan Pérez", "paciente@oftapp.cl", "+56911223344"),
                    new PacienteEntity("15.432.987-K", "María López", "maria.lopez@email.com", "+56922334455"),
                    new PacienteEntity("9.876.543-2", "Carlos Gómez", "carlos.gomez@email.com", "+56933445566"),
                    new PacienteEntity("18.765.432-1", "Ana Martínez", "ana.martinez@email.com", "+56944556677")
            );
            pacientesGuardados = pacienteRepository.saveAll(pacientes);
            log.info("Se cargaron {} pacientes simulados.", pacientesGuardados.size());
        } else {
            pacientesGuardados = pacienteRepository.findAll();
        }

        // 3. Exámenes oftalmológicos simulados vinculados a los pacientes
        if (examenRepository.count() == 0 && pacientesGuardados.size() >= 4) {
            PacienteEntity p1 = pacientesGuardados.get(0);
            PacienteEntity p2 = pacientesGuardados.get(1);
            PacienteEntity p3 = pacientesGuardados.get(2);
            PacienteEntity p4 = pacientesGuardados.get(3);

            List<ExamenEntity> examenes = Arrays.asList(
                    new ExamenEntity(
                            p1.getId(),
                            p1.getNombre(),
                            "Fondo de Ojo",
                            "OD",
                            "2026-11-10T10:00:00Z",
                            "VALIDADO",
                            "Dra. Carla Ruiz",
                            "Retinopatía diabética no proliferativa leve. Control en 6 meses.",
                            "https://oftapp.cl/docs/examen-1.pdf"
                    ),
                    new ExamenEntity(
                            p2.getId(),
                            p2.getNombre(),
                            "Campimetría Computarizada",
                            "OI",
                            "2026-11-12T11:30:00Z",
                            "PENDIENTE",
                            "Dra. Carla Ruiz",
                            "Sospecha de escotoma paracentral en ojo izquierdo.",
                            null
                    ),
                    new ExamenEntity(
                            p3.getId(),
                            p3.getNombre(),
                            "OCT Mácula",
                            "Bifocal",
                            "2026-11-14T09:15:00Z",
                            "VALIDADO",
                            "Dra. Carla Ruiz",
                            "Grosor foveal dentro de límites normales en ambos ojos.",
                            "https://oftapp.cl/docs/examen-3.pdf"
                    ),
                    new ExamenEntity(
                            p4.getId(),
                            p4.getNombre(),
                            "Tonometría de Aplanación",
                            "OD",
                            "2026-11-15T16:00:00Z",
                            "PENDIENTE",
                            "Lic. Andrés Gómez",
                            "Presión intraocular elevada (22 mmHg en OD, 16 mmHg en OI).",
                            null
                    )
            );
            examenRepository.saveAll(examenes);
            log.info("Se cargaron {} exámenes oftalmológicos iniciales.", examenes.size());
        }

        log.info("Datos semilla cargados exitosamente.");
    }
}
