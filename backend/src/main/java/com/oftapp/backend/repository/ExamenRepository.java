package com.oftapp.backend.repository;

import com.oftapp.backend.entity.ExamenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenRepository extends JpaRepository<ExamenEntity, Long> {
    List<ExamenEntity> findByPacienteId(Long pacienteId);
    List<ExamenEntity> findByEstado(String estado);
    List<ExamenEntity> findByPacienteIdAndEstado(Long pacienteId, String estado);
}
