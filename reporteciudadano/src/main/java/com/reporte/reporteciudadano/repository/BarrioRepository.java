package com.reporte.reporteciudadano.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.reporte.reporteciudadano.model.Barrio;

public interface BarrioRepository extends JpaRepository<Barrio, Long> {
    boolean existsByNombre(String nombre);
}