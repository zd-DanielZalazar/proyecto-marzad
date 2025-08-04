package com.sga.marzad.controller;

import com.sga.marzad.model.NuevaCarreraWizardData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class AltaCarreraWizardController implements Initializable {

    @FXML
    private StackPane rootWizard;

    // Objeto para mantener todos los datos del wizard entre pasos
    private final NuevaCarreraWizardData wizardData = new NuevaCarreraWizardData();

    // Control de pasos
    private int pasoActual = 1;
    private static final int PASO_FINAL = 4;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Al abrir el wizard, muestra el primer paso
        mostrarPaso(1);
    }

    /**
     * Muestra el FXML correspondiente al paso actual.
     */
    public void mostrarPaso(int paso) {
        try {
            String fxml = switch (paso) {
                case 1 -> "/view/AltaCarreraPaso1.fxml";
                case 2 -> "/view/AltaCarreraPaso2.fxml";
                case 3 -> "/view/AltaCarreraPaso3.fxml";
                case 4 -> "/view/AltaCarreraPaso4.fxml";
                default -> throw new IllegalStateException("Paso inválido del wizard: " + paso);
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent pasoView = loader.load();

            // Inyecta el objeto wizardData en el controller de cada paso
            switch (paso) {
                case 1 -> {
                    AltaCarreraPaso1Controller c = loader.getController();
                    c.setWizardData(wizardData);
                    c.setOnSiguiente(() -> irAlPaso(2));
                    c.setOnCancelar(() -> cerrarWizard());
                }
                case 2 -> {
                    AltaCarreraPaso2Controller c = loader.getController();
                    c.setWizardData(wizardData);
                    c.setOnAnterior(() -> irAlPaso(1));
                    c.setOnSiguiente(() -> irAlPaso(3));
                }
                case 3 -> {
                    AltaCarreraPaso3Controller c = loader.getController();
                    c.setWizardData(wizardData);
                    c.setOnAnterior(() -> irAlPaso(2));
                    c.setOnSiguiente(() -> irAlPaso(4));
                }
                case 4 -> {
                    AltaCarreraPaso4Controller c = loader.getController();
                    c.setWizardData(wizardData);
                    c.setOnAnterior(() -> irAlPaso(3));
                    c.setOnGuardar(() -> cerrarWizard());
                    c.setOnCancelar(() -> cerrarWizard());
                }
            }

            rootWizard.getChildren().setAll(pasoView);
            pasoActual = paso;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void irAlPaso(int nuevoPaso) {
        mostrarPaso(nuevoPaso);
    }

    private void cerrarWizard() {
        // Cierra la ventana del wizard (Stage)
        rootWizard.getScene().getWindow().hide();
}
}