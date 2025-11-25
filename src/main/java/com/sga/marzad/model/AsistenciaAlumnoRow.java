package com.sga.marzad.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AsistenciaAlumnoRow {

    private final int inscripcionId;
    private final int alumnoId;
    private final StringProperty nombreCompleto = new SimpleStringProperty();
    private final StringProperty dni = new SimpleStringProperty();
    private final BooleanProperty presente = new SimpleBooleanProperty(false);

    public AsistenciaAlumnoRow(int inscripcionId, int alumnoId, String nombreCompleto, String dni, boolean presente) {
        this.inscripcionId = inscripcionId;
        this.alumnoId = alumnoId;
        this.nombreCompleto.set(nombreCompleto);
        this.dni.set(dni);
        this.presente.set(presente);
    }

    public int getInscripcionId() {
        return inscripcionId;
    }

    public int getAlumnoId() {
        return alumnoId;
    }

    public String getNombreCompleto() {
        return nombreCompleto.get();
    }

    public StringProperty nombreCompletoProperty() {
        return nombreCompleto;
    }

    public String getDni() {
        return dni.get();
    }

    public StringProperty dniProperty() {
        return dni;
    }

    public boolean isPresente() {
        return presente.get();
    }

    public void setPresente(boolean value) {
        presente.set(value);
    }

    public BooleanProperty presenteProperty() {
        return presente;
    }
}
