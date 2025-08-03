package com.sga.marzad.model;

public class Docente {
    private int id;
    private int usuarioId;
    private String nombre;
    private String apellido;
    private String legajo;
    private String correo;
    private String genero;
    private boolean habilitado;

    public Docente(int id, int usuarioId, String nombre, String apellido, String legajo, String correo, String genero, boolean habilitado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.legajo = legajo;
        this.correo = correo;
        this.genero = genero;
        this.habilitado = habilitado;
    }

    // Getters y setters...
    // (Agregá los métodos que uses en el ViewModel/Controller)
    public int getId() { return id; }
    public int getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getLegajo() { return legajo; }
    public String getCorreo() { return correo; }
    public String getGenero() { return genero; }
    public boolean isHabilitado() { return habilitado; }
}
