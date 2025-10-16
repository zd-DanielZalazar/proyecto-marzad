package com.sga.marzad.controller;

import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.dao.InscripcionMateriaDAO;
import com.sga.marzad.utils.UsuarioSesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class InscripcionMateriaController {

    @FXML private ComboBox<MateriaDisponible> comboMaterias;
    @FXML private Button btnInscribir;
    @FXML private Label lblEstado;
    @FXML private Label lblCarrera;
    @FXML private TableView<InscripcionMateria> tablaInscripciones;
    @FXML private TableColumn<InscripcionMateria, String> colMateria;
    @FXML private TableColumn<InscripcionMateria, String> colFecha;
    @FXML private TableColumn<InscripcionMateria, String> colEstado;

    private final InscripcionMateriaDAO inscDAO = new InscripcionMateriaDAO();

    @FXML
    public void initialize() {
        // Configuración de tabla
        colMateria.setCellValueFactory(new PropertyValueFactory<>("nombreMateria"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFormateada"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Cargar carrera activa
        lblCarrera.setText(UsuarioSesion.getCarreraNombre() != null ? UsuarioSesion.getCarreraNombre() : "Sin carrera activa");
    }

    private void cargarMateriasDisponibles() {
        int alumnoId = UsuarioSesion.getAlumnoId();
        int carreraId = UsuarioSesion.getCarreraId();

        List<MateriaDisponible> materias = inscDAO.materiasDisponibles(alumnoId, carreraId);

        if (materias.isEmpty()) {
            mostrarAlerta("No se encontraron materias disponibles para inscribirse.", Alert.AlertType.INFORMATION);
            comboMaterias.getItems().clear();
            btnInscribir.setDisable(true);
        } else {
            comboMaterias.getItems().setAll(materias);
        }
    }


    private void mostrarEstadoMateria() {
        MateriaDisponible seleccionada = comboMaterias.getValue();
        if (seleccionada == null) return;

        lblEstado.setText("Estado: " + seleccionada.getEstado());

        btnInscribir.setDisable(!"DISPONIBLE".equals(seleccionada.getEstado()));
    }

    @FXML
    private void inscribirseEnMateria() {
        MateriaDisponible seleccionada = comboMaterias.getValue();
        if (seleccionada == null) return;

        Integer alumnoId = UsuarioSesion.getAlumnoId();
        Integer inscripcionCarreraId = UsuarioSesion.getInscripcionCarreraId();

        if (alumnoId == null || inscripcionCarreraId == null) {
            mostrarAlerta("No hay una inscripción activa a carrera para este alumno.", Alert.AlertType.ERROR);
            return;
        }

        boolean ok = inscDAO.insertar(new InscripcionMateria(
                0,
                alumnoId,
                seleccionada.getId(),
                inscripcionCarreraId,
                null,
                "ACTIVA"
        ));

        if (ok) {
            mostrarAlerta("Inscripción realizada con éxito.", Alert.AlertType.INFORMATION);
            cargarMateriasDisponibles(); // refrescar combo
            cargarInscripcionesAlumno(); // refrescar tabla
        } else {
            mostrarAlerta("Error al inscribirse.", Alert.AlertType.ERROR);
        }
    }

    private void cargarInscripcionesAlumno() {
        Integer alumnoId = UsuarioSesion.getAlumnoId();
        if (alumnoId == null) return;

        List<InscripcionMateria> inscripciones = inscDAO.listarPorAlumno(alumnoId);
        tablaInscripciones.getItems().setAll(inscripciones);
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Inscripción a materias");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void setDatosAlumno(Integer alumnoId, Integer carreraId, Integer inscripcionCarreraId, String carreraNombre) {
        // Guardar los valores en UsuarioSesion (por si no estaban cargados)
        UsuarioSesion.setAlumnoId(alumnoId);
        UsuarioSesion.setCarrera(carreraId, inscripcionCarreraId, carreraNombre);

        // Actualizar label de carrera
        lblCarrera.setText(carreraNombre != null ? carreraNombre : "Sin carrera activa");

        // Refrescar combo y tabla con los datos de este alumno
        cargarMateriasDisponibles();
        cargarInscripcionesAlumno();
    }

}
