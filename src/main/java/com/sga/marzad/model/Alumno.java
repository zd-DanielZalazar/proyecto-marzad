package com.sga.marzad.model;

import java.time.LocalDate;

public class Alumno {
    private int id;
    private int usuarioId;
    private String nombre;
    private String apellido;
    private String dni;
    private String correo;
    private LocalDate fechaNac;
    private String genero;
    private boolean habilitado;

    // Constructor completo
    public Alumno(int id, int usuarioId, String nombre, String apellido, String dni,
                  String correo, LocalDate fechaNac, String genero, boolean habilitado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.correo = correo;
        this.fechaNac = fechaNac;
        this.genero = genero;
        this.habilitado = habilitado;
    }

    // Constructor simple (si lo querés)
    public Alumno(int id, int usuarioId, String nombre, String apellido, String dni) {
        this(id, usuarioId, nombre, apellido, dni, null, null, null, true);
    }

    // Getters y setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public LocalDate getFechaNac() { return fechaNac; }
    public void setFechaNac(LocalDate fechaNac) { this.fechaNac = fechaNac; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
