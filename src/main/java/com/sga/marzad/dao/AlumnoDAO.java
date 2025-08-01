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
                        rs.getDate("fecha_nac").toLocalDate(),
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
            ps.setDate(6, Date.valueOf(a.getFechaNac()));
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
            ps.setDate(6, Date.valueOf(a.getFechaNac()));
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
}
