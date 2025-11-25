package com.sga.marzad.dao;

import com.sga.marzad.model.ExamenFinal;
import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamenFinalDAO {

    public List<ExamenFinal> listarDisponibles(int carreraId, int alumnoId) {
        String sql = """
            SELECT ef.id,
                   ef.materia_id,
                   m.nombre,
                   ef.fecha,
                   ef.aula,
                   ef.cupo,
                   ef.estado,
                   (
                        SELECT COUNT(1)
                        FROM inscripciones_finales ifi
                        WHERE ifi.examen_final_id = ef.id AND ifi.estado = 'ACTIVA'
                   ) AS inscriptos
            FROM examenes_finales ef
            JOIN materias m ON ef.materia_id = m.id
            JOIN planes_estudio p ON m.plan_id = p.id
            WHERE ef.estado = 'PUBLICADO'
              AND p.carrera_id = ?
              AND ef.id NOT IN (
                    SELECT examen_final_id
                    FROM inscripciones_finales
                    WHERE alumno_id = ? AND estado = 'ACTIVA'
              )
            ORDER BY ef.fecha
            """;

        List<ExamenFinal> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carreraId);
            ps.setInt(2, alumnoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public ExamenFinal obtenerPorId(int examenId) {
        String sql = """
            SELECT ef.id,
                   ef.materia_id,
                   m.nombre,
                   ef.fecha,
                   ef.aula,
                   ef.cupo,
                   ef.estado,
                   (
                        SELECT COUNT(1)
                        FROM inscripciones_finales ifi
                        WHERE ifi.examen_final_id = ef.id AND ifi.estado = 'ACTIVA'
                   ) AS inscriptos
            FROM examenes_finales ef
            JOIN materias m ON ef.materia_id = m.id
            WHERE ef.id = ?
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ExamenFinal> listarPorMateria(int materiaId) {
        String sql = """
            SELECT ef.id,
                   ef.materia_id,
                   m.nombre,
                   ef.fecha,
                   ef.aula,
                   ef.cupo,
                   ef.estado,
                   (
                        SELECT COUNT(1)
                        FROM inscripciones_finales ifi
                        WHERE ifi.examen_final_id = ef.id AND ifi.estado = 'ACTIVA'
                   ) AS inscriptos
            FROM examenes_finales ef
            JOIN materias m ON ef.materia_id = m.id
            WHERE ef.materia_id = ?
            ORDER BY ef.fecha DESC
            """;
        List<ExamenFinal> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private ExamenFinal mapRow(ResultSet rs) throws SQLException {
        LocalDateTime fecha = rs.getTimestamp("fecha") != null
                ? rs.getTimestamp("fecha").toLocalDateTime()
                : null;
        return new ExamenFinal(
                rs.getInt("id"),
                rs.getInt("materia_id"),
                rs.getString("nombre"),
                fecha,
                rs.getString("aula"),
                rs.getInt("cupo"),
                rs.getInt("inscriptos"),
                rs.getString("estado")
        );
    }
}
