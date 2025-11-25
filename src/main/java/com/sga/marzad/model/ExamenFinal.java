package com.sga.marzad.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExamenFinal {

    private final int id;
    private final int materiaId;
    private final String materiaNombre;
    private final LocalDateTime fecha;
    private final String aula;
    private final int cupo;
    private final int inscriptos;
    private final String estado;

    public ExamenFinal(int id,
                       int materiaId,
                       String materiaNombre,
                       LocalDateTime fecha,
                       String aula,
                       int cupo,
                       int inscriptos,
                       String estado) {
        this.id = id;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
        this.fecha = fecha;
        this.aula = aula;
        this.cupo = cupo;
        this.inscriptos = inscriptos;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public int getMateriaId() {
        return materiaId;
    }

    public String getMateriaNombre() {
        return materiaNombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getFechaFormateada() {
        if (fecha == null) return "";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getAula() {
        return aula;
    }

    public int getCupo() {
        return cupo;
    }

    public int getInscriptos() {
        return inscriptos;
    }

    public int getCupoDisponible() {
        return Math.max(0, cupo - inscriptos);
    }

    public String getEstado() {
        return estado;
    }

    public boolean tieneCupo() {
        return "PUBLICADO".equalsIgnoreCase(estado) && getCupoDisponible() > 0;
    }
}
