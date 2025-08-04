package com.sga.marzad.controller;

import com.sga.marzad.dao.DocenteDAO;
import com.sga.marzad.model.Docente;
import com.sga.marzad.model.NuevaCarreraWizardData;
import com.sga.marzad.model.NuevaCarreraWizardData.MateriaWizard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class AltaCarreraPaso2Controller {

    @FXML private Label lblCarreraInfo;
    @FXML private TextField txtNombreMateria;
    @FXML private ComboBox<Integer> comboAnio;
    @FXML private ComboBox<Docente> comboDocente;
    @FXML private ComboBox<String> comboDia;
    @FXML private ComboBox<String> comboHora;
    @FXML private TableView<MateriaWizard> tablaMaterias;
    @FXML private TableColumn<MateriaWizard, String> colNombre;
    @FXML private TableColumn<MateriaWizard, Integer> colAnio;
    @FXML private TableColumn<MateriaWizard, String> colDocente;
    @FXML private TableColumn<MateriaWizard, String> colDia;
    @FXML private TableColumn<MateriaWizard, String> colHora;
    @FXML private TableColumn<MateriaWizard, Void> colAcciones;

    private NuevaCarreraWizardData wizardData;
    private ObservableList<MateriaWizard> materiasObservable = FXCollections.observableArrayList();
    private List<Docente> docentesDisponibles;
    private Runnable onAnterior;
    private Runnable onSiguiente;

    public void setWizardData(NuevaCarreraWizardData data) {
        this.wizardData = data;
        lblCarreraInfo.setText("Carrera: " + data.getNombreCarrera() + " | Duración: " + data.getDuracionAnios() + " años");

        comboAnio.getItems().clear();
        for (int i = 1; i <= data.getDuracionAnios(); i++) {
            comboAnio.getItems().add(i);
        }

        materiasObservable.setAll(data.getMaterias());
        tablaMaterias.setItems(materiasObservable);
    }

    public void setOnAnterior(Runnable r) { this.onAnterior = r; }
    public void setOnSiguiente(Runnable r) { this.onSiguiente = r; }

    @FXML
    public void initialize() {
        // --- Cargar docentes desde la base ---
        docentesDisponibles = DocenteDAO.obtenerTodos();
        comboDocente.setItems(FXCollections.observableArrayList(docentesDisponibles));
        comboDocente.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Docente docente, boolean empty) {
                super.updateItem(docente, empty);
                setText((empty || docente == null) ? "" : docente.getNombre() + " " + docente.getApellido());
            }
        });
        comboDocente.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Docente docente, boolean empty) {
                super.updateItem(docente, empty);
                setText((empty || docente == null) ? "" : docente.getNombre() + " " + docente.getApellido());
            }
        });

        // --- Día y hora ---
        comboDia.setItems(FXCollections.observableArrayList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"));
        ObservableList<String> horas = FXCollections.observableArrayList();
        for (int h = 8; h <= 21; h++) {
            horas.add(String.format("%02d:00", h));
            horas.add(String.format("%02d:30", h));
        }
        horas.add("22:00");
        comboHora.setItems(horas);

        // --- Tabla materias ---
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colAnio.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getAnio()).asObject());
        colDocente.setCellValueFactory(data -> {
            int docenteId = data.getValue().getDocenteId();
            Docente docente = docentesDisponibles.stream()
                    .filter(d -> d.getId() == docenteId)
                    .findFirst()
                    .orElse(null);
            return new javafx.beans.property.SimpleStringProperty(docente != null ? docente.getNombre() + " " + docente.getApellido() : "");
        });
        colDia.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDia()));
        colHora.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getHora()));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setOnAction(e -> {
                    MateriaWizard mat = getTableView().getItems().get(getIndex());
                    materiasObservable.remove(mat);
                    wizardData.getMaterias().remove(mat);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        tablaMaterias.setItems(materiasObservable);
    }

    @FXML
    private void onAgregarMateria() {
        String nombre = txtNombreMateria.getText().trim();
        Integer anio = comboAnio.getValue();
        Docente docente = comboDocente.getValue();
        String dia = comboDia.getValue();
        String hora = comboHora.getValue();

        if (nombre.isEmpty() || anio == null || docente == null || dia == null || hora == null) {
            showAlert("Todos los campos son obligatorios.");
            return;
        }

        // Validar que no se repita nombre en el mismo año
        for (MateriaWizard mat : materiasObservable) {
            if (mat.getNombre().equalsIgnoreCase(nombre) && mat.getAnio() == anio) {
                showAlert("Ya existe una materia con ese nombre en el mismo año.");
                return;
            }
        }

        MateriaWizard nueva = new MateriaWizard();
        nueva.setNombre(nombre);
        nueva.setAnio(anio);
        nueva.setDocenteId(docente.getId()); // <<---- IMPORTANTE: guarda el ID
        nueva.setDia(dia);
        nueva.setHora(hora);

        materiasObservable.add(nueva);
        wizardData.getMaterias().add(nueva);

        onLimpiarCampos();
    }

    @FXML
    private void onLimpiarCampos() {
        txtNombreMateria.clear();
        comboAnio.getSelectionModel().clearSelection();
        comboDocente.getSelectionModel().clearSelection();
        comboDia.getSelectionModel().clearSelection();
        comboHora.getSelectionModel().clearSelection();
    }

    @FXML
    private void onAnterior() {
        if (onAnterior != null) onAnterior.run();
    }

    @FXML
    private void onSiguiente() {
        if (materiasObservable.isEmpty()) {
            showAlert("Debe agregar al menos una materia para continuar.");
            return;
        }
        if (onSiguiente != null) onSiguiente.run();
    }

    private void showAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.showAndWait();
}
}