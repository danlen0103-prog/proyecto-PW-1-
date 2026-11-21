package com.reporte.reporteciudadano.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.reporte.reporteciudadano.dto.EstadisticasResponse;
import com.reporte.reporteciudadano.service.EstadisticasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Estadísticas", description = "Resumen y tabla por barrio (FUNCIONARIO y ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/estadisticas")
@PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
public class EstadisticasController {

    private final EstadisticasService servicio;

   
    public EstadisticasController(EstadisticasService servicio) {
        this.servicio = servicio;
    }

   
    @Operation(summary = "Estadísticas por barrio",
            description = "Pendientes incluye RECIBIDO y EN_REVISION. Los casos rechazados no se cuentan.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen y tabla por barrio"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "El rol no es FUNCIONARIO ni ADMIN")})
    @GetMapping
    public EstadisticasResponse resumen() {
        return servicio.resumen();
    }
}