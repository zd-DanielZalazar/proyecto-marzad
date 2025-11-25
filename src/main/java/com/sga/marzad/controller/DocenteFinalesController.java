package com.sga.marzad.controller;

import com.sga.marzad.model.AlumnoFinalInscripto;
import com.sga.marzad.model.ExamenFinal;
import com.sga.marzad.model.Materia;
import com.sga.marzad.service.DocenteService;
import com.sga.marzad.utils.UsuarioSesion;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DocenteFinalesController {

    @FXML private ComboBox<Materia> comboMaterias;
    @FXML private TableView<ExamenFinal> tablaFinales;
    @FXML private TableColumn<ExamenFinal, String> colFecha;
    @FXML private TableColumn<ExamenFinal, String> colAula;
    @FXML private TableColumn<ExamenFinal, Number> colCupo;
    @FXML private TableColumn<ExamenFinal, Number> colInscriptos;

    @FXML private TableView<AlumnoFinalInscripto> tablaInscriptos;
    @FXML private TableColumn<AlumnoFinalInscripto, String> colAlumno;
    @FXML private TableColumn<AlumnoFinalInscripto, String> colDni;
    @FXML private TableColumn<AlumnoFinalInscripto, String> colEstado;

    private final DocenteService service = new DocenteService();
    private int docenteId = 0;

    @FXML
    public void initialize() {
        if (UsuarioSesion.getDocenteId() != null) {
            docenteId = UsuarioSesion.getDocenteId();
        }
        configurarTablas();
        cargarMaterias();
        comboMaterias.setOnAction(e -> cargarFinales());
        tablaFinales.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> cargarInscriptos(newVal));
    }

    private void configurarTablas() {
        tablaFinales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaInscriptos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaFormateada()));
        colAula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAula()));
        colCupo.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCupo()));
        colInscriptos.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getInscriptos()));

        colAlumno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colDni.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDni()));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));
    }

    private void cargarMaterias() {
        var materias = service.obtenerMateriasPorDocente(docenteId);
        comboMaterias.setItems(FXCollections.observableArrayList(materias));
        comboMaterias.getSelectionModel().clearSelection();
    }

    private void cargarFinales() {
        Materia materia = comboMaterias.getValue();
        if (materia == null) {
            tablaFinales.getItems().clear();
            tablaInscriptos.getItems().clear();
            return;
        }
        tablaFinales.setItems(FXCollections.observableArrayList(
                service.obtenerFinalesPorMateria(materia.getId())
        ));
        tablaInscriptos.getItems().clear();
    }

    private void cargarInscriptos(ExamenFinal examenFinal) {
        if (examenFinal == null) {
            tablaInscriptos.getItems().clear();
            return;
        }
        tablaInscriptos.setItems(FXCollections.observableArrayList(
                service.obtenerAlumnosFinales(examenFinal.getId())
        ));
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
