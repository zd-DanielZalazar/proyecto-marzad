package com.sga.marzad.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.utils.ConexionBD;

public class InscripcionMateriaDAO {

    /** Registra la inscripción de un alumno a una materia. */
    public boolean insertar(InscripcionMateria insc) {
        if (existeInscripcionActiva(insc.getAlumnoId(), insc.getMateriaId())) {
            return false;
        }
        if (reactivarInscripcionCancelada(insc)) {
            return true;
        }
        String sql = """
            INSERT INTO inscripciones (alumno_id, materia_id, inscripcion_carrera_id)
            VALUES (?, ?, ?)
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, insc.getAlumnoId());
            ps.setInt(2, insc.getMateriaId());
            ps.setInt(3, insc.getInscripcionCarreraId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Verifica si ya existe una inscripción activa del alumno a una materia. */
    public boolean existeInscripcionActiva(int alumnoId, int materiaId) {
        String sql = """
            SELECT COUNT(*) FROM inscripciones
             WHERE alumno_id = ? AND materia_id = ? AND estado = 'ACTIVA'
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Lista todas las inscripciones del alumno. */
    public List<InscripcionMateria> listarPorAlumno(int alumnoId) {
        List<InscripcionMateria> lista = new ArrayList<>();
        String sql = """
            SELECT i.id, i.alumno_id, i.materia_id, i.inscripcion_carrera_id, i.fecha_insc, i.estado
              FROM inscripciones i
              JOIN (
                   SELECT materia_id, MAX(id) AS id
                     FROM inscripciones
                    WHERE alumno_id = ?
                    GROUP BY materia_id
              ) ult ON ult.id = i.id
             WHERE i.alumno_id = ?
             ORDER BY i.fecha_insc DESC, i.id DESC
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, alumnoId);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Devuelve true si el alumno aprobó una materia (nota >= 4). */
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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Devuelve los IDs de materias correlativas requeridas para una materia. */
    public List<Integer> getCorrelativasIds(int materiaId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT correlativa_id FROM correlatividades WHERE materia_id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    /** Devuelve los nombres de materias correlativas requeridas para una materia. */
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
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) nombres.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombres;
    }

    /**
     * Devuelve las materias disponibles para inscripción,
     * validando correlatividades y devolviendo estado DISPONIBLE/BLOQUEADA/YA_APROBADA.
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
                   SELECT i.materia_id
                     FROM inscripciones i
                     LEFT JOIN calificaciones c2 ON c2.inscripcion_id = i.id
                    WHERE i.alumno_id = ?
                      AND (i.estado = 'ACTIVA' OR (c2.nota >= 4))
               )
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, carreraId);
            ps.setInt(3, alumnoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int materiaId = rs.getInt("id");
                    List<String> correlativas = getCorrelativasNombres(materiaId);

                    // Verificar si todas las correlativas están aprobadas
                    boolean habilitada = true;
                    for (int correlativaId : getCorrelativasIds(materiaId)) {
                        if (!materiaAprobada(alumnoId, correlativaId)) {
                            habilitada = false;
                            break;
                        }
                    }

                    String estado;
                    if (materiaAprobada(alumnoId, materiaId)) {
                        estado = "YA APROBADA";
                    } else if (habilitada) {
                        estado = "DISPONIBLE";
                    } else {
                        estado = "BLOQUEADA";
                    }

                    MateriaDisponible mat = new MateriaDisponible(
                            materiaId,
                            rs.getString("nombre"),
                            rs.getInt("anio"),
                            rs.getInt("cuatrimestre"),
                            correlativas,
                            estado
                    );
                    lista.add(mat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Devuelve el nombre de la materia por su ID. */
    public String obtenerNombreMateriaPorId(int materiaId) {
        String sql = "SELECT nombre FROM materias WHERE id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /** Marca una inscripción como cancelada (borrado lógico). */
    public boolean eliminar(int idInscripcion) {
        String sql = "UPDATE inscripciones SET estado = 'CANCELADA' WHERE id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idInscripcion);
            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Si existe una inscripcion cancelada para el mismo alumno y materia, se reactiva
     * en lugar de generar una fila nueva, evitando duplicados historicos.
     */
    private boolean reactivarInscripcionCancelada(InscripcionMateria insc) {
        String sql = """
            UPDATE inscripciones
               SET estado = 'ACTIVA',
                   inscripcion_carrera_id = ?,
                   fecha_insc = CURRENT_TIMESTAMP
             WHERE id = (
                 SELECT id FROM (
                     SELECT id
                       FROM inscripciones
                      WHERE alumno_id = ?
                        AND materia_id = ?
                      ORDER BY fecha_insc DESC, id DESC
                      LIMIT 1
                 ) tmp
             )
               AND estado = 'CANCELADA'
            """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, insc.getInscripcionCarreraId());
            ps.setInt(2, insc.getAlumnoId());
            ps.setInt(3, insc.getMateriaId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
