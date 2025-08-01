package com.sga.marzad.viewmodel;

import com.sga.marzad.dao.UsuarioDAO;
import com.sga.marzad.model.Usuario;

public class LoginViewModel {
    private final UsuarioDAO dao = new UsuarioDAO();

    /** Autentica comparando texto plano */
    public Usuario autenticar(String user, String pass) {
        Usuario u = dao.findByUsername(user);
        if (u != null && pass.equals(u.getHashPassword())) {
            return u;
        }
        return null;
    }
}
