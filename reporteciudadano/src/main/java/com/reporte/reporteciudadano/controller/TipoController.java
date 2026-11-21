package com.reporte.reporteciudadano.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.reporte.reporteciudadano.dto.TipoRequest;
import com.reporte.reporteciudadano.dto.TipoResponse;
import com.reporte.reporteciudadano.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "Tipos de problema", description = "Lista pública; el CRUD es solo para el rol ADMIN")
@RestController
@RequestMapping("/api/tipos")
public class TipoController {

    private final CatalogoService servicio;

    
    public TipoController(CatalogoService servicio) {
        this.servicio = servicio;
    }

    
    @Operation(summary = "Listar los tipos de problema", description = "Consulta pública.")
    @ApiResponse(responseCode = "200", description = "Lista de tipos")
    @GetMapping
    public List<TipoResponse> listar() {
        return servicio.listarTipos();
    }

    
    @Operation(summary = "Crear un tipo de problema", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "409", description = "Ya existe uno con ese nombre")})
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TipoResponse> crear(@Valid @RequestBody TipoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crearTipo(datos));
    }

    
    @Operation(summary = "Modificar un tipo de problema", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe")})
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TipoResponse actualizar(@Parameter(description = "Identificador del registro", example = "1") @PathVariable Long id,
                                   @Valid @RequestBody TipoRequest datos) {
        return servicio.actualizarTipo(id, datos);
    }

    
    @Operation(summary = "Eliminar un tipo de problema", description = "Falla con 409 si algún reporte ya lo usa.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "Está en uso")})
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador del registro", example = "1") @PathVariable Long id) {
        servicio.eliminarTipo(id);
        return ResponseEntity.noContent().build();
    }
}