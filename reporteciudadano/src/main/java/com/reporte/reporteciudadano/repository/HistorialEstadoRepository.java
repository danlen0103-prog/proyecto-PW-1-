package com.reporte.reporteciudadano.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reporte.reporteciudadano.model.HistorialEstado;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findByReporteIdOrderByFechaAsc(Long reporteId);
}