package com.sga.marzad.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AsistenciaMatrizRow {
    private final int inscripcionId;
    private final String nombreCompleto;
    private final String dni;
    private final Map<LocalDate, Boolean> asistencias = new HashMap<>();

    public AsistenciaMatrizRow(int inscripcionId, String nombreCompleto, String dni) {
        this.inscripcionId = inscripcionId;
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
    }

    public int getInscripcionId() {
        return inscripcionId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getDni() {
        return dni;
    }

    public void marcar(LocalDate fecha, boolean presente) {
        asistencias.put(fecha, presente);
    }

    public String getValor(LocalDate fecha) {
        if (!asistencias.containsKey(fecha)) return "";
        return Boolean.TRUE.equals(asistencias.get(fecha)) ? "P" : "A";
    }

    public long getPresentes() {
        return asistencias.values().stream().filter(Boolean::booleanValue).count();
    }

    public int getTotalClases() {
        return asistencias.size();
    }

    public Map<LocalDate, Boolean> getAsistencias() {
        return asistencias;
    }
}
