package com.oftapp.backend.repository;

import com.oftapp.backend.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {
    Optional<PacienteEntity> findByRut(String rut);
    boolean existsByRut(String rut);
}
