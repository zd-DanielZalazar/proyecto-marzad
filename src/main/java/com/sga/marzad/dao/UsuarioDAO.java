package com.sga.marzad.dao;

import com.sga.marzad.model.Usuario;
import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    /**
     * Busca un usuario por nombre de usuario.
     * @return instancia de Usuario o null si no lo encuentra.
     */
    public Usuario findByUsername(String username) {
        String sql = """
            SELECT id, username, hash_password, rol
              FROM usuarios
             WHERE username = ?
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("hash_password"),
                            rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.findByUsername:");
            e.printStackTrace();
        }
        return null;
    }
}
