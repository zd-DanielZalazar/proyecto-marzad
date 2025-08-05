package com.sga.marzad.controller;

import com.sga.marzad.dao.CarreraDAO;
import com.sga.marzad.dao.PlanEstudioDAO;
import com.sga.marzad.dao.MateriaDAO;
import com.sga.marzad.model.Materia;
import com.sga.marzad.model.NuevaCarreraWizardData;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AltaCarreraPaso4Controller {

    @FXML private Label lblCarrera;
    @FXML private TableView<NuevaCarreraWizardData.MateriaWizard> tablaMaterias;
    @FXML private TableColumn<NuevaCarreraWizardData.MateriaWizard, String> colNombre;
    @FXML private TableColumn<NuevaCarreraWizardData.MateriaWizard, Integer> colAnio;
    @FXML private TableColumn<NuevaCarreraWizardData.MateriaWizard, String> colDocente;
    @FXML private TableColumn<NuevaCarreraWizardData.MateriaWizard, String> colDia;
    @FXML private TableColumn<NuevaCarreraWizardData.MateriaWizard, String> colHora;
    @FXML private TableColumn<NuevaCarreraWizardData.MateriaWizard, String> colCorrelativas;

    private NuevaCarreraWizardData wizardData;
    private Runnable onGuardar, onAnterior, onCancelar;

    public void setWizardData(NuevaCarreraWizardData wizardData) {
        this.wizardData = wizardData;
        cargarResumen();
    }

    public void setOnGuardar(Runnable onGuardar) {
        this.onGuardar = onGuardar;
    }

    public void setOnAnterior(Runnable onAnterior) {
        this.onAnterior = onAnterior;
    }

    public void setOnCancelar(Runnable onCancelar) {
        this.onCancelar = onCancelar;
    }

    @FXML
    private void onAnterior() {
        if (onAnterior != null) onAnterior.run();
    }

    @FXML
    private void onGuardar() {
        try {
            // 1. Guardar la carrera
            CarreraDAO carreraDAO = new CarreraDAO();
            int carreraId = carreraDAO.crearCarrera(wizardData.getNombreCarrera(), wizardData.getDescripcionCarrera());
            if (carreraId == -1) {
                showError("No se pudo guardar la carrera.");
                return;
            }

            // 2. Guardar el plan de estudio y obtener planId
            PlanEstudioDAO planDAO = new PlanEstudioDAO();
            int planId = planDAO.crearPlanEstudio(carreraId, wizardData.getNombreCarrera());
            if (planId == -1) {
                showError("No se pudo guardar el plan de estudio.");
                return;
            }

            // 3. Guardar materias asociadas al plan
            MateriaDAO materiaDAO = new MateriaDAO();
            Map<NuevaCarreraWizardData.MateriaWizard, Integer> materiaIdMap = new HashMap<>();

            for (NuevaCarreraWizardData.MateriaWizard mw : wizardData.getMaterias()) {
                Materia materia = convertirAWizardMateria(mw);
                int materiaId = materiaDAO.crearMateriaSimple(
                        materia.getNombre(),
                        planId, // IMPORTANTE: ahora sí es el plan_id
                        materia.getAnio(),
                        materia.getCuatrimestre(),
                        materia.getCreditos()
                );
                if (materiaId == -1) {
                    showError("No se pudo guardar la materia: " + materia.getNombre());
                    return;
                }
                materia.setId(materiaId);
                mw.setId(materiaId);
                materiaIdMap.put(mw, materiaId);
            }

            // 4. Guardar correlatividades (IDs)
            for (NuevaCarreraWizardData.MateriaWizard mw : wizardData.getMaterias()) {
                List<Integer> correlativas = mw.getCorrelativas();
                if (correlativas != null && !correlativas.isEmpty()) {
                    Integer materiaId = materiaIdMap.get(mw);
                    for (Integer corrWizardId : correlativas) {
                        Integer corrMateriaId = null;
                        for (NuevaCarreraWizardData.MateriaWizard otra : wizardData.getMaterias()) {
                            if (otra.hashCode() == corrWizardId || otra.getId() == corrWizardId) {
                                corrMateriaId = otra.getId();
                                break;
                            }
                        }
                        if (materiaId != null && corrMateriaId != null) {
                            materiaDAO.agregarCorrelativa(materiaId, corrMateriaId);
                        }
                    }
                }
            }

            showSuccess("¡Carrera y materias guardadas correctamente!");
            if (onGuardar != null) onGuardar.run();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error inesperado al guardar: " + e.getMessage());
        }
    }

    // Conversor de MateriaWizard a Materia (solo para guardar)
    private Materia convertirAWizardMateria(NuevaCarreraWizardData.MateriaWizard mw) {
        Materia materia = new Materia();
        materia.setNombre(mw.getNombre());
        materia.setAnio(mw.getAnio());
        materia.setCuatrimestre(mw.getCuatrimestre());
        materia.setCreditos(mw.getCreditos());
        // Agrega más campos si es necesario
        return materia;
    }

    @FXML
    private void onCancelar() {
        if (onCancelar != null) onCancelar.run();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showSuccess(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    // --- NUEVO: cargar el resumen en los controles de la vista ---
    private void cargarResumen() {
        if (wizardData == null) return;

        // Mostrar datos principales de la carrera
        lblCarrera.setText(
                "Carrera: " + wizardData.getNombreCarrera() +
                        " | " + wizardData.getDescripcionCarrera() +
                        " (" + wizardData.getDuracionAnios() + " años)"
        );

        // Configurar columnas
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colAnio.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAnio()).asObject());
        colDocente.setCellValueFactory(data -> new SimpleStringProperty("Docente")); // Si necesitás el nombre real, buscá por ID
        colDia.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDia()));
        colHora.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHora()));
        colCorrelativas.setCellValueFactory(data -> new SimpleStringProperty(getNombresCorrelativas(data.getValue())));

        // Llenar la tabla
        tablaMaterias.setItems(FXCollections.observableArrayList(wizardData.getMaterias()));
    }

    // Helper para mostrar correlatividades como texto
    private String getNombresCorrelativas(NuevaCarreraWizardData.MateriaWizard mw) {
        List<String> nombres = new ArrayList<>();
        for (Integer corrId : mw.getCorrelativas()) {
            wizardData.getMaterias().stream()
                    .filter(m -> m.hashCode() == corrId)
                    .findFirst()
                    .ifPresent(corr -> nombres.add(corr.getNombre()));
        }
        return String.join(", ", nombres);
    }
}
