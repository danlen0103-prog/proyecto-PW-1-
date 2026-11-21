package com.reporte.reporteciudadano.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.reporte.reporteciudadano.dto.*;
import com.reporte.reporteciudadano.model.Barrio;
import com.reporte.reporteciudadano.model.TipoReporte;
import com.reporte.reporteciudadano.repository.BarrioRepository;
import com.reporte.reporteciudadano.repository.TipoReporteRepository;


@Service
public class CatalogoService {

    private final TipoReporteRepository tipos;
    private final BarrioRepository barrios;

    
    public CatalogoService(TipoReporteRepository tipos, BarrioRepository barrios) {
        this.tipos = tipos;
        this.barrios = barrios;
    }

  

    
    public List<TipoResponse> listarTipos() {
        return tipos.findAll(Sort.by("nombre")).stream().map(this::aTipo).toList();
    }

   
    public TipoResponse crearTipo(TipoRequest d) {
        String nombre = d.nombre().trim();
        if (tipos.existsByNombre(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un tipo con ese nombre");
        }
        TipoReporte t = new TipoReporte();
        t.setNombre(nombre);
        t.setPeso(d.peso());
        return aTipo(tipos.save(t));
    }

   
    public TipoResponse actualizarTipo(Long id, TipoRequest d) {
        TipoReporte t = tipos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "El tipo no existe"));
        t.setNombre(d.nombre().trim());
        t.setPeso(d.peso());
        return aTipo(tipos.save(t));
    }

    
    public void eliminarTipo(Long id) {
        if (!tipos.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El tipo no existe");
        }
        tipos.deleteById(id);
    }

   
    public List<BarrioResponse> listarBarrios() {
        return barrios.findAll(Sort.by("nombre")).stream().map(this::aBarrio).toList();
    }

   
    public BarrioResponse crearBarrio(BarrioRequest d) {
        String nombre = d.nombre().trim();
        if (barrios.existsByNombre(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un barrio con ese nombre");
        }
        Barrio b = new Barrio();
        b.setNombre(nombre);
        return aBarrio(barrios.save(b));
    }

    
    public BarrioResponse actualizarBarrio(Long id, BarrioRequest d) {
        Barrio b = barrios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "El barrio no existe"));
        b.setNombre(d.nombre().trim());
        return aBarrio(barrios.save(b));
    }

   
    public void eliminarBarrio(Long id) {
        if (!barrios.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El barrio no existe");
        }
        barrios.deleteById(id);
    }

    private TipoResponse aTipo(TipoReporte t) {
        return new TipoResponse(t.getId(), t.getNombre(), t.getPeso());
    }

    private BarrioResponse aBarrio(Barrio b) {
        return new BarrioResponse(b.getId(), b.getNombre());
    }
}