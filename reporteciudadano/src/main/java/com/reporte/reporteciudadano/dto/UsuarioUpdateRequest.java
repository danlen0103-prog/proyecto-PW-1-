package com.reporte.reporteciudadano.dto;

import com.reporte.reporteciudadano.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record UsuarioUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String nombre,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @NotNull(message = "El estado de la cuenta es obligatorio")
        Boolean activo) {
}