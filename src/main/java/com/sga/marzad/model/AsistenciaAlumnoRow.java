package com.sga.marzad.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
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
    private final BooleanProperty ausente = new SimpleBooleanProperty(false);
    private final IntegerProperty totalPresentes = new SimpleIntegerProperty(0);
    private final IntegerProperty totalClases = new SimpleIntegerProperty(0);

    public AsistenciaAlumnoRow(int inscripcionId, int alumnoId, String nombreCompleto, String dni,
                               boolean presente, boolean marcadoHoy, int totalPresentes, int totalClases) {
        this.inscripcionId = inscripcionId;
        this.alumnoId = alumnoId;
        this.nombreCompleto.set(nombreCompleto);
        this.dni.set(dni);
        if (marcadoHoy) {
            this.presente.set(presente);
            this.ausente.set(!presente);
        } else {
            this.presente.set(false);
            this.ausente.set(false);
        }
        this.totalPresentes.set(totalPresentes);
        this.totalClases.set(totalClases);

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
        if (value) {
            ausente.set(false);
        }
    }

    public BooleanProperty presenteProperty() {
        return presente;
    }

    public boolean isAusente() {
        return ausente.get();
    }

    public void setAusente(boolean value) {
        ausente.set(value);
        if (value) {
            presente.set(false);
        }
    }

    public BooleanProperty ausenteProperty() {
        return ausente;
    }

    public IntegerProperty totalPresentesProperty() {
        return totalPresentes;
    }

    public IntegerProperty totalClasesProperty() {
        return totalClases;
    }

    public int getTotalPresentes() {
        return totalPresentes.get();
    }

    public int getTotalClases() {
        return totalClases.get();
    }

    public String getResumenAsistencias() {
        return getTotalPresentes() + " / " + getTotalClases();
    }
}
