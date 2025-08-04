package com.sga.marzad.model;

import javafx.beans.property.*;

public class AlumnoNotasDocente {
    private final IntegerProperty alumnoId;
    private final StringProperty nombreCompleto;
    private final StringProperty dni;
    private final StringProperty correo;
    private final StringProperty estadoInscripcion;

    // 5 notas
    private final DoubleProperty parcial1;
    private final DoubleProperty recup1;
    private final DoubleProperty parcial2;
    private final DoubleProperty recup2;
    private final DoubleProperty finalPromo;

    public AlumnoNotasDocente(int alumnoId, String nombreCompleto, String dni, String correo, String estadoInscripcion,
                              Double parcial1, Double recup1, Double parcial2, Double recup2, Double finalPromo) {
        this.alumnoId = new SimpleIntegerProperty(alumnoId);
        this.nombreCompleto = new SimpleStringProperty(nombreCompleto);
        this.dni = new SimpleStringProperty(dni);
        this.correo = new SimpleStringProperty(correo);
        this.estadoInscripcion = new SimpleStringProperty(estadoInscripcion);
        this.parcial1 = new SimpleDoubleProperty(parcial1 != null ? parcial1 : 0);
        this.recup1 = new SimpleDoubleProperty(recup1 != null ? recup1 : 0);
        this.parcial2 = new SimpleDoubleProperty(parcial2 != null ? parcial2 : 0);
        this.recup2 = new SimpleDoubleProperty(recup2 != null ? recup2 : 0);
        this.finalPromo = new SimpleDoubleProperty(finalPromo != null ? finalPromo : 0);
    }

    // Getters y Propertys
    public int getAlumnoId() { return alumnoId.get(); }
    public IntegerProperty alumnoIdProperty() { return alumnoId; }

    public String getNombreCompleto() { return nombreCompleto.get(); }
    public StringProperty nombreCompletoProperty() { return nombreCompleto; }

    public String getDni() { return dni.get(); }
    public StringProperty dniProperty() { return dni; }

    public String getCorreo() { return correo.get(); }
    public StringProperty correoProperty() { return correo; }

    public String getEstadoInscripcion() { return estadoInscripcion.get(); }
    public StringProperty estadoInscripcionProperty() { return estadoInscripcion; }

    public Double getParcial1() { return parcial1.get(); }
    public DoubleProperty parcial1Property() { return parcial1; }

    public Double getRecup1() { return recup1.get(); }
    public DoubleProperty recup1Property() { return recup1; }

    public Double getParcial2() { return parcial2.get(); }
    public DoubleProperty parcial2Property() { return parcial2; }

    public Double getRecup2() { return recup2.get(); }
    public DoubleProperty recup2Property() { return recup2; }

    public Double getFinalPromo() { return finalPromo.get(); }
    public DoubleProperty finalProperty() { return finalPromo; }
}
