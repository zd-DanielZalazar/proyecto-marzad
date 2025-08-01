package com.sga.marzad.controller;

import com.sga.marzad.model.Usuario;
import com.sga.marzad.viewmodel.LoginViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField      txtUser;
    @FXML private PasswordField  txtPass;  // <— coincide con <PasswordField> en el FXML

    private final LoginViewModel vm = new LoginViewModel();

    @FXML
    private void onLogin(ActionEvent e) {
        String user = txtUser.getText();
        String pass = txtPass.getText();
        Usuario u = vm.autenticar(user, pass);

        if (u != null) {
            // Navegar a AlumnosView.fxml
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/AlumnosView.fxml")
                );
                Scene alumnosScene = new Scene(loader.load());

                // Obtener la Stage actual desde el botón
                Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
                stage.setScene(alumnosScene);
                stage.setTitle("SGA – Alumnos (“" + u.getUsername() + "”)");
                stage.show();

            } catch (IOException ex) {
                ex.printStackTrace();
                new Alert(
                        Alert.AlertType.ERROR,
                        "No se pudo cargar la vista de Alumnos"
                ).showAndWait();
            }

        } else {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Credenciales inválidas"
            ).showAndWait();
        }
    }
}
