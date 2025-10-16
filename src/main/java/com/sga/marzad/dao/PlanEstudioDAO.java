package com.sga.marzad.dao;

import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlanEstudioDAO {

    /** Crea un plan de estudio para una carrera y devuelve su ID generado */
    public int crearPlanEstudio(int carreraId, String nombre) {
        int planId = -1;
        String sql = "INSERT INTO planes_estudio (carrera_id, nombre) VALUES (?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, carreraId);
            stmt.setString(2, nombre);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        planId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return planId;
    }
}
