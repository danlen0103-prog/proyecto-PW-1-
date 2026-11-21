package com.reporte.reporteciudadano.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.reporte.reporteciudadano.dto.ReporteDetalleResponse;
import com.reporte.reporteciudadano.dto.ReporteResponse;
import com.reporte.reporteciudadano.dto.VotoResponse;
import com.reporte.reporteciudadano.service.ReporteService;
import com.reporte.reporteciudadano.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Reportes", description = "Publicación, consulta, votos y gestión de los casos")
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService servicio;
    private final VotoService votoServicio;

    
    public ReporteController(ReporteService servicio, VotoService votoServicio) {
        this.servicio = servicio;
        this.votoServicio = votoServicio;
    }

    
    @Operation(summary = "Publicar un caso",
            description = "Formulario multipart con la foto (JPG o PNG, máximo 5 MB). Solo para el rol CIUDADANO.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Caso publicado"),
            @ApiResponse(responseCode = "400", description = "Datos o foto inválidos (detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "El rol no es CIUDADANO")})
    @PreAuthorize("hasRole('CIUDADANO')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReporteResponse> crear(@Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Id del tipo de problema") @RequestParam(value = "tipoId", required = false) Long tipoId,
            @Parameter(description = "Id del barrio") @RequestParam(value = "barrioId", required = false) Long barrioId,
            @Parameter(description = "Descripción de 10 a 500 caracteres") @RequestParam(value = "descripcion", required = false) String descripcion,
            @Parameter(description = "Dirección o referencia del lugar (máximo 150 caracteres)") @RequestParam(value = "referencia", required = false) String referencia,
            @Parameter(description = "Latitud entre -90 y 90", example = "2.9273") @RequestParam(value = "latitud", required = false) Double latitud,
            @Parameter(description = "Longitud entre -180 y 180", example = "-75.2819") @RequestParam(value = "longitud", required = false) Double longitud,
            @Parameter(description = "Foto del problema (JPG o PNG, máximo 5 MB)") @RequestParam(value = "foto", required = false) MultipartFile foto) {
        ReporteResponse creado = servicio.crear((Long) auth.getDetails(), tipoId, barrioId,
                descripcion, referencia, latitud, longitud, foto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    
    @Operation(summary = "Listar los casos publicados", description = "Consulta pública con filtros opcionales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de casos"),
            @ApiResponse(responseCode = "400", description = "Estado inválido")})
    @GetMapping
    public List<ReporteResponse> listar(
            @Parameter(description = "Id del tipo de problema") @RequestParam(value = "tipoId", required = false) Long tipoId,
            @Parameter(description = "Id del barrio") @RequestParam(value = "barrioId", required = false) Long barrioId,
            @Parameter(description = "RECIBIDO, EN_REVISION, EN_PROCESO o RESUELTO") @RequestParam(value = "estado", required = false) String estado) {
        return servicio.listarPublicos(tipoId, barrioId, estado);
    }

    
    @Operation(summary = "Mis casos",
            description = "Casos del ciudadano con su historial, comentario de cierre o motivo de rechazo.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Casos del ciudadano"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "El rol no es CIUDADANO")})
    @PreAuthorize("hasRole('CIUDADANO')")
    @GetMapping("/mis")
    public List<ReporteDetalleResponse> mis(@Parameter(hidden = true) Authentication auth) {
        return servicio.misCasos((Long) auth.getDetails());
    }

    
    @Operation(summary = "Detalle de un caso", description = "Consulta pública. Un caso rechazado responde 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Caso con su historial"),
            @ApiResponse(responseCode = "404", description = "El caso no existe")})
    @GetMapping("/{id}")
    public ReporteDetalleResponse detalle(@Parameter(description = "Id del caso") @PathVariable("id") Long id) {
        return servicio.detallePublico(id);
    }

   
    @Operation(summary = "Apoyar un caso",
            description = "No se puede apoyar un caso propio (403), repetir el voto (409) ni apoyar un caso resuelto (409).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voto registrado"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "Rol incorrecto o caso propio"),
            @ApiResponse(responseCode = "404", description = "El caso no existe"),
            @ApiResponse(responseCode = "409", description = "Voto repetido o caso resuelto")})
    @PreAuthorize("hasRole('CIUDADANO')")
    @PostMapping("/{id}/votos")
    public ResponseEntity<VotoResponse> apoyar(@Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Id del caso") @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(votoServicio.apoyar((Long) auth.getDetails(), id));
    }

    
    @Operation(summary = "Cambiar el estado de un caso",
            description = "Solo para el rol FUNCIONARIO. No se saltan pasos y los estados finales no cambian.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido, comentario faltante o foto inválida"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "El rol no es FUNCIONARIO"),
            @ApiResponse(responseCode = "404", description = "El caso no existe"),
            @ApiResponse(responseCode = "409", description = "Transición de estado no permitida")})
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PostMapping(value = "/{id}/estado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReporteResponse cambiarEstado(@Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Id del caso") @PathVariable("id") Long id,
            @Parameter(description = "Nuevo estado: EN_REVISION, EN_PROCESO, RESUELTO o RECHAZADO") @RequestParam(value = "estado", required = false) String estado,
            @Parameter(description = "Comentario de cierre o motivo del rechazo") @RequestParam(value = "comentario", required = false) String comentario,
            @Parameter(description = "Evidencia fotográfica de la solución (opcional)") @RequestParam(value = "foto", required = false) MultipartFile foto) {
        return servicio.cambiarEstado((Long) auth.getDetails(), id, estado, comentario, foto);
    }
}