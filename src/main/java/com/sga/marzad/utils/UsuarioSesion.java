package com.sga.marzad.utils;

import com.sga.marzad.model.Usuario;

/**
 * Clase estática para mantener los datos del usuario logueado durante la sesión.
 * No requiere instanciarse.
 */
public class UsuarioSesion {

    private static String userName;
    private static String rol;
    private static Integer usuarioId;

    // Datos específicos de rol
    private static Integer alumnoId;
    private static Integer docenteId;

    // Datos de carrera
    private static Integer carreraId;
    private static Integer inscripcionCarreraId;
    private static String carreraNombre;

    // Objeto completo de usuario (opcional, por conveniencia)
    private static Usuario usuario;

    // ----------------------------------------------------------
    // Métodos set/get para userName
    public static void setUserName(String name) { userName = name; }
    public static String getUserName() { return userName; }

    // ----------------------------------------------------------
    // Métodos set/get para rol
    public static void setRol(String r) { rol = r; }
    public static String getRol() { return rol; }

    // ----------------------------------------------------------
    // Métodos set/get para usuarioId
    public static void setUsuarioId(Integer id) { usuarioId = id; }
    public static Integer getUsuarioId() { return usuarioId; }

    // ----------------------------------------------------------
    // Métodos set/get para alumnoId
    public static void setAlumnoId(Integer id) { alumnoId = id; }
    public static Integer getAlumnoId() { return alumnoId; }

    // ----------------------------------------------------------
    // Métodos set/get para docenteId
    public static void setDocenteId(Integer id) { docenteId = id; }
    public static Integer getDocenteId() { return docenteId; }

    // ----------------------------------------------------------
    // Métodos set/get para carreraId
    public static void setCarreraId(Integer id) { carreraId = id; }
    public static Integer getCarreraId() { return carreraId; }

    // ----------------------------------------------------------
    // Métodos set/get para inscripcionCarreraId
    public static void setInscripcionCarreraId(Integer id) { inscripcionCarreraId = id; }
    public static Integer getInscripcionCarreraId() { return inscripcionCarreraId; }

    // ----------------------------------------------------------
    // Métodos set/get para carreraNombre
    public static void setCarreraNombre(String nombre) { carreraNombre = nombre; }
    public static String getCarreraNombre() { return carreraNombre; }

    // ----------------------------------------------------------
    // Usuario completo
    public static void setUsuario(Usuario u) {
        usuario = u;
        if (u != null) {
            usuarioId = u.getId();
            userName = u.getUsername();
            rol = u.getRol();
        }
    }
    public static Usuario getUsuario() { return usuario; }

    // ----------------------------------------------------------
    // Nuevo: setCarrera todo en un solo paso
    public static void setCarrera(Integer cId, Integer inscId, String nombreCarrera) {
        carreraId = cId;
        inscripcionCarreraId = inscId;
        carreraNombre = nombreCarrera;
    }

    // ----------------------------------------------------------
    // Método para limpiar la sesión (logout)
    public static void limpiarSesion() {
        userName = null;
        rol = null;
        usuarioId = null;
        alumnoId = null;
        docenteId = null;
        carreraId = null;
        inscripcionCarreraId = null;
        carreraNombre = null;
        usuario = null;
    }
}
