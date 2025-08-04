package com.sga.marzad.model;

import javafx.beans.property.*;
import java.util.List;

public class Materia {
    // Campos...
    private int id;
    private int planId;
    private String nombre;
    private int anio;
    private int cuatrimestre;
    private int creditos;
    private boolean habilitado;
    private List<Materia> correlativas;
    private String docente;
    private String horario;

    private final IntegerProperty idProperty = new SimpleIntegerProperty();
    private final StringProperty nombreProperty = new SimpleStringProperty();
    private final IntegerProperty anioProperty = new SimpleIntegerProperty();

    public Materia() {}

    // CONSTRUCTOR NECESARIO PARA DAO:
    public Materia(int id, int planId, String nombre, int anio, int cuatrimestre, int creditos, boolean habilitado) {
        this.id = id;
        this.planId = planId;
        this.nombre = nombre;
        this.anio = anio;
        this.cuatrimestre = cuatrimestre;
        this.creditos = creditos;
        this.habilitado = habilitado;

        this.idProperty.set(id);
        this.nombreProperty.set(nombre);
        this.anioProperty.set(anio);
    }

    public Materia(int id, String nombre, int anio) {
        this.id = id;
        this.nombre = nombre;
        this.anio = anio;

        this.idProperty.set(id);
        this.nombreProperty.set(nombre);
        this.anioProperty.set(anio);
    }

    // --- Getters y Setters tradicionales ---
    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
        this.idProperty.set(id);
    }

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.nombreProperty.set(nombre);
    }

    public int getAnio() { return anio; }
    public void setAnio(int anio) {
        this.anio = anio;
        this.anioProperty.set(anio);
    }

    public int getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(int cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    public List<Materia> getCorrelativas() { return correlativas; }
    public void setCorrelativas(List<Materia> correlativas) { this.correlativas = correlativas; }

    public String getDocente() { return docente; }
    public void setDocente(String docente) { this.docente = docente; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    // --- Properties para JavaFX ---
    public IntegerProperty idProperty() { return idProperty; }
    public StringProperty nombreProperty() { return nombreProperty; }
    public IntegerProperty anioProperty() { return anioProperty; }

    // --- toString para listas/combos ---
    @Override
    public String toString() {
        return nombre + " (" + anio + "°Año)";
    }
}
