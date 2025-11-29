package com.sga.marzad.model;

import java.time.LocalDate;
import java.util.List;

public class AsistenciaMatrizResult {
    private final List<LocalDate> fechas;
    private final List<AsistenciaMatrizRow> filas;

    public AsistenciaMatrizResult(List<LocalDate> fechas, List<AsistenciaMatrizRow> filas) {
        this.fechas = fechas;
        this.filas = filas;
    }

    public List<LocalDate> getFechas() {
        return fechas;
    }

    public List<AsistenciaMatrizRow> getFilas() {
        return filas;
    }
}
