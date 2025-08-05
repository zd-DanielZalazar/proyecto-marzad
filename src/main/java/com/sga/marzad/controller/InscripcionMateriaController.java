package com.sga.marzad.controller;

import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.viewmodel.InscripcionMateriaViewModel;
import com.sga.marzad.viewmodel.ResultadoInscripcion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class InscripcionMateriaController {

    @FXML private ComboBox<MateriaDisponible> comboMaterias;
    @FXML private Button btnInscribir;
    @FXML private Label lblEstado;
    @FXML private TableView<InscripcionMateria> tablaInscripciones;
    @FXML private Label lblCarrera;

    private final InscripcionMateriaViewModel viewModel = new InscripcionMateriaViewModel();

    private int alumnoId;
    private int carreraId;
    private int inscripcionCarreraId;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarComboBox();
        btnInscribir.setOnAction(event -> inscribirMateria());
    }

    // Llamado desde MainController para setear el contexto
    public void setDatosAlumno(int alumnoId, int carreraId, int inscripcionCarreraId, String nombreCarrera) {
        this.alumnoId = alumnoId;
        this.carreraId = carreraId;
        this.inscripcionCarreraId = inscripcionCarreraId;
        lblCarrera.setText(nombreCarrera);
        cargarMateriasDisponibles();
        cargarInscripcionesAlumno();
    }

    private void configurarTabla() {
        TableColumn<InscripcionMateria, String> colMateria = new TableColumn<>("Materia");
        colMateria.setCellValueFactory(data -> {
            int materiaId = data.getValue().getMateriaId();
            String nombreMateria = new com.sga.marzad.dao.InscripcionMateriaDAO().obtenerNombreMateriaPorId(materiaId);
            return new javafx.beans.property.SimpleStringProperty(nombreMateria);
        });

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
        try {
            List<MateriaDisponible> materias = viewModel.getMateriasDisponibles(alumnoId, carreraId);
            comboMaterias.setItems(FXCollections.observableArrayList(materias));
            comboMaterias.getSelectionModel().clearSelection();
        } catch (Exception e) {
            mostrarEstado("Error cargando materias: " + e.getMessage(), false);
        }
    }

    private void cargarInscripcionesAlumno() {
        try {
            // Aquí deberías tener un método en el DAO o Service que devuelva las inscripciones para el alumno
            // Si mantenés el modelo, podrías usar el viejo service, o crearlo en el ViewModel
            // Aquí uso el viejo InscripcionMateriaDAO por compatibilidad
            List<InscripcionMateria> inscripciones = new com.sga.marzad.dao.InscripcionMateriaDAO().listarPorAlumno(alumnoId);
            ObservableList<InscripcionMateria> data = FXCollections.observableArrayList(inscripciones);
            tablaInscripciones.setItems(data);
        } catch (Exception e) {
            mostrarEstado("Error cargando inscripciones: " + e.getMessage(), false);
        }
    }

    @FXML
    private void inscribirMateria() {
        MateriaDisponible seleccionada = comboMaterias.getValue();
        if (seleccionada == null) {
            mostrarEstado("Debe seleccionar una materia.", false);
            return;
        }
        ResultadoInscripcion resultado = viewModel.inscribir(alumnoId, seleccionada.getId(), inscripcionCarreraId);
        switch (resultado) {
            case OK -> {
                mostrarEstado("Inscripción exitosa.", true);
                cargarMateriasDisponibles();
                cargarInscripcionesAlumno();
            }
            case YA_INSCRIPTO -> mostrarEstado("El alumno ya está inscripto en esta materia.", false);
            case CORRELATIVA_NO_APROBADA -> mostrarEstado("No cumple con las correlativas requeridas.", false);
            case ERROR_BD -> mostrarEstado("Error al inscribir. Intente nuevamente.", false);
        }
    }

    private void mostrarEstado(String mensaje, boolean exito) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle(exito ? "-fx-text-fill: #33cc33;" : "-fx-text-fill: #ff6666;");
    }
}
