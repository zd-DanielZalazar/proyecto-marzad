package com.sga.marzad.controller;

import com.sga.marzad.model.NuevaCarreraWizardData;
import com.sga.marzad.model.NuevaCarreraWizardData.MateriaWizard;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AltaCarreraPaso3Controller {
    @FXML private ListView<MateriaWizard> listMaterias;
    @FXML private ListView<MateriaWizard> listCorrelativasPosibles;
    @FXML private ListView<MateriaWizard> listCorrelativasAsignadas;

    private NuevaCarreraWizardData wizardData;
    private List<MateriaWizard> materias;
    private MateriaWizard materiaSeleccionada;
    private Runnable onAnterior, onSiguiente;

    public void setWizardData(NuevaCarreraWizardData data) {
        this.wizardData = data;
        materias = data.getMaterias();
        listMaterias.setItems(FXCollections.observableArrayList(materias));
        listMaterias.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MateriaWizard item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getNombre() + " (" + item.getAnio() + "°)");
            }
        });
        listMaterias.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            materiaSeleccionada = newSel;
            actualizarCorrelativas();
        });
        if (!materias.isEmpty()) listMaterias.getSelectionModel().selectFirst();
    }
    public void setOnAnterior(Runnable r) { this.onAnterior = r; }
    public void setOnSiguiente(Runnable r) { this.onSiguiente = r; }

    private void actualizarCorrelativas() {
        if (materiaSeleccionada == null) return;
        List<MateriaWizard> posibles = materias.stream()
                .filter(m -> m.getAnio() < materiaSeleccionada.getAnio() && !m.equals(materiaSeleccionada))
                .collect(Collectors.toList());
        List<MateriaWizard> asignadas = new ArrayList<>();
        for (Integer corrId : materiaSeleccionada.getCorrelativas())
            materias.stream().filter(m -> m.hashCode() == corrId).findFirst().ifPresent(asignadas::add);
        List<MateriaWizard> noAsignadas = posibles.stream()
                .filter(m -> !materiaSeleccionada.getCorrelativas().contains(m.hashCode()))
                .collect(Collectors.toList());
        listCorrelativasPosibles.setItems(FXCollections.observableArrayList(noAsignadas));
        listCorrelativasAsignadas.setItems(FXCollections.observableArrayList(asignadas));
    }
    @FXML private void onAgregarCorrelativa() {
        MateriaWizard sel = listCorrelativasPosibles.getSelectionModel().getSelectedItem();
        if (sel != null && materiaSeleccionada != null && !materiaSeleccionada.getCorrelativas().contains(sel.hashCode())) {
            materiaSeleccionada.getCorrelativas().add(sel.hashCode());
            actualizarCorrelativas();
        }
    }
    @FXML private void onQuitarCorrelativa() {
        MateriaWizard sel = listCorrelativasAsignadas.getSelectionModel().getSelectedItem();
        if (sel != null && materiaSeleccionada != null) {
            materiaSeleccionada.getCorrelativas().remove(Integer.valueOf(sel.hashCode()));
            actualizarCorrelativas();
        }
    }
    @FXML private void onAnterior() { if (onAnterior != null) onAnterior.run(); }
    @FXML private void onSiguiente() { if (onSiguiente != null) onSiguiente.run(); }
}
