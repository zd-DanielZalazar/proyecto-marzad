package com.sga.marzad.utils;

/**
 * Clase singleton para mantener los datos del usuario logueado durante la sesión.
 * No requiere instanciarse.
 */
public class UsuarioSesion {
    private static String userName;
    private static String rol;
    private static Integer usuarioId;
    private static Integer alumnoId;
    private static Integer docenteId;

    // Métodos set/get para userName
    public static void setUserName(String name) { userName = name; }
    public static String getUserName() { return userName; }

    // Métodos set/get para rol
    public static void setRol(String r) { rol = r; }
    public static String getRol() { return rol; }

    // Métodos set/get para usuarioId
    public static void setUsuarioId(Integer id) { usuarioId = id; }
    public static Integer getUsuarioId() { return usuarioId; }

    // Métodos set/get para alumnoId
    public static void setAlumnoId(Integer id) { alumnoId = id; }
    public static Integer getAlumnoId() { return alumnoId; }

    // Métodos set/get para docenteId
    public static void setDocenteId(Integer id) { docenteId = id; }
    public static Integer getDocenteId() { return docenteId; }

    // Método para limpiar la sesión (ejemplo: al hacer logout)
    public static void limpiarSesion() {
        userName = null;
        rol = null;
        usuarioId = null;
        alumnoId = null;
        docenteId = null;
    }
}
