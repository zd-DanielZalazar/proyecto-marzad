package com.sga.marzad.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AnaliticoMateria {

    private final String nombreMateria;
    private final int anio;
    private final int cuatrimestre;
    private final Double nota;
    private final String condicion;
    private final LocalDateTime fechaUltimaActualizacion;

    public AnaliticoMateria(String nombreMateria,
                            int anio,
                            int cuatrimestre,
                            Double nota,
                            String condicion,
                            LocalDateTime fechaUltimaActualizacion) {
        this.nombreMateria = nombreMateria;
        this.anio = anio;
        this.cuatrimestre = cuatrimestre;
        this.nota = nota;
        this.condicion = condicion;
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public String getNombreMateria() {
        return nombreMateria;
    }

    public int getAnio() {
        return anio;
    }

    public int getCuatrimestre() {
        return cuatrimestre;
    }

    public Double getNota() {
        return nota;
    }

    public String getNotaTexto() {
        return nota == null ? "-" : String.format("%.1f", nota);
    }

    public String getCondicion() {
        return condicion;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public String getFechaFormateada() {
        if (fechaUltimaActualizacion == null) return "";
        return fechaUltimaActualizacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
