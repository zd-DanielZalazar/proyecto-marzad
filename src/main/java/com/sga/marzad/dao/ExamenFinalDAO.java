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

    /** Listado completo (admin) */
    public List<ExamenFinal> listarTodos() {
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
            ORDER BY ef.fecha DESC
            """;
        List<ExamenFinal> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean crearMesa(int materiaId, LocalDateTime fecha, String aula, int cupo) {
        String sql = """
            INSERT INTO examenes_finales (materia_id, fecha, aula, cupo, estado)
            VALUES (?, ?, ?, ?, 'PUBLICADO')
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            ps.setTimestamp(2, fecha != null ? java.sql.Timestamp.valueOf(fecha) : null);
            ps.setString(3, aula);
            ps.setInt(4, cupo);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarMesa(int id, LocalDateTime fecha, String aula, int cupo) {
        int inscriptos = contarInscriptos(id);
        if (cupo < inscriptos) {
            return false;
        }
        String sql = """
            UPDATE examenes_finales
               SET fecha = ?, aula = ?, cupo = ?
             WHERE id = ?
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, fecha != null ? java.sql.Timestamp.valueOf(fecha) : null);
            ps.setString(2, aula);
            ps.setInt(3, cupo);
            ps.setInt(4, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarMesa(int id) {
        int inscriptos = contarInscriptos(id);
        if (inscriptos > 0) return false;
        String sql = "DELETE FROM examenes_finales WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int contarInscriptos(int examenId) {
        String sql = "SELECT COUNT(1) FROM inscripciones_finales WHERE examen_final_id = ? AND estado = 'ACTIVA'";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
