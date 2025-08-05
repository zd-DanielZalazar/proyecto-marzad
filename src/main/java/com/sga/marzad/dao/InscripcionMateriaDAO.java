package com.sga.marzad.dao;

import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscripcionMateriaDAO {

    /**
     * Registra la inscripción de un alumno a una materia.
     */
    public boolean insertar(InscripcionMateria insc) {
        String sql = """
            INSERT INTO inscripciones (alumno_id, materia_id, inscripcion_carrera_id)
            VALUES (?, ?, ?)
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, insc.getAlumnoId());
            ps.setInt(2, insc.getMateriaId());
            ps.setInt(3, insc.getInscripcionCarreraId());
            int filas = ps.executeUpdate();
            return filas == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifica si ya existe una inscripción activa del alumno a una materia.
     */
    public boolean existeInscripcionActiva(int alumnoId, int materiaId) {
        String sql = """
            SELECT COUNT(*) FROM inscripciones
             WHERE alumno_id = ? AND materia_id = ? AND estado = 'ACTIVA'
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, materiaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lista todas las inscripciones del alumno.
     */
    public List<InscripcionMateria> listarPorAlumno(int alumnoId) {
        List<InscripcionMateria> lista = new ArrayList<>();
        String sql = """
            SELECT id, alumno_id, materia_id, inscripcion_carrera_id, fecha_insc, estado
              FROM inscripciones
             WHERE alumno_id = ?
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InscripcionMateria insc = new InscripcionMateria(
                        rs.getInt("id"),
                        rs.getInt("alumno_id"),
                        rs.getInt("materia_id"),
                        rs.getInt("inscripcion_carrera_id"),
                        rs.getTimestamp("fecha_insc").toLocalDateTime(),
                        rs.getString("estado")
                );
                lista.add(insc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Devuelve true si el alumno aprobó una materia (nota >= 4 en alguna calificación de esa materia).
     */
    public boolean materiaAprobada(int alumnoId, int materiaId) {
        String sql = """
            SELECT COUNT(*) FROM calificaciones c
            JOIN inscripciones i ON c.inscripcion_id = i.id
            WHERE i.alumno_id = ? AND i.materia_id = ? AND c.nota >= 4
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, materiaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Devuelve los IDs de materias correlativas requeridas para una materia.
     */
    public List<Integer> getCorrelativasIds(int materiaId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT correlativa_id FROM correlatividades WHERE materia_id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * Devuelve los nombres de materias correlativas requeridas para una materia.
     */
    public List<String> getCorrelativasNombres(int materiaId) {
        List<String> nombres = new ArrayList<>();
        String sql = """
            SELECT m2.nombre FROM correlatividades c
            JOIN materias m2 ON c.correlativa_id = m2.id
            WHERE c.materia_id = ?
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) nombres.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombres;
    }

    /**
     * Devuelve las materias de la carrera que aún puede inscribirse el alumno (no inscripto y carrera aprobada).
     */
    public List<MateriaDisponible> materiasDisponibles(int alumnoId, int carreraId) {
        List<MateriaDisponible> lista = new ArrayList<>();
        String sql = """
            SELECT m.id, m.nombre, m.anio, m.cuatrimestre
              FROM materias m
              JOIN planes_estudio p ON m.plan_id = p.id
              JOIN carreras c ON p.carrera_id = c.id
              JOIN inscripciones_carrera ic ON ic.carrera_id = c.id AND ic.alumno_id = ?
             WHERE c.id = ?
               AND ic.estado IN ('APROBADA', 'PENDIENTE')
               AND m.id NOT IN (
                   SELECT materia_id FROM inscripciones WHERE alumno_id = ? AND estado = 'ACTIVA'
               )
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, carreraId);
            ps.setInt(3, alumnoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int materiaId = rs.getInt("id");
                MateriaDisponible mat = new MateriaDisponible(
                        materiaId,
                        rs.getString("nombre"),
                        rs.getInt("anio"),
                        rs.getInt("cuatrimestre"),
                        getCorrelativasNombres(materiaId),
                        "DISPONIBLE"
                );
                lista.add(mat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Devuelve el nombre de la materia por su ID.
     * Útil para mostrar en la tabla de inscripciones.
     */
    public String obtenerNombreMateriaPorId(int materiaId) {
        String sql = "SELECT nombre FROM materias WHERE id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
