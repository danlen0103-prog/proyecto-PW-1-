package com.reporte.reporteciudadano.dto;

/**
 *
 * @param id     identificador de la cuenta
 * @param nombre nombre de la persona
 * @param correo correo con el que ingresa
 * @param rol    CIUDADANO, FUNCIONARIO o ADMIN
 * @param activo {@code false} si la cuenta fue desactivada y ya no puede ingresar
 */
public record UsuarioAdminResponse(Long id, String nombre, String correo, String rol, boolean activo) {
}