package com.sga.marzad.model;

public class AlumnoFinalInscripto {

    private final int alumnoId;
    private final String nombreCompleto;
    private final String dni;
    private final String estado;

    public AlumnoFinalInscripto(int alumnoId, String nombreCompleto, String dni, String estado) {
        this.alumnoId = alumnoId;
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.estado = estado;
    }

    public int getAlumnoId() {
        return alumnoId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getDni() {
        return dni;
    }

    public String getEstado() {
        return estado;
    }
}
