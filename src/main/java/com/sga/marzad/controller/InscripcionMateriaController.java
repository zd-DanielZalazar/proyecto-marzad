package com.sga.marzad.controller;

import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.service.InscripcionMateriaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class InscripcionMateriaController {

    @FXML
    private ComboBox<MateriaDisponible> comboMaterias;

    @FXML
    private Button btnInscribir;

    @FXML
    private Label lblEstado;

    @FXML
    private TableView<InscripcionMateria> tablaInscripciones;

    @FXML
    private Label lblCarrera;

    private final InscripcionMateriaService service = new InscripcionMateriaService();

    // Cargar estos valores desde la sesión/logueo real
    private int alumnoId = 1; // Debe venir del usuario logueado
    private int carreraId = 1; // Debe venir del alumno logueado
    private int inscripcionCarreraId = 1; // El ID real de inscripcion_carrera

    @FXML
    public void initialize() {
        lblCarrera.setText("Analista en Sistemas"); // O el nombre dinámico que corresponda

        configurarTabla();
        configurarComboBox();
        cargarMateriasDisponibles();
        cargarInscripcionesAlumno();

        btnInscribir.setOnAction(event -> inscribirMateria());
    }

    private void configurarTabla() {
        TableColumn<InscripcionMateria, String> colMateria = new TableColumn<>("Materia");
        colMateria.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                service.obtenerNombreMateriaPorId(data.getValue().getMateriaId())
        ));

        TableColumn<InscripcionMateria, String> colFecha = new TableColumn<>("Fecha Inscripción");
        colFecha.setCellValueFactory(data -> {
            var fecha = data.getValue().getFechaInsc();
            return new javafx.beans.property.SimpleStringProperty(fecha != null ? fecha.toString() : "");
        });

        TableColumn<InscripcionMateria, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado()));

        tablaInscripciones.getColumns().setAll(colMateria, colFecha, colEstado);
    }

    private void configurarComboBox() {
        // Para mostrar nombre, año y cuatrimestre
        comboMaterias.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(MateriaDisponible item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " (" + item.getAnio() + "° año, " + item.getCuatrimestre() + "° C)");
                }
            }
        });
        comboMaterias.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MateriaDisponible item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " (" + item.getAnio() + "° año, " + item.getCuatrimestre() + "° C)");
                }
            }
        });
    }

    private void cargarMateriasDisponibles() {
        List<MateriaDisponible> materias = service.obtenerMateriasDisponibles(alumnoId, carreraId);
        comboMaterias.setItems(FXCollections.observableArrayList(materias));
        comboMaterias.getSelectionModel().clearSelection();
    }

    private void cargarInscripcionesAlumno() {
        List<InscripcionMateria> inscripciones = service.obtenerInscripcionesPorAlumno(alumnoId);
        ObservableList<InscripcionMateria> data = FXCollections.observableArrayList(inscripciones);
        tablaInscripciones.setItems(data);
    }

    @FXML
    private void inscribirMateria() {
        MateriaDisponible seleccionada = comboMaterias.getValue();
        if (seleccionada == null) {
            mostrarEstado("Debe seleccionar una materia.", false);
            return;
        }
        // Validación robusta: correlativas y si ya está inscripto
        if (!service.puedeInscribirse(alumnoId, seleccionada.getId())) {
            mostrarEstado("No cumple correlativas o ya está inscripto.", false);
            return;
        }
        String mensaje = service.inscribirAlumnoAMateria(alumnoId, seleccionada.getId(), inscripcionCarreraId);
        mostrarEstado(mensaje, mensaje.contains("éxito"));
        cargarMateriasDisponibles();
        cargarInscripcionesAlumno();
    }

    private void mostrarEstado(String mensaje, boolean exito) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle(exito ? "-fx-text-fill: #33cc33;" : "-fx-text-fill: #ff6666;");
    }
}
