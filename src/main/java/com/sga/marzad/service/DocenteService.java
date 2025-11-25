package com.sga.marzad.service;

import com.sga.marzad.dao.AsistenciaDAO;
import com.sga.marzad.dao.ExamenFinalDAO;
import com.sga.marzad.dao.InscripcionFinalDAO;
import com.sga.marzad.model.AlumnoFinalInscripto;
import com.sga.marzad.model.AlumnoNotasDocente;
import com.sga.marzad.model.AsistenciaAlumnoRow;
import com.sga.marzad.model.AsistenciaRegistro;
import com.sga.marzad.model.ExamenFinal;
import com.sga.marzad.model.Materia;
import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteService {

    private final ExamenFinalDAO examenFinalDAO = new ExamenFinalDAO();
    private final InscripcionFinalDAO inscripcionFinalDAO = new InscripcionFinalDAO();
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    public List<Materia> obtenerMateriasPorDocente(int docenteId) {
        List<Materia> materias = new ArrayList<>();
        String sql = """
            SELECT m.id, m.plan_id, m.nombre, m.anio, m.cuatrimestre, m.creditos, m.habilitado
            FROM materia_docente md
            JOIN materias m ON md.materia_id = m.id
            WHERE md.docente_id = ?
            ORDER BY m.anio, m.nombre
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    materias.add(new Materia(
                            rs.getInt("id"),
                            rs.getInt("plan_id"),
                            rs.getString("nombre"),
                            rs.getInt("anio"),
                            rs.getInt("cuatrimestre"),
                            rs.getInt("creditos"),
                            rs.getBoolean("habilitado")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materias;
    }

    public List<AlumnoNotasDocente> obtenerAlumnosNotasPorMateria(int docenteId, int materiaId) {
        List<AlumnoNotasDocente> alumnos = new ArrayList<>();
        String sql = """
            SELECT i.id AS inscripcion_id,
                   a.id AS alumno_id,
                   CONCAT(a.nombre, ' ', a.apellido) AS nombre,
                   a.dni,
                   a.correo,
                   i.estado,
                   MAX(CASE WHEN c.tipo = 'PARCIAL_1' THEN c.nota END) AS parcial1,
                   MAX(CASE WHEN c.tipo = 'RECUP_1' THEN c.nota END) AS recup1,
                   MAX(CASE WHEN c.tipo = 'PARCIAL_2' THEN c.nota END) AS parcial2,
                   MAX(CASE WHEN c.tipo = 'RECUP_2' THEN c.nota END) AS recup2,
                   MAX(CASE WHEN c.tipo = 'FINAL' THEN c.nota END) AS nota_final
            FROM inscripciones i
            JOIN alumnos a ON a.id = i.alumno_id
            JOIN materia_docente md ON md.materia_id = i.materia_id
            LEFT JOIN calificaciones c ON c.inscripcion_id = i.id
            WHERE md.docente_id = ? AND i.materia_id = ?
              AND i.estado IN ('ACTIVA','CANCELADA')
            GROUP BY i.id, a.id, nombre, a.dni, a.correo, i.estado
            ORDER BY nombre
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.setInt(2, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alumnos.add(new AlumnoNotasDocente(
                            rs.getInt("inscripcion_id"),
                            rs.getInt("alumno_id"),
                            rs.getString("nombre"),
                            rs.getString("dni"),
                            rs.getString("correo"),
                            rs.getString("estado"),
                            getNullableDouble(rs, "parcial1"),
                            getNullableDouble(rs, "recup1"),
                            getNullableDouble(rs, "parcial2"),
                            getNullableDouble(rs, "recup2"),
                            getNullableDouble(rs, "nota_final")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alumnos;
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    public void guardarNota(int inscripcionId, int docenteId, String tipoNota, Double valor) {
        String sql = """
            INSERT INTO calificaciones (inscripcion_id, docente_id, tipo, nota)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE nota = VALUES(nota), fecha_carga = CURRENT_TIMESTAMP
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            ps.setInt(2, docenteId);
            ps.setString(3, tipoNota);
            ps.setDouble(4, valor);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminarTodasLasNotas(int inscripcionId, int docenteId) {
        String sql = "DELETE FROM calificaciones WHERE inscripcion_id = ? AND docente_id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            ps.setInt(2, docenteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ExamenFinal> obtenerFinalesPorMateria(int materiaId) {
        return examenFinalDAO.listarPorMateria(materiaId);
    }

    public List<AlumnoFinalInscripto> obtenerAlumnosFinales(int examenId) {
        return inscripcionFinalDAO.listarAlumnosPorExamen(examenId);
    }

    public List<AsistenciaRegistro> obtenerAsistencias(int inscripcionId) {
        return asistenciaDAO.listarPorInscripcion(inscripcionId);
    }

    public List<AsistenciaAlumnoRow> obtenerAsistenciaDiaria(int docenteId, int materiaId, LocalDate fecha) {
        String sql = """
            SELECT i.id AS inscripcion_id,
                   a.id AS alumno_id,
                   CONCAT(a.nombre, ' ', a.apellido) AS nombre,
                   a.dni,
                   IFNULL(ast.presente, FALSE) AS presente
            FROM inscripciones i
            JOIN alumnos a ON a.id = i.alumno_id
            JOIN materia_docente md ON md.materia_id = i.materia_id AND md.docente_id = ?
            LEFT JOIN asistencias ast ON ast.inscripcion_id = i.id AND ast.fecha = ?
            WHERE i.materia_id = ?
              AND i.estado IN ('ACTIVA','CANCELADA')
            ORDER BY nombre
            """;
        List<AsistenciaAlumnoRow> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setInt(3, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AsistenciaAlumnoRow(
                            rs.getInt("inscripcion_id"),
                            rs.getInt("alumno_id"),
                            rs.getString("nombre"),
                            rs.getString("dni"),
                            rs.getBoolean("presente")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean guardarAsistencia(int inscripcionId, int docenteId, LocalDate fecha, boolean presente) {
        return asistenciaDAO.guardar(inscripcionId, docenteId, fecha, presente);
    }

    public boolean eliminarAsistencia(int asistenciaId) {
        return asistenciaDAO.eliminar(asistenciaId);
    }
}
