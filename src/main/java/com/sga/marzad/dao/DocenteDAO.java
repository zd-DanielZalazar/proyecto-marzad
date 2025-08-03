package com.sga.marzad.dao;

import com.sga.marzad.model.Docente;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;

public class DocenteDAO {

    // Buscar docente por usuario_id
    public static Docente buscarPorUsuarioId(int usuarioId) {
        Docente docente = null;
        try (Connection conn = ConexionBD.getConnection()) {
            String sql = "SELECT * FROM docentes WHERE usuario_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                docente = new Docente(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("legajo"),
                        rs.getString("correo"),
                        rs.getString("genero"),
                        rs.getBoolean("habilitado")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docente;
    }

    // Buscar docente por id
    public static Docente buscarPorId(int id) {
        Docente docente = null;
        try (Connection conn = ConexionBD.getConnection()) {
            String sql = "SELECT * FROM docentes WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                docente = new Docente(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("legajo"),
                        rs.getString("correo"),
                        rs.getString("genero"),
                        rs.getBoolean("habilitado")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docente;
    }

    // Actualizar datos del docente (excepto legajo y usuario_id)
    public static boolean actualizar(int id, String nombre, String apellido, String correo, String genero) {
        // Verifica correo único
        if (!correoDisponible(id, correo)) {
            return false;
        }
        String sql = "UPDATE docentes SET nombre = ?, apellido = ?, correo = ?, genero = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, correo);
            stmt.setString(4, genero);
            stmt.setInt(5, id);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica que el correo sea único (excepto para este docente)
    public static boolean correoDisponible(int idActual, String correo) {
        String sql = "SELECT id FROM docentes WHERE correo = ? AND id <> ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);
            stmt.setInt(2, idActual);
            ResultSet rs = stmt.executeQuery();
            return !rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            // En caso de error, mejor devolver false (no permitir duplicado por seguridad)
            return false;
        }
    }

    // Actualizar contraseña docente (por usuario_id)
    public static boolean actualizarPassword(int usuarioId, String nuevaPasswordHasheada) {
        // Asume que el hash se hace en otro lado (por seguridad)
        String sql = "UPDATE usuarios SET hash_password = ? WHERE id = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevaPasswordHasheada);
            stmt.setInt(2, usuarioId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener contraseña hasheada actual (para verificar al cambiar contraseña)
    public static String obtenerPasswordActual(int usuarioId) {
        String sql = "SELECT hash_password FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("hash_password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
