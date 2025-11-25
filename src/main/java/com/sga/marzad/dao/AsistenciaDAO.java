package com.sga.marzad.dao;

import com.sga.marzad.model.AsistenciaRegistro;
import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaDAO {

    public List<AsistenciaRegistro> listarPorInscripcion(int inscripcionId) {
        List<AsistenciaRegistro> lista = new ArrayList<>();
        String sql = """
            SELECT id, inscripcion_id, fecha, presente
            FROM asistencias
            WHERE inscripcion_id = ?
            ORDER BY fecha DESC
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AsistenciaRegistro(
                            rs.getInt("id"),
                            rs.getInt("inscripcion_id"),
                            rs.getDate("fecha").toLocalDate(),
                            rs.getBoolean("presente")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean guardar(int inscripcionId, int docenteId, LocalDate fecha, boolean presente) {
        String sql = """
            INSERT INTO asistencias (inscripcion_id, docente_id, fecha, presente)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE presente = VALUES(presente), docente_id = VALUES(docente_id)
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            ps.setInt(2, docenteId);
            ps.setDate(3, Date.valueOf(fecha));
            ps.setBoolean(4, presente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int asistenciaId) {
        String sql = "DELETE FROM asistencias WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, asistenciaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
