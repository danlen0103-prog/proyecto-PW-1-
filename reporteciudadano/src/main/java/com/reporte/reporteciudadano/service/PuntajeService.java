package com.reporte.reporteciudadano.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import com.reporte.reporteciudadano.model.Estado;
import com.reporte.reporteciudadano.model.Reporte;


@Service
public class PuntajeService {

   
    public int calcular(int peso, int votos, long dias) {
        return peso * 3 + votos * 2 + (int) dias;
    }

    
    public Integer calcular(Reporte r) {
        if (r.getEstado() == Estado.RECHAZADO || r.getEstado() == Estado.RESUELTO) {
            return null;
        }
        long dias = ChronoUnit.DAYS.between(r.getFecha().toLocalDate(), LocalDate.now());
        return calcular(r.getTipo().getPeso(), r.getVotos(), Math.max(dias, 0));
    }
}