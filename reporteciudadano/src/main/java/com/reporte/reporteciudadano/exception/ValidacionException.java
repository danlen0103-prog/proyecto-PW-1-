package com.reporte.reporteciudadano.exception;

import java.util.Map;

public class ValidacionException extends RuntimeException {
    private final Map<String, String> campos;

    public ValidacionException(Map<String, String> campos) {
        super("Datos inválidos");
        this.campos = campos;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}