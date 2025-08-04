package com.sga.marzad.controller;

import com.sga.marzad.dao.CarreraDAO;
import com.sga.marzad.dao.MateriaDAO;
import com.sga.marzad.model.Materia;
import com.sga.marzad.model.NuevaCarreraWizardData;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AltaCarreraPaso4Controller {

    private NuevaCarreraWizardData wizardData;
    private Runnable onGuardar, onAnterior, onCancelar;

    public void setWizardData(NuevaCarreraWizardData wizardData) {
        this.wizardData = wizardData;
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

            // 2. Guardar materias asociadas a la carrera
            MateriaDAO materiaDAO = new MateriaDAO();
            // Mapear MateriaWizard a su ID generado, para correlatividades
            Map<NuevaCarreraWizardData.MateriaWizard, Integer> materiaIdMap = new HashMap<>();

            for (NuevaCarreraWizardData.MateriaWizard mw : wizardData.getMaterias()) {
                Materia materia = convertirAWizardMateria(mw);
                int materiaId = materiaDAO.crearMateriaSimple(
                        materia.getNombre(),
                        carreraId,
                        materia.getAnio(),
                        materia.getCuatrimestre(),
                        materia.getCreditos()
                );
                if (materiaId == -1) {
                    showError("No se pudo guardar la materia: " + materia.getNombre());
                    return;
                }
                materia.setId(materiaId);
                mw.setId(materiaId); // Guarda el id en el wizard
                materiaIdMap.put(mw, materiaId);
            }

            // 3. Guardar correlatividades (IDs)
            for (NuevaCarreraWizardData.MateriaWizard mw : wizardData.getMaterias()) {
                List<Integer> correlativas = mw.getCorrelativas();
                if (correlativas != null && !correlativas.isEmpty()) {
                    Integer materiaId = materiaIdMap.get(mw);
                    for (Integer corrWizardId : correlativas) {
                        // Buscar el id real en la base para la correlativa
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

    // Utilidades de mensajes
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showSuccess(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
