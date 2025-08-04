package com.sga.marzad.model;

public class Carrera {
    private int id;
    private String nombre;
    private String descripcion;
    private boolean habilitado;

    public Carrera(int id, String nombre, String descripcion, boolean habilitado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.habilitado = habilitado;
    }

    public Carrera() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    @Override
    public String toString() {
        return nombre; // Para mostrar en el ListView
    }
}
