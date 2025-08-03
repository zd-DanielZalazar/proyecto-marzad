package com.sga.marzad.service;

import com.sga.marzad.dao.AlumnoDAO;
import com.sga.marzad.dao.DocenteDAO;
import com.sga.marzad.model.Alumno;
import com.sga.marzad.model.Docente;

import java.time.LocalDate;

public class PerfilService {

    public static Alumno buscarAlumnoPorUsuarioId(int usuarioId) {
        return AlumnoDAO.buscarPorUsuarioId(usuarioId);
    }
    public static Docente buscarDocentePorUsuarioId(int usuarioId) {
        return DocenteDAO.buscarPorUsuarioId(usuarioId);
    }

    public static boolean actualizarAlumno(int id, String nombre, String apellido, String correo, String genero, LocalDate fechaNac) {
        return AlumnoDAO.actualizar(id, nombre, apellido, correo, genero, fechaNac);
    }
    public static boolean actualizarDocente(int id, String nombre, String apellido, String correo, String genero) {
        return DocenteDAO.actualizar(id, nombre, apellido, correo, genero);
    }
}
