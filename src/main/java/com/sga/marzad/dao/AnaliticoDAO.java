package com.sga.marzad.dao;

import com.sga.marzad.model.AnaliticoMateria;
import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnaliticoDAO {

    public List<AnaliticoMateria> listarPorAlumno(int alumnoId) {
        String sql = """
            SELECT m.nombre,
                   m.anio,
                   m.cuatrimestre,
                   MAX(c.nota) AS nota,
                   MAX(c.fecha_carga) AS fecha_carga,
                   CASE
                       WHEN MAX(c.nota) >= 4 THEN 'APROBADA'
                       WHEN MAX(c.nota) BETWEEN 1 AND 3.99 THEN 'DESAPROBADA'
                       WHEN i.estado = 'CANCELADA' THEN 'CANCELADA'
                       ELSE 'CURSANDO'
                   END AS condicion
            FROM inscripciones i
            JOIN materias m ON i.materia_id = m.id
            LEFT JOIN calificaciones c ON c.inscripcion_id = i.id
            WHERE i.alumno_id = ?
            GROUP BY m.nombre, m.anio, m.cuatrimestre, i.estado
            ORDER BY m.anio, m.cuatrimestre, m.nombre
            """;

        List<AnaliticoMateria> salida = new ArrayList<>();
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha_carga");
                    LocalDateTime fechaCarga = fecha != null ? fecha.toLocalDateTime() : null;
                    double notaValor = rs.getDouble("nota");
                    Double nota = rs.wasNull() ? null : notaValor;
                    String condicion = rs.getString("condicion");
                    if ("APROBADA".equalsIgnoreCase(condicion) || "REGULAR".equalsIgnoreCase(condicion)) {
                        salida.add(new AnaliticoMateria(
                                rs.getString("nombre"),
                                rs.getInt("anio"),
                                rs.getInt("cuatrimestre"),
                                nota,
                                condicion,
                                fechaCarga
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salida;
    }
}
