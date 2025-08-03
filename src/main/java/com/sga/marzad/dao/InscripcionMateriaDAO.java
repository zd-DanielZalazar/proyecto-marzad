package com.sga.marzad.dao;

import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InscripcionMateriaDAO {

    // Registrar inscripción de un alumno a una materia
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

    // Verificar si ya existe inscripción activa
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

    // Listar inscripciones de un alumno (puede filtrar por estado si querés)
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

    // Materias disponibles para inscribirse (incluye correlativas y estado)
    public List<MateriaDisponible> materiasDisponibles(int alumnoId, int carreraId) {
        List<MateriaDisponible> lista = new ArrayList<>();
        String sql = """
            SELECT
                m.id, m.nombre, m.anio, m.cuatrimestre,
                GROUP_CONCAT(cor.nombre SEPARATOR ',') AS correlativas
            FROM materias m
            LEFT JOIN correlatividades co ON co.materia_id = m.id
            LEFT JOIN materias cor ON co.correlativa_id = cor.id
            JOIN planes_estudio p ON m.plan_id = p.id
            JOIN carreras c ON p.carrera_id = c.id
            JOIN inscripciones_carrera ic ON ic.carrera_id = c.id AND ic.alumno_id = ?
            WHERE c.id = ?
              AND ic.estado = 'APROBADA'
              AND m.id NOT IN (
                  SELECT materia_id FROM inscripciones WHERE alumno_id = ? AND estado = 'ACTIVA'
              )
            GROUP BY m.id, m.nombre, m.anio, m.cuatrimestre
        """;
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, carreraId);
            ps.setInt(3, alumnoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Parsear correlativas
                List<String> correlativas = new ArrayList<>();
                String correlativasStr = rs.getString("correlativas");
                if (correlativasStr != null && !correlativasStr.isEmpty()) {
                    correlativas = Arrays.asList(correlativasStr.split(","));
                }
                // TODO: Acá podés calcular el estado real según la lógica de tu negocio
                String estado = "DISPONIBLE";

                MateriaDisponible mat = new MateriaDisponible(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("anio"),
                        rs.getInt("cuatrimestre"),
                        correlativas,
                        estado
                );
                lista.add(mat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
