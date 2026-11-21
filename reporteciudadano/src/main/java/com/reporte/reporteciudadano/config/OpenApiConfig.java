package com.reporte.reporteciudadano.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;


@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API de Reporte Ciudadano",
        version = "1.0",
        description = "Plataforma web de reporte ciudadano con priorización automática de problemas urbanos. "
                + "Los ciudadanos reportan problemas con foto y ubicación; los funcionarios los gestionan "
                + "según un puntaje de prioridad calculado automáticamente.",
        contact = @Contact(name = "Danna Valentina Alarcon Moreno · Universidad Surcolombiana")))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT que devuelve POST /api/auth/login. Se pega sin la palabra 'Bearer'.")
public class OpenApiConfig {
}