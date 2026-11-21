package com.reporte.reporteciudadano.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.reporte.reporteciudadano.dto.UsuarioAdminResponse;
import com.reporte.reporteciudadano.dto.UsuarioCreateRequest;
import com.reporte.reporteciudadano.dto.UsuarioUpdateRequest;
import com.reporte.reporteciudadano.model.Rol;
import com.reporte.reporteciudadano.model.Usuario;
import com.reporte.reporteciudadano.repository.UsuarioRepository;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    /**
     * @param usuarios acceso a la tabla de usuarios
     * @param encoder  cifrador de claves (BCrypt)
     */
    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    /** @return todas las cuentas ordenadas por nombre */
    public List<UsuarioAdminResponse> listar() {
        return usuarios.findAll(Sort.by("nombre")).stream().map(this::aRespuesta).toList();
    }

    /**
     * @param id identificador de la cuenta
     * @return la cuenta
     * @throws ResponseStatusException 404 si no existe
     */
    public UsuarioAdminResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    /**
     * Crea una cuenta con el rol indicado. La clave se guarda cifrada con BCrypt.
     *
     * @param d nombre, correo, clave y rol
     * @return la cuenta creada
     * @throws ResponseStatusException 409 si el correo ya está registrado
     */
    public UsuarioAdminResponse crear(UsuarioCreateRequest d) {
        String correo = d.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        Usuario u = new Usuario();
        u.setNombre(d.nombre().trim());
        u.setCorreo(correo);
        u.setClave(encoder.encode(d.clave()));
        u.setRol(d.rol());
        u.setActivo(true);
        return aRespuesta(usuarios.save(u));
    }

    /**
     *
     * @param adminId identificador del administrador que hace el cambio
     * @param id      identificador de la cuenta a modificar
     * @param d       nuevos datos
     * @return la cuenta actualizada
     * @throws ResponseStatusException 404 si no existe; 409 si el administrador intenta quitarse su rol o desactivarse
     */
    public UsuarioAdminResponse actualizar(Long adminId, Long id, UsuarioUpdateRequest d) {
        Usuario u = buscar(id);
        if (u.getId().equals(adminId) && (d.rol() != Rol.ADMIN || !d.activo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No puedes quitarte el rol de administrador ni desactivar tu propia cuenta");
        }
        u.setNombre(d.nombre().trim());
        u.setRol(d.rol());
        u.setActivo(d.activo());
        return aRespuesta(usuarios.save(u));
    }

    /**
     *
     * @param adminId identificador del administrador que hace el cambio
     * @param id      identificador de la cuenta a eliminar
     * @throws ResponseStatusException 404 si no existe; 409 si intenta eliminar su propia cuenta
     */
    public void eliminar(Long adminId, Long id) {
        if (id.equals(adminId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta");
        }
        buscar(id);
        usuarios.deleteById(id);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));
    }

    private UsuarioAdminResponse aRespuesta(Usuario u) {
        return new UsuarioAdminResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol().name(),
                Boolean.TRUE.equals(u.getActivo()));
    }
}