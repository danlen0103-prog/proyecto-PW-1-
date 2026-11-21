package com.reporte.reporteciudadano.dto;

import java.util.List;

public record ReporteDetalleResponse(ReporteResponse reporte, List<HistorialResponse> historial) {
}