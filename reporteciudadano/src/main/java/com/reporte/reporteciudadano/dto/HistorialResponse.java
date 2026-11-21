package com.reporte.reporteciudadano.dto;

import java.time.LocalDateTime;

public record HistorialResponse(String estadoAnterior, String estadoNuevo, LocalDateTime fecha,
        String funcionario, String comentario, String evidencia) {
}