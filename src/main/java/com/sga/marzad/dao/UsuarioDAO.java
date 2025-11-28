package com.sga.marzad.dao;

import com.sga.marzad.model.Rol;
import com.sga.marzad.model.Usuario;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /** Devuelve todos los usuarios con su rol */
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username, u.password, r.nombre AS rol,
                   u.habilitado, u.creado_en, u.actualizado_en
              FROM usuarios u
              JOIN roles r ON u.rol_id = r.id
             ORDER BY u.id
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("rol"),
                        rs.getBoolean("habilitado"),
                        toLocalDateTime(rs.getTimestamp("creado_en")),
                        toLocalDateTime(rs.getTimestamp("actualizado_en"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.findAll:");
            e.printStackTrace();
        }
        return usuarios;
    }

    /** Lista de roles disponibles */
    public List<Rol> findRoles() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id, nombre FROM roles ORDER BY nombre";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(new Rol(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.findRoles:");
            e.printStackTrace();
        }
        return roles;
    }

    /** Verifica si existe username (opcionalmente excluyendo un ID) */
    public boolean usernameExists(String username, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE LOWER(username)=LOWER(?)";
        boolean hasExclude = excludeId != null;
        if (hasExclude) sql += " AND id <> ?";

        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            if (hasExclude) ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.usernameExists:");
            e.printStackTrace();
        }
        return false;
    }

    /** Inserta un usuario y devuelve la instancia con ID cargado */
    public Usuario insert(Usuario usuario) {
        String sql = """
            INSERT INTO usuarios(username, password, rol_id, habilitado)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int rolId = obtenerRolId(c, usuario.getRol());
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPassword());
            ps.setInt(3, rolId);
            ps.setBoolean(4, usuario.isHabilitado());

            int rows = ps.executeUpdate();
            if (rows == 1) {
                try (ResultSet key = ps.getGeneratedKeys()) {
                    if (key.next()) usuario.setId(key.getInt(1));
                }
                return usuario;
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.insert:");
            e.printStackTrace();
        }
        return null;
    }

    /** Actualiza un usuario existente */
    public boolean update(Usuario usuario) {
        String sql = """
            UPDATE usuarios
               SET username = ?, password = ?, rol_id = ?, habilitado = ?
             WHERE id = ?
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            int rolId = obtenerRolId(c, usuario.getRol());
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPassword());
            ps.setInt(3, rolId);
            ps.setBoolean(4, usuario.isHabilitado());
            ps.setInt(5, usuario.getId());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.update:");
            e.printStackTrace();
            return false;
        }
    }

    /** Elimina un usuario por ID */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private int obtenerRolId(Connection c, String rolNombre) throws SQLException {
        String sql = "SELECT id FROM roles WHERE UPPER(nombre) = UPPER(?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rolNombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                throw new SQLException("Rol no encontrado: " + rolNombre);
            }
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

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
