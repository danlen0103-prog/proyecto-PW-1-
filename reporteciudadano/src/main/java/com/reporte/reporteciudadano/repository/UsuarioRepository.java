package com.reporte.reporteciudadano.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reporte.reporteciudadano.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
}