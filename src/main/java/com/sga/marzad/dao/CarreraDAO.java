package com.sga.marzad.dao;

import com.sga.marzad.model.Carrera;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarreraDAO {

    /** Obtiene todas las carreras habilitadas */
    public List<Carrera> obtenerCarrerasHabilitadas() {
        List<Carrera> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, habilitado FROM carreras WHERE habilitado = TRUE";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Carrera carrera = new Carrera(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBoolean("habilitado")
                );
                lista.add(carrera);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Busca una carrera por su ID */
    public Carrera obtenerCarreraPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, habilitado FROM carreras WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Carrera(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getBoolean("habilitado")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Actualiza el nombre y la descripción de una carrera */
    public boolean actualizarCarrera(Carrera carrera) {
        String sql = "UPDATE carreras SET nombre=?, descripcion=? WHERE id=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, carrera.getNombre());
            ps.setString(2, carrera.getDescripcion());
            ps.setInt(3, carrera.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Deshabilita una carrera */
    public boolean deshabilitarCarrera(int id) {
        String sql = "UPDATE carreras SET habilitado=FALSE WHERE id=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Crea una nueva carrera y devuelve el ID generado */
    public int crearCarrera(String nombre, String descripcion) {
        String sql = "INSERT INTO carreras (nombre, descripcion, habilitado) VALUES (?, ?, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
