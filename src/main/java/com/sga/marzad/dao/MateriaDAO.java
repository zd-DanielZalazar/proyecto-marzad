package com.sga.marzad.dao;

import com.sga.marzad.model.Materia;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MateriaDAO {

    // Obtener materias habilitadas por carrera
    public List<Materia> obtenerMateriasPorCarrera(int carreraId) {
        List<Materia> lista = new ArrayList<>();
        String sql = "SELECT m.id, m.plan_id, m.nombre, m.anio, m.cuatrimestre, m.creditos, m.habilitado " +
                "FROM materias m " +
                "JOIN planes_estudio p ON m.plan_id = p.id " +
                "WHERE p.carrera_id = ? AND m.habilitado = TRUE";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carreraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Materia materia = new Materia(
                            rs.getInt("id"),
                            rs.getInt("plan_id"),
                            rs.getString("nombre"),
                            rs.getInt("anio"),
                            rs.getInt("cuatrimestre"),
                            rs.getInt("creditos"),
                            rs.getBoolean("habilitado")
                    );
                    lista.add(materia);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Actualizar materia
    public boolean actualizarMateria(Materia materia) {
        String sql = "UPDATE materias SET nombre=?, anio=?, cuatrimestre=?, creditos=? WHERE id=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, materia.getNombre());
            ps.setInt(2, materia.getAnio());
            ps.setInt(3, materia.getCuatrimestre());
            ps.setInt(4, materia.getCreditos());
            ps.setInt(5, materia.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deshabilitar materia (eliminación lógica)
    public boolean deshabilitarMateria(int id) {
        String sql = "UPDATE materias SET habilitado=FALSE WHERE id=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Crear nueva materia para una carrera (busca el plan de estudio)
    public Materia crearMateria(String nombre, int carreraId) {
        String obtenerPlan = "SELECT id FROM planes_estudio WHERE carrera_id = ? LIMIT 1";
        String insertMateria = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES (?, ?, 1, 1, 0, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement psPlan = conn.prepareStatement(obtenerPlan);
             PreparedStatement psInsert = conn.prepareStatement(insertMateria, Statement.RETURN_GENERATED_KEYS)) {
            // Buscar plan
            psPlan.setInt(1, carreraId);
            ResultSet rsPlan = psPlan.executeQuery();
            if (!rsPlan.next()) return null;
            int planId = rsPlan.getInt("id");
            // Insertar materia
            psInsert.setInt(1, planId);
            psInsert.setString(2, nombre);
            int filas = psInsert.executeUpdate();
            if (filas > 0) {
                ResultSet rs = psInsert.getGeneratedKeys();
                if (rs.next()) {
                    int materiaId = rs.getInt(1);
                    return new Materia(materiaId, planId, nombre, 1, 1, 0, true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtener correlativas de una materia
    public List<Materia> obtenerCorrelativas(int materiaId) {
        List<Materia> lista = new ArrayList<>();
        String sql = "SELECT m2.id, m2.plan_id, m2.nombre, m2.anio, m2.cuatrimestre, m2.creditos, m2.habilitado " +
                "FROM correlatividades c " +
                "JOIN materias m2 ON c.correlativa_id = m2.id " +
                "WHERE c.materia_id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Materia correlativa = new Materia(
                            rs.getInt("id"),
                            rs.getInt("plan_id"),
                            rs.getString("nombre"),
                            rs.getInt("anio"),
                            rs.getInt("cuatrimestre"),
                            rs.getInt("creditos"),
                            rs.getBoolean("habilitado")
                    );
                    lista.add(correlativa);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Agregar correlativa
    public boolean agregarCorrelativa(int materiaId, int correlativaId) {
        String sql = "INSERT INTO correlatividades (materia_id, correlativa_id) VALUES (?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            ps.setInt(2, correlativaId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Si es duplicado, ignorar error
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return true;
            }
            e.printStackTrace();
            return false;
        }
    }
    public int crearMateriaCompleta(String nombre, int planId, int anio, int cuatrimestre, int creditos) {
        String sql = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, planId);
            ps.setString(2, nombre);
            ps.setInt(3, anio);
            ps.setInt(4, cuatrimestre);
            ps.setInt(5, creditos);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    // Usando carreraId como "plan_id" (si tu tabla lo requiere)
    public int crearMateriaSimple(String nombre, int carreraId, int anio, int cuatrimestre, int creditos) {
        String sql = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, carreraId); // Usar carreraId como plan_id (por compatibilidad)
            ps.setString(2, nombre);
            ps.setInt(3, anio);
            ps.setInt(4, cuatrimestre);
            ps.setInt(5, creditos);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


}
