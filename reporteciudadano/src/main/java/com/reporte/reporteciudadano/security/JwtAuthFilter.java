package com.reporte.reporteciudadano.security;

import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de seguridad HTTP que intercepta cada petición para validar tokens JWT
 * enviados en la cabecera {@code Authorization} con el esquema {@code Bearer}.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Crea una instancia del filtro con el servicio encargado de verificar los tokens JWT.
     *
     * @param jwtService servicio para leer y validar tokens JWT
     */
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Extrae el token JWT de la petición entrante, verifica su validez y registra la
     * autenticación del usuario en el {@link SecurityContextHolder}. Si el token es
     * inválido o ha expirado, limpia el contexto y responde con estado {@code 401}.
     *
     * @param request  petición HTTP entrante
     * @param response respuesta HTTP saliente
     * @param chain    cadena de filtros de seguridad
     * @throws ServletException si ocurre un error en el procesamiento del servlet
     * @throws IOException      si ocurre un error de entrada/salida durante el filtro
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");

        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.leerClaims(cabecera.substring(7));
            String rol = claims.get("rol", String.class);
            Long id = ((Number) claims.get("id")).longValue();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
            auth.setDetails(id);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Token inválido o vencido\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}