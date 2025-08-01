package com.sga.marzad.model;

public class Usuario {
    private int id;
    private String username;
    private String hashPassword;
    private String rol; // ADMIN, DOCENTE, ALUMNO

    public Usuario(int id, String username, String hashPassword, String rol) {
        this.id = id;
        this.username = username;
        this.hashPassword = hashPassword;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getHashPassword() { return hashPassword; }
    public String getRol() { return rol; }
}
