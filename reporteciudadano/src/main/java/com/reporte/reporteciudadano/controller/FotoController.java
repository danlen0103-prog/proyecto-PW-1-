package com.reporte.reporteciudadano.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.reporte.reporteciudadano.service.FotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Fotos", description = "Descarga pública de las imágenes de los casos")
@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    private final FotoService fotos;

    
    public FotoController(FotoService fotos) {
        this.fotos = fotos;
    }

    
    @Operation(summary = "Ver una foto", description = "Consulta pública. El nombre es un identificador aleatorio con extensión .jpg o .png.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La imagen"),
            @ApiResponse(responseCode = "404", description = "La foto no existe")})
    @GetMapping("/{nombre}")
    public ResponseEntity<Resource> ver(@Parameter(description = "Nombre del archivo") @PathVariable("nombre") String nombre) {
        Resource recurso = fotos.cargar(nombre);
        MediaType tipo = nombre.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo)
                .header("Cache-Control", "public, max-age=86400").body(recurso);
    }
}