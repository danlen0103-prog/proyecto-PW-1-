package com.reporte.reporteciudadano.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.reporte.reporteciudadano.model.Voto;

public interface VotoRepository extends JpaRepository<Voto, Long> {
    boolean existsByUsuarioIdAndReporteId(Long usuarioId, Long reporteId);
    long countByReporteId(Long reporteId);
}