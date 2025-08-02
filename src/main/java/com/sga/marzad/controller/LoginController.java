package com.sga.marzad.controller;

import com.sga.marzad.Main;
import com.sga.marzad.model.Usuario;
import com.sga.marzad.viewmodel.LoginViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;


public class LoginController {
    @FXML private TextField txtUser;
    @FXML private TextField txtPass;

    private final LoginViewModel vm = new LoginViewModel();

    @FXML
    private void onLogin(ActionEvent e) {
        String user = txtUser.getText();
        String pass = txtPass.getText();
        Usuario u = vm.autenticar(user, pass);

        if (u != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
                Parent root = loader.load();

                MainController mainController = loader.getController();
                mainController.cargarDatosUsuario(u.getUsername(), u.getRol());

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Sistema de Gestión Académica");
                stage.show();

                // Cerrar ventana login actual
                Stage currentStage = (Stage) txtUser.getScene().getWindow();
                currentStage.close();

            } catch (IOException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error al abrir la vista principal.").showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Credenciales inválidas").showAndWait();
        }
    }

    @FXML
    private void onRegister() {
        // Abrir modal de registro
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegistroView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
