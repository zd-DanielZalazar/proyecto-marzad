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

    private InscripcionMateriaService service = new InscripcionMateriaService();

    // Debes cargar estos valores según el usuario logueado
    private int alumnoId = 1; // Ejemplo, reemplaza por el id real de la sesión
    private int carreraId = 1; // Ejemplo, reemplaza por la carrera del alumno
    private int inscripcionCarreraId = 1; // Ejemplo, busca el id real de la inscripcion_carrera aprobada

    @FXML
    public void initialize() {
        cargarMateriasDisponibles();
        cargarInscripcionesAlumno();
    }

    private void cargarMateriasDisponibles() {
        List<MateriaDisponible> materias = service.obtenerMateriasDisponibles(alumnoId, carreraId);
        comboMaterias.setItems(FXCollections.observableArrayList(materias));
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
            lblEstado.setText("Debe seleccionar una materia.");
            lblEstado.setStyle("-fx-text-fill: #ff6666;");
            return;
        }
        String mensaje = service.inscribirAlumnoAMateria(alumnoId, seleccionada.getId(), inscripcionCarreraId);
        lblEstado.setText(mensaje);
        lblEstado.setStyle(mensaje.contains("éxito") ? "-fx-text-fill: #33cc33;" : "-fx-text-fill: #ff6666;");
        cargarMateriasDisponibles();
        cargarInscripcionesAlumno();
    }
}
