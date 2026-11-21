package com.reporte.reporteciudadano.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.reporte.reporteciudadano.model.Estado;
import com.reporte.reporteciudadano.model.Reporte;

public interface ReporteRepository extends JpaRepository<Reporte, Long>, JpaSpecificationExecutor<Reporte> {

    List<Reporte> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

  
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Reporte r SET r.votos = r.votos + 1 WHERE r.id = :id")
    void sumarVoto(@Param("id") Long id);

   
    @Query("SELECT r.barrio.id, r.estado, COUNT(r) FROM Reporte r "
            + "WHERE r.estado <> :excluido GROUP BY r.barrio.id, r.estado")
    List<Object[]> contarPorBarrioYEstado(@Param("excluido") Estado excluido);
}