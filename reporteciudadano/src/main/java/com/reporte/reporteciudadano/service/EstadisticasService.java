package com.reporte.reporteciudadano.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reporte.reporteciudadano.dto.EstadisticaBarrioResponse;
import com.reporte.reporteciudadano.dto.EstadisticasResponse;
import com.reporte.reporteciudadano.model.Barrio;
import com.reporte.reporteciudadano.model.Estado;
import com.reporte.reporteciudadano.repository.BarrioRepository;
import com.reporte.reporteciudadano.repository.ReporteRepository;


@Service
public class EstadisticasService {

    private final ReporteRepository reportes;
    private final BarrioRepository barrios;

    
    public EstadisticasService(ReporteRepository reportes, BarrioRepository barrios) {
        this.reportes = reportes;
        this.barrios = barrios;
    }

    
    @Transactional(readOnly = true)
    public EstadisticasResponse resumen() {
       
        Map<Long, long[]> acumulado = new HashMap<>();
        for (Object[] fila : reportes.contarPorBarrioYEstado(Estado.RECHAZADO)) {
            Long barrioId = (Long) fila[0];
            Estado estado = (Estado) fila[1];
            long n = ((Number) fila[2]).longValue();
            long[] a = acumulado.computeIfAbsent(barrioId, k -> new long[3]);
            switch (estado) {
                case RECIBIDO, EN_REVISION -> a[0] += n;
                case EN_PROCESO -> a[1] += n;
                case RESUELTO -> a[2] += n;
                default -> { }
            }
        }

        List<EstadisticaBarrioResponse> filas = new ArrayList<>();
        long pendientes = 0, enProceso = 0, resueltos = 0;
        for (Barrio b : barrios.findAll(Sort.by("nombre"))) {
            long[] a = acumulado.getOrDefault(b.getId(), new long[3]);
            filas.add(new EstadisticaBarrioResponse(b.getId(), b.getNombre(),
                    a[0], a[1], a[2], a[0] + a[1] + a[2]));
            pendientes += a[0];
            enProceso += a[1];
            resueltos += a[2];
        }
        
        filas.sort(Comparator.comparingLong(EstadisticaBarrioResponse::total).reversed());

        return new EstadisticasResponse(pendientes, enProceso, resueltos,
                pendientes + enProceso + resueltos, filas);
    }
}