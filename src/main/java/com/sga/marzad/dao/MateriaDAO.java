package com.sga.marzad.dao;

import com.sga.marzad.model.Materia;
import com.sga.marzad.utils.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MateriaDAO {

    /** Obtener materias habilitadas por carrera */
    public List<Materia> obtenerMateriasPorCarrera(int carreraId) {
        List<Materia> lista = new ArrayList<>();
        String sql = """
            SELECT m.id,
                   m.plan_id,
                   m.nombre,
                   m.anio,
                   m.cuatrimestre,
                   m.creditos,
                   m.habilitado,
                   m.horario,
                   md.docente_id,
                   CONCAT(d.nombre, ' ', d.apellido) AS docente_nombre
              FROM materias m
              JOIN planes_estudio p ON m.plan_id = p.id
              LEFT JOIN materia_docente md ON md.materia_id = m.id
              LEFT JOIN docentes d ON d.id = md.docente_id
             WHERE p.carrera_id = ? AND m.habilitado = TRUE
            """;
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
                            rs.getBoolean("habilitado"),
                            rs.getObject("docente_id") != null ? rs.getInt("docente_id") : null,
                            rs.getString("docente_nombre"),
                            safeGetString(rs, "horario")
                    );
                    lista.add(materia);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("horario")) {
                return obtenerMateriasPorCarreraSinHorario(carreraId);
            }
            e.printStackTrace();
        }
        return lista;
    }

    private List<Materia> obtenerMateriasPorCarreraSinHorario(int carreraId) {
        List<Materia> lista = new ArrayList<>();
        String sql = """
            SELECT m.id,
                   m.plan_id,
                   m.nombre,
                   m.anio,
                   m.cuatrimestre,
                   m.creditos,
                   m.habilitado,
                   md.docente_id,
                   CONCAT(d.nombre, ' ', d.apellido) AS docente_nombre
              FROM materias m
              JOIN planes_estudio p ON m.plan_id = p.id
              LEFT JOIN materia_docente md ON md.materia_id = m.id
              LEFT JOIN docentes d ON d.id = md.docente_id
             WHERE p.carrera_id = ? AND m.habilitado = TRUE
            """;
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
                            rs.getBoolean("habilitado"),
                            rs.getObject("docente_id") != null ? rs.getInt("docente_id") : null,
                            rs.getString("docente_nombre"),
                            null
                    );
                    lista.add(materia);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Actualizar materia (sin creditos, con horario opcional) */
    public boolean actualizarMateria(Materia materia) {
        String sql = "UPDATE materias SET nombre=?, anio=?, cuatrimestre=?, horario=? WHERE id=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, materia.getNombre());
            ps.setInt(2, materia.getAnio());
            ps.setInt(3, materia.getCuatrimestre());
            ps.setString(4, materia.getHorario());
            ps.setInt(5, materia.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unknown column 'horario'")) {
                try (Connection conn = ConexionBD.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE materias SET nombre=?, anio=?, cuatrimestre=? WHERE id=?")) {
                    ps.setString(1, materia.getNombre());
                    ps.setInt(2, materia.getAnio());
                    ps.setInt(3, materia.getCuatrimestre());
                    ps.setInt(4, materia.getId());
                    return ps.executeUpdate() > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            e.printStackTrace();
            return false;
        }
    }

    /** Deshabilitar materia (eliminación lógica) */
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

    /** Crear nueva materia para una carrera (busca el plan de estudio) */
    public Materia crearMateria(String nombre, int carreraId) {
        String obtenerPlan = "SELECT id FROM planes_estudio WHERE carrera_id = ? LIMIT 1";
        String insertMateria = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, horario, habilitado) VALUES (?, ?, 1, 1, 0, ?, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement psPlan = conn.prepareStatement(obtenerPlan);
             PreparedStatement psInsert = conn.prepareStatement(insertMateria, Statement.RETURN_GENERATED_KEYS)) {
            psPlan.setInt(1, carreraId);
            try (ResultSet rsPlan = psPlan.executeQuery()) {
                if (!rsPlan.next()) return null;
                int planId = rsPlan.getInt("id");

                psInsert.setInt(1, planId);
                psInsert.setString(2, nombre);
                psInsert.setString(3, "");
                int filas = psInsert.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = psInsert.getGeneratedKeys()) {
                        if (rs.next()) {
                            int materiaId = rs.getInt(1);
                            return new Materia(materiaId, planId, nombre, 1, 1, 0, true, null, null, "");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("horario")) {
                return crearMateriaSinHorario(nombre, carreraId);
            }
            e.printStackTrace();
        }
        return null;
    }

    private Materia crearMateriaSinHorario(String nombre, int carreraId) {
        String obtenerPlan = "SELECT id FROM planes_estudio WHERE carrera_id = ? LIMIT 1";
        String insertMateria = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES (?, ?, 1, 1, 0, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement psPlan = conn.prepareStatement(obtenerPlan);
             PreparedStatement psInsert = conn.prepareStatement(insertMateria, Statement.RETURN_GENERATED_KEYS)) {
            psPlan.setInt(1, carreraId);
            try (ResultSet rsPlan = psPlan.executeQuery()) {
                if (!rsPlan.next()) return null;
                int planId = rsPlan.getInt("id");

                psInsert.setInt(1, planId);
                psInsert.setString(2, nombre);
                int filas = psInsert.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = psInsert.getGeneratedKeys()) {
                        if (rs.next()) {
                            int materiaId = rs.getInt(1);
                            return new Materia(materiaId, planId, nombre, 1, 1, 0, true, null, null, null);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Obtener correlativas de una materia */
    public List<Materia> obtenerCorrelativas(int materiaId) {
        List<Materia> lista = new ArrayList<>();
        String sql = """
            SELECT m2.id, m2.plan_id, m2.nombre, m2.anio, m2.cuatrimestre, m2.creditos, m2.habilitado, m2.horario
              FROM correlatividades c
              JOIN materias m2 ON c.correlativa_id = m2.id
             WHERE c.materia_id = ?
            """;
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
                            rs.getBoolean("habilitado"),
                            null,
                            null,
                            safeGetString(rs, "horario")
                    );
                    lista.add(correlativa);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Agregar correlativa */
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

    /** Crear materia completa con datos de plan */
    public int crearMateriaCompleta(String nombre, int planId, int anio, int cuatrimestre, int creditos) {
        String sql = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, horario, habilitado) VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, planId);
            ps.setString(2, nombre);
            ps.setInt(3, anio);
            ps.setInt(4, cuatrimestre);
            ps.setInt(5, creditos);
            ps.setString(6, "");
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("horario")) {
                return crearMateriaCompletaSinHorario(nombre, planId, anio, cuatrimestre, creditos);
            }
            e.printStackTrace();
        }
        return -1;
    }

    /** Crear materia usando carreraId como plan_id */
    public int crearMateriaSimple(String nombre, int carreraId, int anio, int cuatrimestre, int creditos) {
        String sql = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, horario, habilitado) VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, carreraId); // Se usa carreraId como plan_id (compatibilidad)
            ps.setString(2, nombre);
            ps.setInt(3, anio);
            ps.setInt(4, cuatrimestre);
            ps.setInt(5, creditos);
            ps.setString(6, "");
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("horario")) {
                return crearMateriaSimpleSinHorario(nombre, carreraId, anio, cuatrimestre, creditos);
            }
            e.printStackTrace();
        }
        return -1;
    }

    private int crearMateriaCompletaSinHorario(String nombre, int planId, int anio, int cuatrimestre, int creditos) {
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
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int crearMateriaSimpleSinHorario(String nombre, int carreraId, int anio, int cuatrimestre, int creditos) {
        String sql = "INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, carreraId);
            ps.setString(2, nombre);
            ps.setInt(3, anio);
            ps.setInt(4, cuatrimestre);
            ps.setInt(5, creditos);
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

    /** Asignar docente (reemplaza el actual) */
    public boolean asignarDocente(int materiaId, Integer docenteId) {
        String deleteSql = "DELETE FROM materia_docente WHERE materia_id = ?";
        String insertSql = "INSERT INTO materia_docente (materia_id, docente_id) VALUES (?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement del = conn.prepareStatement(deleteSql)) {
            del.setInt(1, materiaId);
            del.executeUpdate();

            if (docenteId != null) {
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setInt(1, materiaId);
                    ins.setInt(2, docenteId);
                    ins.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String safeGetString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    /** Listar todas las materias habilitadas (admin) */
    public List<Materia> obtenerTodasHabilitadas() {
        List<Materia> lista = new ArrayList<>();
        String sql = """
            SELECT id, plan_id, nombre, anio, cuatrimestre, creditos, habilitado
              FROM materias
             WHERE habilitado = 1
             ORDER BY nombre
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Materia(
                        rs.getInt("id"),
                        rs.getInt("plan_id"),
                        rs.getString("nombre"),
                        rs.getInt("anio"),
                        rs.getInt("cuatrimestre"),
                        rs.getInt("creditos"),
                        rs.getBoolean("habilitado")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Obtener la carrera a la que pertenece una materia */
    public Integer obtenerCarreraIdPorMateria(int materiaId) {
        String sql = """
            SELECT p.carrera_id
              FROM materias m
              JOIN planes_estudio p ON m.plan_id = p.id
             WHERE m.id = ?
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("carrera_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
