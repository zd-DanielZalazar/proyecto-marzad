package com.sga.marzad.controller;

import com.sga.marzad.viewmodel.PerfilViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class CambiarPasswordDialog extends Dialog<Void> {

    public CambiarPasswordDialog(PerfilViewModel vm) {
        setTitle("Cambio de contraseña");
        setHeaderText("Ingrese su contraseña actual y la nueva contraseña");

        // UI
        PasswordField passActual = new PasswordField();
        passActual.setPromptText("Contraseña actual");
        PasswordField passNueva = new PasswordField();
        passNueva.setPromptText("Nueva contraseña");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Actual:"), 0, 0);
        grid.add(passActual, 1, 0);
        grid.add(new Label("Nueva:"), 0, 1);
        grid.add(passNueva, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Lógica
        final Button btnOk = (Button) getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String actual = passActual.getText();
            String nueva = passNueva.getText();
            if (actual.isEmpty() || nueva.isEmpty()) {
                mostrarAlerta("Ambos campos son obligatorios.");
                event.consume();
                return;
            }
            boolean exito = vm.cambiarPassword(actual, nueva);
            if (exito) {
                mostrarAlertaInfo("Contraseña cambiada con éxito.");
                close();
            } else {
                mostrarAlerta("Contraseña actual incorrecta.");
                event.consume();
            }
        });
    }

    private void mostrarAlerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
    private void mostrarAlertaInfo(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
}
