package com.sga.marzad.dao;

import com.sga.marzad.service.AnaliticoParcialService.Materia;

import java.sql.*;
import java.util.*;

public class AnaliticoParcialDAO {

    private static Connection conectar() throws Exception {
        String url = "jdbc:mysql://localhost:3306/gestion_academica";
        String user = "root";
        String pass = "tu_contraseña";
        return DriverManager.getConnection(url, user, pass);
    }

    // --- Registrar solicitud del alumno ---
    public static void registrarSolicitud(int alumnoId) {
        try (Connection conn = conectar()) {
            String sql = "INSERT INTO solicitudes_analitico (alumno_id, estado, fecha_solicitud) VALUES (?, 'pendiente', NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, alumnoId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Obtener materias cursadas por alumno ---
    public static List<Materia> obtenerMateriasCursadas(int alumnoId) {
        List<Materia> materias = new ArrayList<>();
        try (Connection conn = conectar()) {
            String sql = "SELECT m.nombre FROM materias m JOIN cursadas c ON m.id = c.materia_id WHERE c.alumno_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, alumnoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                materias.add(new Materia(rs.getString("nombre")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return materias;
    }

    // --- Obtener correlativas por materia ---
    public static Map<String, List<String>> obtenerCorrelativas(List<Materia> materias) {
        Map<String, List<String>> mapa = new HashMap<>();
        try (Connection conn = conectar()) {
            String sql = "SELECT m1.nombre AS materia, m2.nombre AS correlativa " +
                    "FROM correlatividades c " +
                    "JOIN materias m1 ON c.materia_id = m1.id " +
                    "JOIN materias m2 ON c.correlativa_id = m2.id " +
                    "WHERE m1.nombre = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (Materia materia : materias) {
                stmt.setString(1, materia.getNombre());
                ResultSet rs = stmt.executeQuery();
                List<String> correlativas = new ArrayList<>();
                while (rs.next()) {
                    correlativas.add(rs.getString("correlativa"));
                }
                mapa.put(materia.getNombre(), correlativas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapa;
    }

    // --- Obtener solicitudes pendientes ---
    public static List<SolicitudAnalitico> obtenerSolicitudesPendientes() {
        List<SolicitudAnalitico> lista = new ArrayList<>();
        try (Connection conn = conectar()) {
            String sql = "SELECT s.id, a.nombre, a.apellido, a.dni, s.fecha_solicitud " +
                    "FROM solicitudes_analitico s JOIN alumnos a ON s.alumno_id = a.id " +
                    "WHERE s.estado = 'pendiente'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                java.util.Date fechaConvertida = new java.util.Date(rs.getDate("fecha_solicitud").getTime());
                lista.add(new SolicitudAnalitico(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        fechaConvertida
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // --- Validar solicitud con firma y sello ---
    public static void validarSolicitud(int solicitudId, String firma, String sello) {
        try (Connection conn = conectar()) {
            String sql = "UPDATE solicitudes_analitico SET estado = 'validado', firma = ?, sello = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, firma);
            stmt.setString(2, sello);
            stmt.setInt(3, solicitudId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Obtener datos completos de una solicitud ---
    public static SolicitudAnalitico obtenerSolicitudPorId(int solicitudId) {
        try (Connection conn = conectar()) {
            String sql = "SELECT s.id, a.nombre, a.apellido, a.dni, s.fecha_solicitud, s.firma, s.sello " +
                    "FROM solicitudes_analitico s JOIN alumnos a ON s.alumno_id = a.id WHERE s.id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, solicitudId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                java.util.Date fechaConvertida = new java.util.Date(rs.getDate("fecha_solicitud").getTime());
                return new SolicitudAnalitico(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        fechaConvertida,
                        rs.getString("firma"),
                        rs.getString("sello")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Clase auxiliar para solicitudes ---
    public static class SolicitudAnalitico {
        private int id;
        private String nombre;
        private String apellido;
        private String dni;
        private java.util.Date fechaSolicitud;
        private String firma;
        private String sello;

        public SolicitudAnalitico(int id, String nombre, String apellido, String dni, java.util.Date fechaSolicitud) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.dni = dni;
            this.fechaSolicitud = fechaSolicitud;
        }

        public SolicitudAnalitico(int id, String nombre, String apellido, String dni, java.util.Date fechaSolicitud, String firma, String sello) {
            this(id, nombre, apellido, dni, fechaSolicitud);
            this.firma = firma;
            this.sello = sello;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getDni() { return dni; }
        public java.util.Date getFechaSolicitud() { return fechaSolicitud; }
        public String getFirma() { return firma; }
        public String getSello() { return sello; }
    }
}