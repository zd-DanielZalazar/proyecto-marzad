package com.sga.marzad.controller;

import com.sga.marzad.model.NuevaCarreraWizardData;
import com.sga.marzad.model.NuevaCarreraWizardData.MateriaWizard;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

public class AltaCarreraPaso4Controller {
    @FXML private Label lblCarrera;
    @FXML private TableView<MateriaWizard> tablaMaterias;
    @FXML private TableColumn<MateriaWizard, String> colNombre;
    @FXML private TableColumn<MateriaWizard, Integer> colAnio;
    @FXML private TableColumn<MateriaWizard, String> colDocente;
    @FXML private TableColumn<MateriaWizard, String> colDia;
    @FXML private TableColumn<MateriaWizard, String> colHora;
    @FXML private TableColumn<MateriaWizard, String> colCorrelativas;

    private NuevaCarreraWizardData wizardData;
    private Runnable onAnterior, onGuardar, onCancelar;

    public void setWizardData(NuevaCarreraWizardData data) {
        this.wizardData = data;
        lblCarrera.setText("Carrera: " + data.getNombreCarrera() + " (" + data.getDuracionAnios() + " años)"
                + (data.getDescripcionCarrera() != null && !data.getDescripcionCarrera().isEmpty() ? "\n" + data.getDescripcionCarrera() : ""));
        tablaMaterias.setItems(FXCollections.observableArrayList(data.getMaterias()));
        colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().nombre));
        colAnio.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().anio).asObject());
        colDocente.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(getDocenteNombreById(cell.getValue().docenteId)));
        colDia.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().dia));
        colHora.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().hora));
        colCorrelativas.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                getCorrelativasNombres(cell.getValue(), data.getMaterias())));
    }
    public void setOnAnterior(Runnable r) { this.onAnterior = r; }
    public void setOnGuardar(Runnable r) { this.onGuardar = r; }
    public void setOnCancelar(Runnable r) { this.onCancelar = r; }
    @FXML private void onAnterior() { if (onAnterior != null) onAnterior.run(); }
    @FXML private void onGuardar() {
        // Aquí deberías implementar el guardado real en DB
        showSuccess("¡Carrera y materias guardadas correctamente!");
        if (onGuardar != null) onGuardar.run();
    }
    @FXML private void onCancelar() { if (onCancelar != null) onCancelar.run(); }

    private String getDocenteNombreById(int id) {
        return switch (id) { case 1 -> "Juan Perez"; case 2 -> "Maria Garcia"; case 3 -> "Pedro Diaz"; default -> ""; };
    }
    private String getCorrelativasNombres(MateriaWizard materia, List<MateriaWizard> todas) {
        if (materia.correlativas.isEmpty()) return "-";
        List<String> nombres = todas.stream().filter(m -> materia.correlativas.contains(m.hashCode())).map(m -> m.nombre).collect(Collectors.toList());
        return String.join(", ", nombres);
    }
    private void showSuccess(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
