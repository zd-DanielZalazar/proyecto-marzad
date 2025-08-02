package com.sga.marzad.controller;

import com.sga.marzad.utils.ConexionBD;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SplashScreenController {
    @FXML private Label lblEstado;

    @FXML
    public void initialize() {
        // Ejecuta la conexión en otro hilo
        new Thread(() -> {
            boolean conectado = false;
            try {
                ConexionBD.getConnection();
                conectado = true;
            } catch (Exception e) { }
            boolean finalConectado = conectado;
            Platform.runLater(() -> {
                if (finalConectado) {
                    lblEstado.setText("¡Conexión a la BD exitosa!");
                } else {
                    lblEstado.setText("¡No se pudo conectar a la base de datos!");
                }
                // Espera 1 segundo y avanza
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> com.sga.marzad.Main.goToLogin());
                }).start();
            });
        }).start();
    }
}
