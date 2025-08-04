package com.sga.marzad.controller;

import com.sga.marzad.model.NuevaCarreraWizardData;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AltaCarreraPaso1Controller {
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private Spinner<Integer> spnDuracion;

    private NuevaCarreraWizardData wizardData;
    private Runnable onSiguiente;
    private Runnable onCancelar;

    public void setWizardData(NuevaCarreraWizardData data) {
        this.wizardData = data;
        if (data.getNombreCarrera() != null) txtNombre.setText(data.getNombreCarrera());
        if (data.getDescripcionCarrera() != null) txtDescripcion.setText(data.getDescripcionCarrera());
        if (data.getDuracionAnios() > 0) spnDuracion.getValueFactory().setValue(data.getDuracionAnios());
    }

    public void setOnSiguiente(Runnable r) { this.onSiguiente = r; }
    public void setOnCancelar(Runnable r) { this.onCancelar = r; }

    @FXML
    private void onSiguiente() {
        String nombre = txtNombre.getText().trim();
        String desc = txtDescripcion.getText().trim();
        int duracion = spnDuracion.getValue();
        if (nombre.isEmpty() || duracion < 1) {
            showAlert("Ingrese nombre y duración válida.");
            return;
        }
        wizardData.setNombreCarrera(nombre);
        wizardData.setDescripcionCarrera(desc);
        wizardData.setDuracionAnios(duracion);
        if (onSiguiente != null) onSiguiente.run();
    }
    @FXML
    private void onCancelar() {
        if (onCancelar != null) onCancelar.run();
    }
    private void showAlert(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}
