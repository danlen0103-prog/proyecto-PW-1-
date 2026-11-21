package com.reporte.reporteciudadano.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.reporte.reporteciudadano.dto.*;
import com.reporte.reporteciudadano.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "Autenticación", description = "Registro público e inicio de sesión único para los tres roles")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    
    @Operation(summary = "Registrar un ciudadano",
            description = "Crea una cuenta con el rol CIUDADANO. La clave se guarda cifrada con BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos o inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una cuenta con ese correo")})
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(datos));
    }


    @Operation(summary = "Iniciar sesión",
            description = "Devuelve un token JWT con el rol del usuario. Se envía después en la cabecera Authorization: Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingreso correcto"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos"),
            @ApiResponse(responseCode = "401", description = "Correo o clave incorrectos")})
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }
}