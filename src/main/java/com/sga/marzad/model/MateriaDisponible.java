package com.sga.marzad.model;

public class MateriaDisponible {
    private int id;
    private String nombre;
    private int anio;
    private int cuatrimestre;
    private int planId;
    private String carreraNombre;

    public MateriaDisponible(int id, String nombre, int anio, int cuatrimestre, int planId, String carreraNombre) {
        this.id = id;
        this.nombre = nombre;
        this.anio = anio;
        this.cuatrimestre = cuatrimestre;
        this.planId = planId;
        this.carreraNombre = carreraNombre;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getAnio() { return anio; }
    public int getCuatrimestre() { return cuatrimestre; }
    public int getPlanId() { return planId; }
    public String getCarreraNombre() { return carreraNombre; }

    @Override
    public String toString() {
        return nombre + " (" + anio + "° año - " + cuatrimestre + "° cuatri)";
    }
}
