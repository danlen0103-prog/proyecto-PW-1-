package com.reporte.reporteciudadano.dto;

public record EstadisticaBarrioResponse(Long barrioId, String barrio, long pendientes,
        long enProceso, long resueltos, long total) {
}