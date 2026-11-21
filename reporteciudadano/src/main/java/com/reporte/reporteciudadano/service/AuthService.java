package com.reporte.reporteciudadano.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.reporte.reporteciudadano.dto.*;
import com.reporte.reporteciudadano.model.Rol;
import com.reporte.reporteciudadano.model.Usuario;
import com.reporte.reporteciudadano.repository.UsuarioRepository;
import com.reporte.reporteciudadano.security.JwtService;


@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    
    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

  
    public UsuarioResponse registrar(RegistroRequest datos) {
        String correo = datos.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        Usuario u = new Usuario();
        u.setNombre(datos.nombre().trim());
        u.setCorreo(correo);
        u.setClave(encoder.encode(datos.clave()));
        u.setRol(Rol.CIUDADANO);
        u = usuarios.save(u);
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol().name());
    }

    
    public AuthResponse login(LoginRequest datos) {
        Usuario u = usuarios.findByCorreoIgnoreCase(datos.correo().trim())
                .filter(x -> Boolean.TRUE.equals(x.getActivo()))
                .filter(x -> encoder.matches(datos.clave(), x.getClave()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Correo o clave incorrectos"));
        String token = jwtService.generarToken(u);
        return new AuthResponse(token, "Bearer", u.getId(), u.getNombre(), u.getCorreo(), u.getRol().name());
    }
}