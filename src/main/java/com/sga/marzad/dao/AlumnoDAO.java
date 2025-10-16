package com.sga.marzad.dao;

import com.sga.marzad.model.Alumno;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO {

    /** Recupera todos los alumnos */
    public List<Alumno> findAll() {
        List<Alumno> lista = new ArrayList<>();
        String sql = """
            SELECT id, usuario_id, nombre, apellido,
                   dni, correo, fecha_nac, genero, habilitado
              FROM alumnos
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Alumno a = new Alumno(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getString("correo"),
                        rs.getDate("fecha_nac") != null ? rs.getDate("fecha_nac").toLocalDate() : null,
                        rs.getString("genero"),
                        rs.getBoolean("habilitado")
                );
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Inserta un nuevo alumno */
    public boolean insert(Alumno a) {
        String sql = """
            INSERT INTO alumnos(usuario_id, nombre, apellido, dni, correo,
                                 fecha_nac, genero, habilitado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getUsuarioId());
            ps.setString(2, a.getNombre());
            ps.setString(3, a.getApellido());
            ps.setString(4, a.getDni());
            ps.setString(5, a.getCorreo());
            ps.setDate(6, a.getFechaNac() != null ? Date.valueOf(a.getFechaNac()) : null);
            ps.setString(7, a.getGenero());
            ps.setBoolean(8, a.isHabilitado());

            int rows = ps.executeUpdate();
            if (rows == 1) {
                try (ResultSet key = ps.getGeneratedKeys()) {
                    if (key.next()) a.setId(key.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** obtiene Id por usuario */
    public int obtenerIdPorUsuarioId(int usuarioId) {
        String sql = "SELECT id FROM alumnos WHERE usuario_id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // No encontrado
    }

    /** Actualiza un alumno existente */
    public boolean update(Alumno a) {
        String sql = """
            UPDATE alumnos
               SET usuario_id=?, nombre=?, apellido=?, dni=?, correo=?,
                   fecha_nac=?, genero=?, habilitado=?
             WHERE id=?
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, a.getUsuarioId());
            ps.setString(2, a.getNombre());
            ps.setString(3, a.getApellido());
            ps.setString(4, a.getDni());
            ps.setString(5, a.getCorreo());
            ps.setDate(6, a.getFechaNac() != null ? Date.valueOf(a.getFechaNac()) : null);
            ps.setString(7, a.getGenero());
            ps.setBoolean(8, a.isHabilitado());
            ps.setInt(9, a.getId());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Elimina un alumno por ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM alumnos WHERE id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Recupera un alumno por usuario_id */
    public static Alumno buscarPorUsuarioId(int usuarioId) {
        Alumno alumno = null;
        String sql = "SELECT * FROM alumnos WHERE usuario_id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                alumno = new Alumno(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getString("correo"),
                        rs.getDate("fecha_nac") != null ? rs.getDate("fecha_nac").toLocalDate() : null,
                        rs.getString("genero"),
                        rs.getBoolean("habilitado")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alumno;
    }

    /** Actualiza sólo los campos permitidos del perfil */
    public static boolean actualizar(int id, String nombre, String apellido, String correo, String genero, LocalDate fechaNac) {
        String sql = "UPDATE alumnos SET nombre=?, apellido=?, correo=?, genero=?, fecha_nac=? WHERE id=?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, correo);
            ps.setString(4, genero);
            ps.setDate(5, fechaNac != null ? Date.valueOf(fechaNac) : null);
            ps.setInt(6, id);

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Obtiene la contraseña actual del usuario (texto plano) */
    public static String obtenerPasswordActual(int usuarioId) {
        String sql = "SELECT password FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Actualiza la contraseña del usuario */
    public static boolean actualizarPassword(int usuarioId, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevaPassword);
            stmt.setInt(2, usuarioId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
