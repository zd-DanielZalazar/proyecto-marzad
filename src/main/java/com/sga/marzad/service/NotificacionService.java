package com.sga.marzad.service;

import com.sga.marzad.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificacionService {

    private NotificacionService() {
    }

    public static void registrarCambioPassword(int usuarioId, String correoDestino) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String asunto = "Cambio de contraseña";
        String mensaje = "Se notificó el cambio de contraseña realizado el " + fecha +
                ". Si no fuiste vos, comunicate con mesa de ayuda.";
        registrar(usuarioId, asunto, mensaje);

        // Simulación de envío (puede reemplazarse por email real)
        System.out.println("Notificando al correo " + correoDestino + " sobre el cambio de contraseña.");
    }

    public static void registrar(int usuarioId, String asunto, String mensaje) {
        String sql = "INSERT INTO notificaciones_usuario (usuario_id, asunto, mensaje) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, asunto);
            ps.setString(3, mensaje);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
