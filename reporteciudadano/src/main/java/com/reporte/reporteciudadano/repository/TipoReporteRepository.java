package com.reporte.reporteciudadano.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.reporte.reporteciudadano.model.TipoReporte;

public interface TipoReporteRepository extends JpaRepository<TipoReporte, Long> {
    boolean existsByNombre(String nombre);
}