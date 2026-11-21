package com.reporte.reporteciudadano.dto;

public record AuthResponse(String token, String tipo, Long id, String nombre, String correo, String rol) {
}