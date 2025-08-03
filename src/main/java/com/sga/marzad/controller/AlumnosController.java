package com.sga.marzad.controller;

import com.sga.marzad.model.Alumno;
import com.sga.marzad.utils.ConexionBD;
import java.sql.*;

public class AlumnosController {

    public Alumno buscarAlumnoPorUsuarioId(int usuarioId) {
        Alumno alumno = null;
        try (Connection conn = ConexionBD.getConnection()) {
            String sql = "SELECT * FROM alumnos WHERE usuario_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                alumno = new Alumno(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alumno;
    }

    // Si necesitas otros métodos, ponelos acá adentro
}
