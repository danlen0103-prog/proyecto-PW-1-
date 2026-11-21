package com.reporte.reporteciudadano.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.reporte.reporteciudadano.dto.ReporteResponse;
import com.reporte.reporteciudadano.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Panel del funcionario", description = "Casos ordenados por prioridad (FUNCIONARIO y ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/panel")
@PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
public class PanelController {

    private final ReporteService servicio;

   
    public PanelController(ReporteService servicio) {
        this.servicio = servicio;
    }

   
    @Operation(summary = "Casos ordenados por prioridad",
            description = "puntaje = (peso del tipo × 3) + (votos × 2) + (días sin resolver × 1).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Casos ordenados por puntaje"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "El rol no es FUNCIONARIO ni ADMIN")})
    @GetMapping("/reportes")
    public List<ReporteResponse> reportes(
            @Parameter(description = "Id del tipo de problema") @RequestParam(value = "tipoId", required = false) Long tipoId,
            @Parameter(description = "Id del barrio") @RequestParam(value = "barrioId", required = false) Long barrioId,
            @Parameter(description = "RECIBIDO, EN_REVISION, EN_PROCESO, RESUELTO o RECHAZADO") @RequestParam(value = "estado", required = false) String estado) {
        return servicio.panel(tipoId, barrioId, estado);
    }
}