package com.sga.marzad.viewmodel;

import com.sga.marzad.model.Usuario;
import com.sga.marzad.model.Alumno;
import com.sga.marzad.model.Docente;
import com.sga.marzad.dao.AlumnoDAO;
import com.sga.marzad.dao.DocenteDAO;
import com.sga.marzad.service.PerfilService;

import java.time.LocalDate;

public class PerfilViewModel {

    private Usuario usuarioActual;
    private Alumno alumnoActual;
    private Docente docenteActual;
    private boolean esAlumno = false;

    public PerfilViewModel(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }


    public void cargarPerfilActual() {
        if (usuarioActual.getRol().equalsIgnoreCase("ALUMNO")) {
            esAlumno = true;
            alumnoActual = PerfilService.buscarAlumnoPorUsuarioId(usuarioActual.getId());
        } else if (usuarioActual.getRol().equalsIgnoreCase("DOCENTE")) {
            esAlumno = false;
            docenteActual = PerfilService.buscarDocentePorUsuarioId(usuarioActual.getId());
        }
    }

    // Métodos getters para el controller

    public String getNombre() { return esAlumno ? alumnoActual.getNombre() : docenteActual.getNombre(); }
    public String getApellido() { return esAlumno ? alumnoActual.getApellido() : docenteActual.getApellido(); }
    public String getCorreo() { return esAlumno ? alumnoActual.getCorreo() : docenteActual.getCorreo(); }
    public String getGenero() { return esAlumno ? alumnoActual.getGenero() : docenteActual.getGenero(); }
    public String getDni() { return esAlumno ? alumnoActual.getDni() : ""; }
    public String getLegajo() { return esAlumno ? "" : docenteActual.getLegajo(); }
    public LocalDate getFechaNac() { return esAlumno ? alumnoActual.getFechaNac() : null; }
    public boolean isAlumno() { return esAlumno; }
    public boolean isDocente() { return !esAlumno; }

    // Guardar cambios (valida duplicados, etc.)
    public boolean guardarPerfil(String nombre, String apellido, String correo, String genero, LocalDate fechaNac) {
        if (esAlumno) {
            return PerfilService.actualizarAlumno(alumnoActual.getId(), nombre, apellido, correo, genero, fechaNac);
        } else {
            return PerfilService.actualizarDocente(docenteActual.getId(), nombre, apellido, correo, genero);
        }
    }

    public boolean cambiarPassword(String actual, String nueva) {
        // Trae usuario actual desde sesión
        int usuarioId = usuarioActual.getId();

        // 1. Obtener el hash de la contraseña actual de la tabla usuarios
        String passwordActualHash = null;
        if (isAlumno()) {
            passwordActualHash = AlumnoDAO.obtenerPasswordActual(usuarioId);
        } else if (isDocente()) {
            passwordActualHash = DocenteDAO.obtenerPasswordActual(usuarioId);
        }

        if (passwordActualHash == null) return false;

        // 2. Comparar el hash del password ingresado por el usuario con el de la base
        // Si no usás hash (solo texto plano, no recomendado), compara directo:
        // if (!actual.equals(passwordActualHash)) return false;

        // Si usás hash, reemplazá esto por el método correcto, por ejemplo:
        if (!actual.equals(passwordActualHash)) return false;

        // 3. Guardar la nueva contraseña (en la tabla usuarios)
        // (Recomendado: aplicar hash antes de guardar si querés mejorar seguridad)
        // Si guardás en texto plano, simplemente:
        String nuevaPassword = nueva; // O el hash si lo usás

        boolean exito = false;
        if (isAlumno()) {
            exito = AlumnoDAO.actualizarPassword(usuarioId, nuevaPassword);
        } else if (isDocente()) {
            exito = DocenteDAO.actualizarPassword(usuarioId, nuevaPassword);
        }

        return exito;
    }
}
