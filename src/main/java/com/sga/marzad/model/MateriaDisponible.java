package com.sga.marzad.model;

import java.util.List;

public class MateriaDisponible {
    private int id;
    private String nombre;
    private int anio;
    private int cuatrimestre;
    private List<String> correlativas;
    private String estado; // "INSCRIPTO", "APROBADA", "DISPONIBLE", "NO_CORRELATIVAS"

    public MateriaDisponible(int id, String nombre, int anio, int cuatrimestre, List<String> correlativas, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.anio = anio;
        this.cuatrimestre = cuatrimestre;
        this.correlativas = correlativas;
        this.estado = estado;
    }

    // Getters y setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getAnio() { return anio; }
    public int getCuatrimestre() { return cuatrimestre; }
    public List<String> getCorrelativas() { return correlativas; }
    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }
}
