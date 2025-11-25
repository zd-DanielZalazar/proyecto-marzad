package com.sga.marzad.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AsistenciaRegistro {
    private final int id;
    private final int inscripcionId;
    private final LocalDate fecha;
    private final boolean presente;

    public AsistenciaRegistro(int id, int inscripcionId, LocalDate fecha, boolean presente) {
        this.id = id;
        this.inscripcionId = inscripcionId;
        this.fecha = fecha;
        this.presente = presente;
    }

    public int getId() {
        return id;
    }

    public int getInscripcionId() {
        return inscripcionId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getFechaFormateada() {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public boolean isPresente() {
        return presente;
    }

    public String getEstado() {
        return presente ? "Presente" : "Ausente";
    }
}
