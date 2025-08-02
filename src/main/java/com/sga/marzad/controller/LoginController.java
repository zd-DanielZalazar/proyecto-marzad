package com.sga.marzad.controller;

import com.sga.marzad.Main;
import com.sga.marzad.model.Usuario;
import com.sga.marzad.viewmodel.LoginViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

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
            // Navega al dashboard principal
            Main.goToMainView(u.getUsername(), u.getRol());
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Credenciales inválidas").showAndWait();
        }
    }
}
