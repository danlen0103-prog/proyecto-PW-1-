package com.reporte.reporteciudadano.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.reporte.reporteciudadano.dto.*;
import com.reporte.reporteciudadano.exception.ValidacionException;
import com.reporte.reporteciudadano.model.*;
import com.reporte.reporteciudadano.repository.*;
import jakarta.persistence.criteria.Predicate;

/**
 * Servicio de negocio que gestiona el ciclo de vida de los reportes ciudadanos:
 * creación, consulta pública y personal, priorización en el panel de gestión y
 * transiciones de estado.
 */
@Service
public class ReporteService {

    private static final int DESC_MIN = 10, DESC_MAX = 500, REF_MAX = 150, COM_MAX = 500;

    /**
     * Mapa de transiciones de estado permitidas para un reporte.
     */
    private static final Map<Estado, Set<Estado>> TRANSICIONES = Map.of(
            Estado.RECIBIDO, Set.of(Estado.EN_REVISION, Estado.RECHAZADO),
            Estado.EN_REVISION, Set.of(Estado.EN_PROCESO, Estado.RECHAZADO),
            Estado.EN_PROCESO, Set.of(Estado.RESUELTO, Estado.RECHAZADO));

    private final ReporteRepository reportes;
    private final TipoReporteRepository tipos;
    private final BarrioRepository barrios;
    private final UsuarioRepository usuarios;
    private final HistorialEstadoRepository historial;
    private final FotoService fotos;
    private final PuntajeService puntaje;

    /**
     * Construye el servicio de reportes con sus repositorios y servicios auxiliares.
     *
     * @param reportes  repositorio de reportes ciudadanos
     * @param tipos     repositorio de tipos de reporte
     * @param barrios   repositorio de barrios
     * @param usuarios  repositorio de usuarios
     * @param historial repositorio del historial de estados
     * @param fotos     servicio de validación y almacenamiento de evidencias fotográficas
     * @param puntaje   servicio encargado de calcular el puntaje de prioridad de cada reporte
     */
    public ReporteService(ReporteRepository reportes, TipoReporteRepository tipos, BarrioRepository barrios,
                          UsuarioRepository usuarios, HistorialEstadoRepository historial,
                          FotoService fotos, PuntajeService puntaje) {
        this.reportes = reportes;
        this.tipos = tipos;
        this.barrios = barrios;
        this.usuarios = usuarios;
        this.historial = historial;
        this.fotos = fotos;
        this.puntaje = puntaje;
    }

    /**
     * Valida los datos de entrada y registra un nuevo reporte ciudadano en estado
     * {@link Estado#RECIBIDO} junto con su fotografía adjunta.
     *
     * @param usuarioId   identificador del usuario que crea el reporte
     * @param tipoId      identificador del tipo de problema reportado
     * @param barrioId    identificador del barrio donde ocurre el problema
     * @param descripcion descripción detallada del reporte
     * @param referencia  dirección o punto de referencia en el barrio
     * @param latitud     coordenada de latitud geográfica
     * @param longitud    coordenada de longitud geográfica
     * @param foto        archivo de imagen asociado al reporte
     * @return representación de respuesta del reporte creado
     * @throws ValidacionException       si alguno de los campos o la imagen no cumple las reglas de validación
     * @throws ResponseStatusException si el usuario autenticado no existe
     */
    @Transactional
    public ReporteResponse crear(Long usuarioId, Long tipoId, Long barrioId, String descripcion,
                                 String referencia, Double latitud, Double longitud, MultipartFile foto) {
        Map<String, String> errores = new LinkedHashMap<>();

        TipoReporte tipo = null;
        if (tipoId == null) {
            errores.put("tipoId", "El tipo de problema es obligatorio");
        } else {
            tipo = tipos.findById(tipoId).orElse(null);
            if (tipo == null) errores.put("tipoId", "El tipo de problema no existe");
        }

        Barrio barrio = null;
        if (barrioId == null) {
            errores.put("barrioId", "El barrio es obligatorio");
        } else {
            barrio = barrios.findById(barrioId).orElse(null);
            if (barrio == null) errores.put("barrioId", "El barrio no existe");
        }

        String desc = descripcion == null ? "" : descripcion.trim();
        if (desc.isEmpty()) {
            errores.put("descripcion", "La descripción es obligatoria");
        } else if (desc.length() < DESC_MIN || desc.length() > DESC_MAX) {
            errores.put("descripcion", "La descripción debe tener entre " + DESC_MIN + " y " + DESC_MAX + " caracteres");
        }

        String ref = referencia == null ? "" : referencia.trim();
        if (ref.isEmpty()) {
            errores.put("referencia", "La dirección o referencia es obligatoria");
        } else if (ref.length() > REF_MAX) {
            errores.put("referencia", "La referencia admite máximo " + REF_MAX + " caracteres");
        }

        if (latitud == null) {
            errores.put("latitud", "La latitud es obligatoria");
        } else if (latitud < -90 || latitud > 90) {
            errores.put("latitud", "La latitud debe estar entre -90 y 90");
        }
        if (longitud == null) {
            errores.put("longitud", "La longitud es obligatoria");
        } else if (longitud < -180 || longitud > 180) {
            errores.put("longitud", "La longitud debe estar entre -180 y 180");
        }

        String errorFoto = fotos.errorDeFoto(foto);
        if (errorFoto != null) errores.put("foto", errorFoto);

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        Usuario autor = usuarios.findById(usuarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

        Reporte r = new Reporte();
        r.setUsuario(autor);
        r.setTipo(tipo);
        r.setBarrio(barrio);
        r.setDescripcion(desc);
        r.setReferencia(ref);
        r.setLatitud(latitud);
        r.setLongitud(longitud);
        r.setEstado(Estado.RECIBIDO);
        r.setVotos(0);
        r.setFoto(fotos.guardar(foto));
        return aRespuesta(reportes.save(r));
    }

    /**
     * Lista los reportes visibles públicamente (excluyendo los rechazados), filtrados
     * opcionalmente por tipo, barrio y estado, ordenados por fecha descendente.
     *
     * @param tipoId    filtro opcional por identificador de tipo de reporte
     * @param barrioId  filtro opcional por identificador de barrio
     * @param estadoTxt filtro opcional por nombre de estado
     * @return lista de reportes públicos que cumplen los criterios
     */
    @Transactional(readOnly = true)
    public List<ReporteResponse> listarPublicos(Long tipoId, Long barrioId, String estadoTxt) {
        Estado estado = parseEstado(estadoTxt);
        Specification<Reporte> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.notEqual(root.get("estado"), Estado.RECHAZADO));
            if (tipoId != null) p.add(cb.equal(root.get("tipo").get("id"), tipoId));
            if (barrioId != null) p.add(cb.equal(root.get("barrio").get("id"), barrioId));
            if (estado != null) p.add(cb.equal(root.get("estado"), estado));
            return cb.and(p.toArray(new Predicate[0]));
        };
        return reportes.findAll(spec, Sort.by(Sort.Direction.DESC, "fecha")).stream()
                .map(this::aRespuesta).toList();
    }

    /**
     * Obtiene el detalle completo de un reporte público junto con su historial de cambios
     * de estado.
     *
     * @param id identificador del reporte
     * @return detalle del reporte y su historial
     * @throws ResponseStatusException si el reporte no existe o está en estado {@link Estado#RECHAZADO}
     */
    @Transactional(readOnly = true)
    public ReporteDetalleResponse detallePublico(Long id) {
        Reporte r = reportes.findById(id)
                .filter(x -> x.getEstado() != Estado.RECHAZADO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El caso no existe"));
        return aDetalle(r);
    }

    /**
     * Devuelve los reportes creados por un usuario específico, ordenados del más reciente
     * al más antiguo, incluyendo su historial de estados.
     *
     * @param usuarioId identificador del usuario autor
     * @return lista detallada de reportes del usuario
     */
    @Transactional(readOnly = true)
    public List<ReporteDetalleResponse> misCasos(Long usuarioId) {
        return reportes.findByUsuarioIdOrderByFechaDesc(usuarioId).stream().map(this::aDetalle).toList();
    }

    /**
     * Consulta los reportes activos para el panel de gestión (o filtrados por un estado
     * concreto) y los ordena por puntaje de prioridad descendente y fecha más reciente.
     *
     * @param tipoId    filtro opcional por identificador de tipo de reporte
     * @param barrioId  filtro opcional por identificador de barrio
     * @param estadoTxt filtro opcional por nombre de estado
     * @return lista priorizada de reportes para atención
     */
    @Transactional(readOnly = true)
    public List<ReporteResponse> panel(Long tipoId, Long barrioId, String estadoTxt) {
        Estado estado = parseEstado(estadoTxt);
        Specification<Reporte> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (estado != null) {
                p.add(cb.equal(root.get("estado"), estado));
            } else {
                p.add(root.get("estado").in(Estado.RECIBIDO, Estado.EN_REVISION, Estado.EN_PROCESO));
            }
            if (tipoId != null) p.add(cb.equal(root.get("tipo").get("id"), tipoId));
            if (barrioId != null) p.add(cb.equal(root.get("barrio").get("id"), barrioId));
            return cb.and(p.toArray(new Predicate[0]));
        };
        Comparator<Integer> porPuntaje = Comparator.nullsLast(Comparator.<Integer>reverseOrder());
        Comparator<LocalDateTime> masRecientePrimero = Comparator.<LocalDateTime>reverseOrder();
        return reportes.findAll(spec).stream()
                .map(this::aRespuesta)
                .sorted(Comparator.comparing(ReporteResponse::puntaje, porPuntaje)
                        .thenComparing(ReporteResponse::fecha, masRecientePrimero))
                .toList();
    }

    /**
     * Cambia el estado de un reporte validando la transición permitida, el comentario
     * obligatorio según el estado destino y la evidencia fotográfica opcional.
     *
     * @param funcionarioId identificador del funcionario que realiza el cambio
     * @param id            identificador del reporte a actualizar
     * @param estadoTxt     nombre del nuevo estado objetivo
     * @param comentario    observación, motivo de rechazo o comentario de cierre
     * @param foto          imagen opcional de evidencia de solución
     * @return reporte actualizado en formato de respuesta
     * @throws ValidacionException       si faltan datos obligatorios o la imagen no es válida
     * @throws ResponseStatusException si el reporte no existe, la transición no está permitida
     *                                o el funcionario no es válido
     */
    @Transactional
    public ReporteResponse cambiarEstado(Long funcionarioId, Long id, String estadoTxt,
                                         String comentario, MultipartFile foto) {
        Map<String, String> errores = new LinkedHashMap<>();

        Estado nuevo = null;
        if (estadoTxt == null || estadoTxt.isBlank()) {
            errores.put("estado", "El nuevo estado es obligatorio");
        } else {
            try {
                nuevo = Estado.valueOf(estadoTxt.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errores.put("estado", "Estado inválido");
            }
        }

        String texto = comentario == null ? "" : comentario.trim();
        if (texto.length() > COM_MAX) {
            errores.put("comentario", "El comentario admite máximo " + COM_MAX + " caracteres");
        } else if (nuevo == Estado.RESUELTO && texto.isEmpty()) {
            errores.put("comentario", "El comentario de cierre es obligatorio para resolver un caso");
        } else if (nuevo == Estado.RECHAZADO && texto.isEmpty()) {
            errores.put("comentario", "El motivo es obligatorio para rechazar un caso");
        }

        boolean hayFoto = foto != null && !foto.isEmpty();
        if (hayFoto) {
            String errorFoto = fotos.errorDeFoto(foto);
            if (errorFoto != null) errores.put("foto", errorFoto);
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        Reporte r = reportes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "El caso no existe"));
        Estado actual = r.getEstado();
        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pasar de " + actual + " a " + nuevo);
        }
        Usuario funcionario = usuarios.findById(funcionarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

        r.setEstado(nuevo);
        r.setFuncionario(funcionario);
        r.setComentarioEstado(texto.isEmpty() ? null : texto);
        if (nuevo == Estado.RESUELTO && hayFoto) {
            r.setFotoSolucion(fotos.guardar(foto));
        }
        return aRespuesta(reportes.save(r));
    }

    /**
     * Convierte un texto de estado en su valor correspondiente del enum {@link Estado}.
     */
    private Estado parseEstado(String txt) {
        if (txt == null || txt.isBlank()) return null;
        try {
            return Estado.valueOf(txt.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(Map.of("estado", "Estado inválido"));
        }
    }

    /**
     * Construye el objeto de detalle de un reporte incluyendo su historial ordenado cronológicamente.
     */
    private ReporteDetalleResponse aDetalle(Reporte r) {
        List<HistorialResponse> h = historial.findByReporteIdOrderByFechaAsc(r.getId()).stream()
                .map(x -> new HistorialResponse(x.getEstadoAnterior(), x.getEstadoNuevo(), x.getFecha(),
                        x.getFuncionario() == null ? null : x.getFuncionario().getNombre(),
                        x.getComentario(), urlFoto(x.getEvidencia())))
                .toList();
        return new ReporteDetalleResponse(aRespuesta(r), h);
    }

    /**
     * Transforma una entidad {@link Reporte} en su DTO {@link ReporteResponse} calculando el puntaje.
     */
    private ReporteResponse aRespuesta(Reporte r) {
        return new ReporteResponse(r.getId(), r.getDescripcion(), r.getReferencia(), urlFoto(r.getFoto()),
                r.getLatitud(), r.getLongitud(), r.getEstado().name(), r.getFecha(), r.getVotos(),
                puntaje.calcular(r), r.getComentarioEstado(), urlFoto(r.getFotoSolucion()),
                new TipoResponse(r.getTipo().getId(), r.getTipo().getNombre(), r.getTipo().getPeso()),
                new BarrioResponse(r.getBarrio().getId(), r.getBarrio().getNombre()));
    }

    /**
     * Construye la ruta relativa de descarga para una imagen almacenada.
     */
    private String urlFoto(String nombre) {
        return nombre == null ? null : "/api/fotos/" + nombre;
    }
}