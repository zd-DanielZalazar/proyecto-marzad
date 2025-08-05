package com.sga.marzad.model;

public class InscripcionCarrera {
    private int id;
    private int alumnoId;
    private int carreraId;
    private String nombreCarrera;
    private String estado;

    public InscripcionCarrera(int id, int alumnoId, int carreraId, String nombreCarrera, String estado) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.carreraId = carreraId;
        this.nombreCarrera = nombreCarrera;
        this.estado = estado;
    }

    public int getId() { return id; }
    public int getAlumnoId() { return alumnoId; }
    public int getCarreraId() { return carreraId; }
    public String getNombreCarrera() { return nombreCarrera; }
    public String getEstado() { return estado; }
}
