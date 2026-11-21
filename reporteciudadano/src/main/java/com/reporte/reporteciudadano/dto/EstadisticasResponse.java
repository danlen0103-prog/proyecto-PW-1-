package com.reporte.reporteciudadano.dto;

import java.util.List;

public record EstadisticasResponse(long pendientes, long enProceso, long resueltos, long total,
        List<EstadisticaBarrioResponse> porBarrio) {
}