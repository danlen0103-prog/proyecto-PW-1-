package com.reporte.reporteciudadano.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.reporte.reporteciudadano.dto.VotoResponse;
import com.reporte.reporteciudadano.model.Estado;
import com.reporte.reporteciudadano.model.Reporte;
import com.reporte.reporteciudadano.model.Voto;
import com.reporte.reporteciudadano.repository.ReporteRepository;
import com.reporte.reporteciudadano.repository.UsuarioRepository;
import com.reporte.reporteciudadano.repository.VotoRepository;


@Service
public class VotoService {

    private final VotoRepository votos;
    private final ReporteRepository reportes;
    private final UsuarioRepository usuarios;
    private final PuntajeService puntaje;

    
    public VotoService(VotoRepository votos, ReporteRepository reportes,
                       UsuarioRepository usuarios, PuntajeService puntaje) {
        this.votos = votos;
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.puntaje = puntaje;
    }

    
    @Transactional
    public VotoResponse apoyar(Long usuarioId, Long reporteId) {
        Reporte r = reportes.findById(reporteId)
                .filter(x -> x.getEstado() != Estado.RECHAZADO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El caso no existe"));

        if (r.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes apoyar tus propios casos");
        }
        if (r.getEstado() == Estado.RESUELTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El caso ya está resuelto");
        }
        if (votos.existsByUsuarioIdAndReporteId(usuarioId, reporteId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya apoyaste este caso");
        }

        Voto v = new Voto();
        v.setUsuario(usuarios.getReferenceById(usuarioId));
        v.setReporte(r);
        try {
            votos.saveAndFlush(v);
        } catch (DataIntegrityViolationException e) {
            
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya apoyaste este caso");
        }

        reportes.sumarVoto(reporteId);
        Reporte actualizado = reportes.findById(reporteId).orElseThrow();
        return new VotoResponse(reporteId, actualizado.getVotos(), puntaje.calcular(actualizado));
    }
}