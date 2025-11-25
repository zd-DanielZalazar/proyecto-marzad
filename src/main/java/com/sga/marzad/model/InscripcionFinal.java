package com.sga.marzad.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InscripcionFinal {

    private final int id;
    private final int alumnoId;
    private final int examenFinalId;
    private final LocalDateTime fechaInscripcion;
    private final String estado;
    private final String materiaNombre;
    private final LocalDateTime fechaExamen;
    private final String aula;

    public InscripcionFinal(int id,
                            int alumnoId,
                            int examenFinalId,
                            LocalDateTime fechaInscripcion,
                            String estado,
                            String materiaNombre,
                            LocalDateTime fechaExamen,
                            String aula) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.examenFinalId = examenFinalId;
        this.fechaInscripcion = fechaInscripcion;
        this.estado = estado;
        this.materiaNombre = materiaNombre;
        this.fechaExamen = fechaExamen;
        this.aula = aula;
    }

    public int getId() {
        return id;
    }

    public int getAlumnoId() {
        return alumnoId;
    }

    public int getExamenFinalId() {
        return examenFinalId;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public String getFechaInscripcionFormateada() {
        if (fechaInscripcion == null) return "";
        return fechaInscripcion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getEstado() {
        return estado;
    }

    public String getMateriaNombre() {
        return materiaNombre;
    }

    public LocalDateTime getFechaExamen() {
        return fechaExamen;
    }

    public String getFechaExamenFormateada() {
        if (fechaExamen == null) return "";
        return fechaExamen.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getAula() {
        return aula;
    }
}
