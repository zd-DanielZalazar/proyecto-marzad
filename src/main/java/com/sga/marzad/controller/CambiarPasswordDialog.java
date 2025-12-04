package com.sga.marzad.controller;

import com.sga.marzad.viewmodel.PerfilViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class CambiarPasswordDialog extends Dialog<Void> {

    public CambiarPasswordDialog(PerfilViewModel vm) {
        setTitle("Cambio de contrasena");
        setHeaderText("Ingrese su contrasena actual y la nueva contrasena");

        PasswordField passActual = new PasswordField();
        passActual.setPromptText("Contrasena actual");
        PasswordField passNueva = new PasswordField();
        passNueva.setPromptText("Nueva contrasena");
        PasswordField passConfirm = new PasswordField();
        passConfirm.setPromptText("Repetir contrasena");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Actual:"), 0, 0);
        grid.add(passActual, 1, 0);
        grid.add(new Label("Nueva:"), 0, 1);
        grid.add(passNueva, 1, 1);
        grid.add(new Label("Confirmar:"), 0, 2);
        grid.add(passConfirm, 1, 2);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final Button btnOk = (Button) getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String actual = passActual.getText();
            String nueva = passNueva.getText();
            String repetir = passConfirm.getText();
            if (actual.isEmpty() || nueva.isEmpty() || repetir.isEmpty()) {
                mostrarAlerta("Todos los campos son obligatorios.");
                event.consume();
                return;
            }
            if (!nueva.equals(repetir)) {
                mostrarAlerta("La nueva contrasena y su confirmacion no coinciden.");
                event.consume();
                return;
            }
            boolean exito = vm.cambiarPassword(actual, nueva);
            if (exito) {
                mostrarAlertaInfo("Contrasena cambiada con exito.");
                close();
            } else {
                mostrarAlerta("Contrasena actual incorrecta.");
                event.consume();
            }
        });
    }

    private void mostrarAlerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void mostrarAlertaInfo(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
