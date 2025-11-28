package com.sga.marzad.model;

import java.time.LocalDateTime;

public class Usuario {
    private int id;
    private String username;
    private String password;
    private String rol; // ADMIN, DOCENTE, ALUMNO
    private boolean habilitado;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public Usuario(int id, String username, String password, String rol) {
        this(id, username, password, rol, true, null, null);
    }

    public Usuario(int id,
                   String username,
                   String password,
                   String rol,
                   boolean habilitado,
                   LocalDateTime creadoEn,
                   LocalDateTime actualizadoEn) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.habilitado = habilitado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
