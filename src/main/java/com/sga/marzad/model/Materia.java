package com.sga.marzad.model;

import javafx.beans.property.*;

public class Materia {
    private final IntegerProperty id;
    private final StringProperty nombre;
    private final IntegerProperty anio;

    public Materia(int id, String nombre, int anio) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.anio = new SimpleIntegerProperty(anio);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }

    public int getAnio() { return anio.get(); }
    public IntegerProperty anioProperty() { return anio; }

    @Override
    public String toString() {
        return nombre.get() + " (" + anio.get() + "° Año)";
    }
}
