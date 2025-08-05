package com.sga.marzad.dao;

import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlanEstudioDAO {
    public int crearPlanEstudio(int carreraId, String nombre) {
        int planId = -1;
        String sql = "INSERT INTO planes_estudio (carrera_id, nombre) VALUES (?, ?)";

        try {
            Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, carreraId);
            stmt.setString(2, nombre);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    planId = rs.getInt(1);
                }
            }
            // Cerrá recursos manualmente si no usás try-with-resources:
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return planId;
    }
}
