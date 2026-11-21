package com.reporte.reporteciudadano.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.reporte.reporteciudadano.dto.UsuarioAdminResponse;
import com.reporte.reporteciudadano.dto.UsuarioCreateRequest;
import com.reporte.reporteciudadano.dto.UsuarioUpdateRequest;
import com.reporte.reporteciudadano.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "Usuarios (administrador)", description = "Gestión de cuentas. Solo para el rol ADMIN")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService servicio;

    /**
     * @param servicio lógica de gestión de cuentas
     */
    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    /**
     * @return todas las cuentas, sin las claves
     */
    @Operation(summary = "Listar las cuentas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cuentas"),
            @ApiResponse(responseCode = "401", description = "Sin token o token vencido"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")})
    @GetMapping
    public List<UsuarioAdminResponse> listar() {
        return servicio.listar();
    }

    /**
     * @param id identificador de la cuenta
     * @return la cuenta
     */
    @Operation(summary = "Ver una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La cuenta"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")})
    @GetMapping("/{id}")
    public UsuarioAdminResponse ver(@Parameter(description = "Id de la cuenta") @PathVariable Long id) {
        return servicio.obtener(id);
    }

    /**
     * @param datos nombre, correo, clave y rol de la cuenta nueva
     * @return la cuenta creada con estado HTTP 201
     */
    @Operation(summary = "Crear una cuenta con cualquier rol", description = "Sirve, por ejemplo, para crear funcionarios sin tocar el código.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "409", description = "Ya existe una cuenta con ese correo")})
    @PostMapping
    public ResponseEntity<UsuarioAdminResponse> crear(@Valid @RequestBody UsuarioCreateRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos));
    }

    /**
     * @param auth  sesión del administrador
     * @param id    identificador de la cuenta
     * @param datos nuevo nombre, rol y estado (activa o desactivada)
     * @return la cuenta actualizada
     */
    @Operation(summary = "Modificar una cuenta", description = "Cambia nombre, rol y estado. El administrador no puede quitarse su propio rol ni desactivarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe"),
            @ApiResponse(responseCode = "409", description = "Intento de quitarse el rol o desactivar la propia cuenta")})
    @PutMapping("/{id}")
    public UsuarioAdminResponse actualizar(@Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Id de la cuenta") @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest datos) {
        return servicio.actualizar((Long) auth.getDetails(), id, datos);
    }

    /**
     * @param auth sesión del administrador
     * @param id   identificador de la cuenta a eliminar
     * @return estado HTTP 204 sin cuerpo
     */
    @Operation(summary = "Eliminar una cuenta", description = "Falla con 409 si la cuenta ya tiene reportes, votos o historial; en ese caso se desactiva.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta eliminada"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe"),
            @ApiResponse(responseCode = "409", description = "Es la cuenta propia o está en uso")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Id de la cuenta") @PathVariable Long id) {
        servicio.eliminar((Long) auth.getDetails(), id);
        return ResponseEntity.noContent().build();
    }
}