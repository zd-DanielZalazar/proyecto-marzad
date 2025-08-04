package com.sga.marzad.controller;

import com.sga.marzad.model.AlumnoNotasDocente;
import com.sga.marzad.model.Materia;
import com.sga.marzad.service.DocenteService;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

public class DocenteMateriasController {

    @FXML
    private ComboBox<Materia> comboMaterias;

    @FXML
    private TableView<AlumnoNotasDocente> tablaAlumnos;

    @FXML
    private TableColumn<AlumnoNotasDocente, String> colNombre;
    @FXML
    private TableColumn<AlumnoNotasDocente, String> colDni;
    @FXML
    private TableColumn<AlumnoNotasDocente, String> colCorreo;
    @FXML
    private TableColumn<AlumnoNotasDocente, String> colEstado;

    @FXML
    private TableColumn<AlumnoNotasDocente, Double> colParcial1;
    @FXML
    private TableColumn<AlumnoNotasDocente, Double> colRecup1;
    @FXML
    private TableColumn<AlumnoNotasDocente, Double> colParcial2;
    @FXML
    private TableColumn<AlumnoNotasDocente, Double> colRecup2;
    @FXML
    private TableColumn<AlumnoNotasDocente, Double> colFinal;

    @FXML
    private TableColumn<AlumnoNotasDocente, Void> colAcciones;

    private final DocenteService service = new DocenteService();

    private final int docenteId = 1; // Reemplaza por el ID real de sesión

    @FXML
    public void initialize() {
        cargarMateriasDocente();
        configurarTabla();
        comboMaterias.setOnAction(e -> cargarAlumnosInscriptos());
    }

    private void cargarMateriasDocente() {
        var materias = service.obtenerMateriasPorDocente(docenteId);
        comboMaterias.setItems(FXCollections.observableArrayList(materias));
        comboMaterias.getSelectionModel().clearSelection();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colDni.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDni()));
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreo()));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstadoInscripcion()));

        setEditableDoubleCol(colParcial1, AlumnoNotasDocente::parcial1Property, "Primer Parcial");
        setEditableDoubleCol(colRecup1, AlumnoNotasDocente::recup1Property, "Recup. 1");
        setEditableDoubleCol(colParcial2, AlumnoNotasDocente::parcial2Property, "Segundo Parcial");
        setEditableDoubleCol(colRecup2, AlumnoNotasDocente::recup2Property, "Recup. 2");
        setEditableDoubleCol(colFinal, AlumnoNotasDocente::finalProperty, "Final/Promo");

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnBorrar = new Button("Borrar Notas");

            {
                btnBorrar.setOnAction(event -> {
                    AlumnoNotasDocente alumno = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que desea borrar todas las notas de este alumno?", ButtonType.YES, ButtonType.NO);
                    alert.setHeaderText(null);
                    alert.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.YES) {
                            service.eliminarTodasLasNotas(alumno.getAlumnoId(), getMateriaSeleccionadaId());
                            cargarAlumnosInscriptos();
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnBorrar);
            }
        });

        tablaAlumnos.setEditable(true);
    }

    private void setEditableDoubleCol(
            TableColumn<AlumnoNotasDocente, Double> col,
            java.util.function.Function<AlumnoNotasDocente, DoubleProperty> propertyGetter,
            String tipoNota) {
        col.setCellValueFactory(cellData -> propertyGetter.apply(cellData.getValue()).asObject());

        // Edición inline con estilo
        col.setCellFactory(tc -> new TextFieldTableCell<AlumnoNotasDocente, Double>(new DoubleStringConverter()) {
            @Override
            public void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (!empty) {
                    setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-background-color: #225;");
                } else {
                    setStyle("");
                }
            }
        });

        col.setOnEditCommit(event -> {
            AlumnoNotasDocente alumno = event.getRowValue();
            Double nuevaNota = event.getNewValue();

            if (nuevaNota == null || nuevaNota < 0 || nuevaNota > 10) {
                mostrarAlerta("La nota debe ser entre 0 y 10.");
                tablaAlumnos.refresh();
                return;
            }

            service.guardarNota(alumno.getAlumnoId(), getMateriaSeleccionadaId(), tipoNota, nuevaNota);
            cargarAlumnosInscriptos();
        });
    }

    private void cargarAlumnosInscriptos() {
        Materia materia = comboMaterias.getValue();
        if (materia == null) {
            tablaAlumnos.setItems(FXCollections.observableArrayList());
            return;
        }
        ObservableList<AlumnoNotasDocente> lista = FXCollections.observableArrayList(
                service.obtenerAlumnosNotasPorMateria(materia.getId())
        );
        tablaAlumnos.setItems(lista);
    }

    private int getMateriaSeleccionadaId() {
        Materia m = comboMaterias.getValue();
        return m != null ? m.getId() : 0;
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
