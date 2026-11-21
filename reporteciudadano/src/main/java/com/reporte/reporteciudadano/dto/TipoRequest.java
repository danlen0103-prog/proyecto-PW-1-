package com.reporte.reporteciudadano.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre admite máximo 60 caracteres")
        String nombre,

        @NotNull(message = "El peso es obligatorio")
        @Min(value = 1, message = "El peso mínimo es 1")
        @Max(value = 5, message = "El peso máximo es 5")
        Integer peso) {
}