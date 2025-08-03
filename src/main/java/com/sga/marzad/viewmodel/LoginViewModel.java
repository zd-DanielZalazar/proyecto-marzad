package com.sga.marzad.viewmodel;

import com.sga.marzad.dao.AlumnoDAO;
import com.sga.marzad.dao.DocenteDAO;
import com.sga.marzad.dao.UsuarioDAO;
import com.sga.marzad.model.Usuario;

public class LoginViewModel {
    private final UsuarioDAO usuarioDao = new UsuarioDAO();
    private final AlumnoDAO alumnoDao = new AlumnoDAO();
    private final DocenteDAO docenteDao = new DocenteDAO();

    /** Autentica comparando texto plano */
    public Usuario autenticar(String user, String pass) {
        Usuario u = usuarioDao.findByUsername(user);
        if (u != null && pass.equals(u.getHashPassword())) {
            return u;
        }
        return null;
    }

    /** Devuelve el id de alumno asociado a un usuario, o -1 si no existe */
    public int obtenerAlumnoIdPorUsuarioId(int usuarioId) {
        return alumnoDao.obtenerIdPorUsuarioId(usuarioId);
    }

    /** Devuelve el id de docente asociado a un usuario, o -1 si no existe */
    public int obtenerDocenteIdPorUsuarioId(int usuarioId) {
        return docenteDao.obtenerIdPorUsuarioId(usuarioId);
    }
}
