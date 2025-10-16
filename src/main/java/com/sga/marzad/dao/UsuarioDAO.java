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
            SELECT u.id, u.username, u.password, r.nombre AS rol
              FROM usuarios u
              JOIN roles r ON u.rol_id = r.id
             WHERE u.username = ?
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
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

    /**
     * Valida si la password ingresada coincide con la guardada.
     */
    public boolean validarPassword(int usuarioId, String plainPassword) {
        String sql = "SELECT 1 FROM usuarios WHERE id = ? AND password = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, plainPassword);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.validarPassword:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza la password de un usuario.
     */
    public boolean actualizarPassword(int usuarioId, String nuevaPlain) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nuevaPlain);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.actualizarPassword:");
            e.printStackTrace();
            return false;
        }
    }
}
