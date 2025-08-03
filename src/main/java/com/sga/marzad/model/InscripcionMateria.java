package com.sga.marzad.model;

import java.time.LocalDateTime;

public class InscripcionMateria {
    private int id;
    private int alumnoId;
    private int materiaId;
    private int inscripcionCarreraId;
    private LocalDateTime fechaInsc;
    private String estado;

    public InscripcionMateria(int id, int alumnoId, int materiaId, int inscripcionCarreraId,
                              LocalDateTime fechaInsc, String estado) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.materiaId = materiaId;
        this.inscripcionCarreraId = inscripcionCarreraId;
        this.fechaInsc = fechaInsc;
        this.estado = estado;
    }

    // Constructor para inserciones (sin id ni fecha ni estado)
    public InscripcionMateria(int alumnoId, int materiaId, int inscripcionCarreraId) {
        this.alumnoId = alumnoId;
        this.materiaId = materiaId;
        this.inscripcionCarreraId = inscripcionCarreraId;
    }

    // Getters y Setters
    public int getId() { return id; }
    public int getAlumnoId() { return alumnoId; }
    public int getMateriaId() { return materiaId; }
    public int getInscripcionCarreraId() { return inscripcionCarreraId; }
    public LocalDateTime getFechaInsc() { return fechaInsc; }
    public String getEstado() { return estado; }

    public void setId(int id) { this.id = id; }
    public void setAlumnoId(int alumnoId) { this.alumnoId = alumnoId; }
    public void setMateriaId(int materiaId) { this.materiaId = materiaId; }
    public void setInscripcionCarreraId(int inscripcionCarreraId) { this.inscripcionCarreraId = inscripcionCarreraId; }
    public void setFechaInsc(LocalDateTime fechaInsc) { this.fechaInsc = fechaInsc; }
    public void setEstado(String estado) { this.estado = estado; }
}
