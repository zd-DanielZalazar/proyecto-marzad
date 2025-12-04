package com.sga.marzad.dao;

import com.sga.marzad.model.Docente;
import com.sga.marzad.utils.ConexionBD;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class DocenteDAO {

    // Buscar docente por usuario_id
    public static Docente buscarPorUsuarioId(int usuarioId) {
        Docente docente = null;
        try (Connection conn = ConexionBD.getConnection()) {
            String sql = "SELECT * FROM docentes WHERE usuario_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                docente = new Docente(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("legajo"),
                        rs.getString("correo"),
                        getGeneroSafe(rs),
                        rs.getBoolean("habilitado")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docente;
    }

    // Buscar docente por id
    public static Docente buscarPorId(int id) {
        Docente docente = null;
        try (Connection conn = ConexionBD.getConnection()) {
            String sql = "SELECT * FROM docentes WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                docente = new Docente(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("legajo"),
                        rs.getString("correo"),
                        getGeneroSafe(rs),
                        rs.getBoolean("habilitado")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docente;
    }

    // Actualizar datos del docente (excepto legajo y usuario_id)
    public static boolean actualizar(int id, String nombre, String apellido, String correo, String genero) {
        // Verifica correo único
        if (!correoDisponible(id, correo)) {
            return false;
        }
        String sql = "UPDATE docentes SET nombre = ?, apellido = ?, correo = ?, genero = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, correo);
            stmt.setString(4, genero);
            stmt.setInt(5, id);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            // Si la columna genero no existe (bases antiguas), actualizar sin ese campo
            if (e.getMessage() != null && e.getMessage().contains("Unknown column 'genero'")) {
                try (Connection conn = ConexionBD.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE docentes SET nombre = ?, apellido = ?, correo = ? WHERE id = ?")) {
                    stmt.setString(1, nombre);
                    stmt.setString(2, apellido);
                    stmt.setString(3, correo);
                    stmt.setInt(4, id);
                    return stmt.executeUpdate() > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            e.printStackTrace();
            return false;
        }
    }

    /** obtiene Id por usuario */
    public int obtenerIdPorUsuarioId(int usuarioId) {
        String sql = "SELECT id FROM docentes WHERE usuario_id = ?";
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // No encontrado
    }

    // Verifica que el correo sea único (excepto para este docente)
    public static boolean correoDisponible(int idActual, String correo) {
        String sql = "SELECT id FROM docentes WHERE correo = ? AND id <> ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);
            stmt.setInt(2, idActual);
            ResultSet rs = stmt.executeQuery();
            return !rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            // En caso de error, mejor devolver false (no permitir duplicado por seguridad)
            return false;
        }
    }

    // Actualizar contraseña docente (por usuario_id)
    public static boolean actualizarPassword(int usuarioId, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevaPassword);
            stmt.setInt(2, usuarioId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener contraseña actual (texto plano)
    public static String obtenerPasswordActual(int usuarioId) {
        String sql = "SELECT password FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Docente> obtenerTodos() {
        return obtenerDocentesHabilitados();
    }

    /** Devuelve docentes habilitados y cuyo usuario tambien esta habilitado y rol = DOCENTE */
    public static List<Docente> obtenerDocentesHabilitados() {
        sincronizarDocentesDesdeUsuarios();
        List<Docente> docentes = new ArrayList<>();
        try {
            docentes.addAll(consultaDocentes(true));
        } catch (SQLException e) {
            // bases sin columna genero
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("genero")) {
                try {
                    docentes.addAll(consultaDocentes(false));
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }
        return docentes;
    }

    private static String getGeneroSafe(ResultSet rs) throws SQLException {
        try {
            return rs.getString("genero");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("genero")) {
                return null;
            }
            throw e;
        }
    }

    /** Inserta filas en docentes para usuarios con rol DOCENTE que no tengan entrada en docentes */
    private static void sincronizarDocentesDesdeUsuarios() {
        String sql = """
            INSERT INTO docentes (usuario_id, nombre, apellido, legajo, correo, habilitado)
            SELECT u.id, u.username, u.username, CONCAT('DOC', u.id), CONCAT(u.username, '@mail.local'), u.habilitado
              FROM usuarios u
              JOIN roles r ON r.id = u.rol_id
              LEFT JOIN docentes d ON d.usuario_id = u.id
             WHERE d.usuario_id IS NULL
               AND u.habilitado = 1
               AND UPPER(r.nombre) = 'DOCENTE'
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Si falla (por esquema viejo sin alguna columna), lo ignoramos: no interrumpe la carga
        }
    }

    private static List<Docente> consultaDocentes(boolean conGenero) throws SQLException {
        List<Docente> docentes = new ArrayList<>();
        String sqlConGenero = """
            SELECT d.id, d.usuario_id, d.nombre, d.apellido, d.legajo, d.correo, d.genero, d.habilitado
              FROM docentes d
              JOIN usuarios u ON u.id = d.usuario_id
              JOIN roles r ON r.id = u.rol_id
             WHERE d.habilitado = 1
               AND u.habilitado = 1
               AND UPPER(r.nombre) = 'DOCENTE'
             ORDER BY d.apellido, d.nombre
            """;
        String sqlSinGenero = """
            SELECT d.id, d.usuario_id, d.nombre, d.apellido, d.legajo, d.correo, d.habilitado
              FROM docentes d
              JOIN usuarios u ON u.id = d.usuario_id
              JOIN roles r ON r.id = u.rol_id
             WHERE d.habilitado = 1
               AND u.habilitado = 1
               AND UPPER(r.nombre) = 'DOCENTE'
             ORDER BY d.apellido, d.nombre
            """;
        String sql = conGenero ? sqlConGenero : sqlSinGenero;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String genero = null;
                if (conGenero) {
                    try { genero = rs.getString("genero"); } catch (SQLException ignore) { genero = null; }
                }
                docentes.add(new Docente(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("legajo"),
                        rs.getString("correo"),
                        genero,
                        rs.getBoolean("habilitado")
                ));
            }
        }
        return docentes;
    }

    /** Inserta un docente con datos basicos (para usuarios creados desde admin) */
    public boolean insert(Docente d) {
        String sqlConGenero = """
            INSERT INTO docentes (usuario_id, nombre, apellido, legajo, correo, genero, habilitado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        String sqlSinGenero = """
            INSERT INTO docentes (usuario_id, nombre, apellido, legajo, correo, habilitado)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = ConexionBD.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlConGenero)) {
                ps.setInt(1, d.getUsuarioId());
                ps.setString(2, d.getNombre());
                ps.setString(3, d.getApellido());
                ps.setString(4, d.getLegajo());
                ps.setString(5, d.getCorreo());
                ps.setString(6, d.getGenero());
                ps.setBoolean(7, d.isHabilitado());
                return ps.executeUpdate() == 1;
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("genero")) {
                    try (PreparedStatement ps2 = conn.prepareStatement(sqlSinGenero)) {
                        ps2.setInt(1, d.getUsuarioId());
                        ps2.setString(2, d.getNombre());
                        ps2.setString(3, d.getApellido());
                        ps2.setString(4, d.getLegajo());
                        ps2.setString(5, d.getCorreo());
                        ps2.setBoolean(6, d.isHabilitado());
                        return ps2.executeUpdate() == 1;
                    }
                }
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
