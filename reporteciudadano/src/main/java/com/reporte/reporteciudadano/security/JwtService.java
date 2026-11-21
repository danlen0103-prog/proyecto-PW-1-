package com.reporte.reporteciudadano.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.reporte.reporteciudadano.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio encargado de generar, interpretar y validar tokens JWT firmados
 * mediante HMAC-SHA para la autenticación de usuarios en la aplicación.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    /**
     * Inicializa la clave secreta y el tiempo de expiración a partir de las
     * propiedades de configuración de la aplicación.
     *
     * @param secreto      clave secreta configurada en {@code app.jwt.secret}
     * @param expiracionMs tiempo de vida del token en milisegundos ({@code app.jwt.expiration-ms})
     */
    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    /**
     * Genera un token JWT firmado con el correo como sujeto, el identificador del usuario
     * y su rol, estableciendo fecha de emisión y expiración.
     *
     * @param usuario entidad del usuario autenticado
     * @return token JWT compacto y firmado
     */
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("id", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Verifica la firma del token JWT y devuelve el conjunto de claims contenidos en él.
     *
     * @param token cadena del token JWT sin el prefijo {@code Bearer }
     * @return claims extraídos del cuerpo del token
     * @throws JwtException si el token está mal formado, expirado o su firma no es válida
     */
    public Claims leerClaims(String token) {
        return Jwts.parser().verifyWith(clave).build()
                .parseSignedClaims(token).getPayload();
    }

    /**
     * Comprueba si un token JWT tiene firma válida, estructura correcta y no ha vencido.
     *
     * @param token cadena del token JWT a evaluar
     * @return {@code true} si el token es válido; {@code false} en caso contrario
     */
    public boolean esValido(String token) {
        try {
            leerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}