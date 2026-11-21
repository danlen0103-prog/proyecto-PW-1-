package com.reporte.reporteciudadano.dto;

import java.time.LocalDateTime;

public record ReporteResponse(Long id, String descripcion, String referencia, String foto,
        Double latitud, Double longitud, String estado, LocalDateTime fecha, Integer votos,
        Integer puntaje, String comentarioEstado, String fotoSolucion,
        TipoResponse tipo, BarrioResponse barrio) {
}