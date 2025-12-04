package com.sga.marzad.dao;

import com.sga.marzad.model.AlumnoFinalInscripto;
import com.sga.marzad.model.ExamenFinal;
import com.sga.marzad.model.InscripcionFinal;
import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InscripcionFinalDAO {

    private final ExamenFinalDAO examenFinalDAO = new ExamenFinalDAO();

    public List<InscripcionFinal> listarPorAlumno(int alumnoId) {
        String sql = """
            SELECT ifi.id,
                   ifi.alumno_id,
                   ifi.examen_final_id,
                   ifi.fecha_insc,
                   ifi.estado,
                   m.nombre,
                   ef.fecha,
                   ef.aula
            FROM inscripciones_finales ifi
            JOIN examenes_finales ef ON ifi.examen_final_id = ef.id
            JOIN materias m ON ef.materia_id = m.id
            WHERE ifi.alumno_id = ?
            ORDER BY ef.fecha DESC
            """;

        List<InscripcionFinal> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
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

    public boolean insertar(int alumnoId, int examenFinalId) {
        ExamenFinal examenFinal = examenFinalDAO.obtenerPorId(examenFinalId);
        if (examenFinal == null || !examenFinal.tieneCupo()) {
            return false;
        }

        String sql = """
            INSERT INTO inscripciones_finales (alumno_id, examen_final_id, estado)
            VALUES (?, ?, 'ACTIVA')
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, examenFinalId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelar(int inscripcionId) {
        String sql = "DELETE FROM inscripciones_finales WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existeInscripcionActiva(int alumnoId, int examenFinalId) {
        String sql = """
            SELECT COUNT(1) FROM inscripciones_finales
            WHERE alumno_id = ? AND examen_final_id = ? AND estado = 'ACTIVA'
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, examenFinalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Inscripcion de admin con validacion de regularidad */
    public boolean inscribirAdmin(int alumnoId, int examenFinalId) {
        ExamenFinal examenFinal = examenFinalDAO.obtenerPorId(examenFinalId);
        if (examenFinal == null || !examenFinal.tieneCupo()) return false;
        if (existeInscripcionActiva(alumnoId, examenFinalId)) return false;
        if (!alumnoRegularEnMateria(alumnoId, examenFinal.getMateriaId())) return false;
        return insertar(alumnoId, examenFinalId);
    }

    public boolean eliminarInscripcionPorAlumno(int examenFinalId, int alumnoId) {
        String sql = "DELETE FROM inscripciones_finales WHERE examen_final_id = ? AND alumno_id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examenFinalId);
            ps.setInt(2, alumnoId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Valida si el alumno tiene la materia regular/aprobada (simplificada) */
    private boolean alumnoRegularEnMateria(int alumnoId, int materiaId) {
        String sqlNota = """
            SELECT COUNT(1)
              FROM inscripciones i
              JOIN calificaciones c ON c.inscripcion_id = i.id
             WHERE i.alumno_id = ? AND i.materia_id = ? AND c.nota >= 4
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlNota)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Si no hay notas aprobadas, considerar NO regular
        return false;
    }

    private InscripcionFinal mapRow(ResultSet rs) throws SQLException {
        Timestamp fechaInscTs = rs.getTimestamp("fecha_insc");
        Timestamp fechaExamenTs = rs.getTimestamp("fecha");
        LocalDateTime fechaInsc = fechaInscTs != null ? fechaInscTs.toLocalDateTime() : null;
        LocalDateTime fechaExamen = fechaExamenTs != null ? fechaExamenTs.toLocalDateTime() : null;
        return new InscripcionFinal(
                rs.getInt("id"),
                rs.getInt("alumno_id"),
                rs.getInt("examen_final_id"),
                fechaInsc,
                rs.getString("estado"),
                rs.getString("nombre"),
                fechaExamen,
                rs.getString("aula")
        );
    }

    public List<AlumnoFinalInscripto> listarAlumnosPorExamen(int examenFinalId) {
        List<AlumnoFinalInscripto> lista = new ArrayList<>();
        String sql = """
            SELECT a.id,
                   CONCAT(a.nombre, ' ', a.apellido) AS nombre,
                   a.dni,
                   ifi.estado
            FROM inscripciones_finales ifi
            JOIN alumnos a ON a.id = ifi.alumno_id
            WHERE ifi.examen_final_id = ?
            ORDER BY a.apellido, a.nombre
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examenFinalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AlumnoFinalInscripto(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("dni"),
                            rs.getString("estado")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
