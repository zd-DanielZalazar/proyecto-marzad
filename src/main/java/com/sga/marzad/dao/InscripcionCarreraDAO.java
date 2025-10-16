package com.sga.marzad.dao;

import com.sga.marzad.model.InscripcionCarrera;
import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InscripcionCarreraDAO {

    /**
     * Devuelve la inscripción activa del alumno a una carrera, o null si no hay ninguna.
     * IMPORTANTE: Solo devuelve UNA, si tuviera más de una inscripta ACTIVA (no debería pasar).
     */
    public InscripcionCarrera obtenerInscripcionActivaPorAlumno(int alumnoId) {
        String sql = """
            SELECT ic.id, ic.alumno_id, ic.carrera_id, c.nombre, ic.estado
            FROM inscripciones_carrera ic
            JOIN carreras c ON ic.carrera_id = c.id
            WHERE ic.alumno_id = ? AND ic.estado IN ('APROBADA')
            LIMIT 1
            """;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new InscripcionCarrera(
                            rs.getInt("id"),
                            rs.getInt("alumno_id"),
                            rs.getInt("carrera_id"),
                            rs.getString("nombre"),
                            rs.getString("estado")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
