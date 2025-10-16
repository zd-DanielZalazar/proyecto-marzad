package com.sga.marzad.model;

public class Usuario {
    private int id;
    private String username;
    private String password;
    private String rol; // ADMIN, DOCENTE, ALUMNO

    public Usuario(int id, String username, String password, String rol) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
}
