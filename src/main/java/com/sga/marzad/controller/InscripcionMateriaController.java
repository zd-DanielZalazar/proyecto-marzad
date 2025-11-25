package com.sga.marzad.controller;

import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.dao.InscripcionMateriaDAO;
import com.sga.marzad.utils.UsuarioSesion;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class InscripcionMateriaController {

    // 🔹 Referencias FXML
    @FXML private ComboBox<MateriaDisponible> comboMaterias;
    @FXML private Button btnInscribir;
    @FXML private Label lblEstado;
    @FXML private Label lblCarrera;
    @FXML private TableView<InscripcionMateria> tablaInscripciones;
    @FXML private TableColumn<InscripcionMateria, String> colMateria;
    @FXML private TableColumn<InscripcionMateria, String> colFecha;
    @FXML private TableColumn<InscripcionMateria, String> colEstado;
    @FXML private TableColumn<InscripcionMateria, Void> colAcciones; // Nueva columna de botones

    private final InscripcionMateriaDAO inscDAO = new InscripcionMateriaDAO();

    // ======================================================
    // 🔹 Inicialización general de la vista
    // ======================================================
    @FXML
    public void initialize() {
        btnInscribir.getStyleClass().add("btn-primary");

        colMateria.setCellValueFactory(new PropertyValueFactory<>("nombreMateria"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFormateada"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaInscripciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 🔹 Personalizar color de la columna Estado
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    if ("CANCELADA".equals(estado)) {
                        setStyle("-fx-text-fill: #AAAAAA; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // 🔹 Columna de botones "Desinscribir"
        configurarColumnaAcciones();

        lblCarrera.setText(
                UsuarioSesion.getCarreraNombre() != null
                        ? UsuarioSesion.getCarreraNombre()
                        : "Sin carrera activa"
        );
    }

    // ======================================================
// 🔹 Configura la columna "Acciones" con el botón Desinscribir
// ======================================================
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnDesinscribir = new Button("Desinscribir");

            {
                // Aplicar estilo del CSS global
                btnDesinscribir.getStyleClass().add("btn-danger");

                // Acción al hacer clic en el botón
                btnDesinscribir.setOnAction(e -> {
                    InscripcionMateria inscripcion = getTableView().getItems().get(getIndex());
                    desinscribirse(inscripcion);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    InscripcionMateria insc = getTableView().getItems().get(getIndex());

                    // 🔹 Si está CANCELADA → desactivar botón
                    if ("CANCELADA".equals(insc.getEstado())) {
                        btnDesinscribir.setDisable(true);
                        btnDesinscribir.setText("Cancelada");
                        btnDesinscribir.setStyle("-fx-background-color: #6b7280; -fx-text-fill: #f0f0f0; -fx-padding: 6 16;");
                    } else {
                        btnDesinscribir.setDisable(false);
                        btnDesinscribir.setText("Desinscribir");
                        btnDesinscribir.getStyleClass().setAll("btn-danger");
                    }

                    setGraphic(btnDesinscribir);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }


    // ======================================================
    // 🔹 Acción de desinscribirse de una materia
    // ======================================================
    private void desinscribirse(InscripcionMateria inscripcion) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar desinscripción");
        confirm.setHeaderText("¿Deseas desinscribirte de la materia?");
        confirm.setContentText(inscripcion.getNombreMateria());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean exito = inscDAO.eliminar(inscripcion.getId());
                if (exito) {
                    mostrarAlerta("Desinscripción exitosa.", Alert.AlertType.INFORMATION);
                    cargarInscripcionesAlumno(); // refrescar tabla
                    cargarMateriasDisponibles(); // refrescar combo
                } else {
                    mostrarAlerta("Error al desinscribirse.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ======================================================
    // 🔹 Cargar materias disponibles para inscribirse
    // ======================================================
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
            btnInscribir.setDisable(false);
        }
    }

    // ======================================================
    // 🔹 Mostrar estado de la materia seleccionada
    // ======================================================
    private void mostrarEstadoMateria() {
        MateriaDisponible seleccionada = comboMaterias.getValue();
        if (seleccionada == null) return;

        lblEstado.setText("Estado: " + seleccionada.getEstado());
        btnInscribir.setDisable(!"DISPONIBLE".equals(seleccionada.getEstado()));
    }

    // ======================================================
    // 🔹 Acción principal: inscribirse
    // ======================================================
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
            cargarMateriasDisponibles();
            cargarInscripcionesAlumno();
        } else {
            mostrarAlerta("Error al inscribirse.", Alert.AlertType.ERROR);
        }
    }

    // ======================================================
    // 🔹 Cargar inscripciones activas del alumno
    // ======================================================
    private void cargarInscripcionesAlumno() {
        Integer alumnoId = UsuarioSesion.getAlumnoId();
        if (alumnoId == null) return;

        List<InscripcionMateria> inscripciones = inscDAO.listarPorAlumno(alumnoId);
        tablaInscripciones.getItems().setAll(inscripciones);
    }

    // ======================================================
    // 🔹 Mostrar alertas informativas
    // ======================================================
    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Inscripción a Materias");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ======================================================
    // 🔹 Cargar datos del alumno actual
    // ======================================================
    public void setDatosAlumno(Integer alumnoId, Integer carreraId, Integer inscripcionCarreraId, String carreraNombre) {
        UsuarioSesion.setAlumnoId(alumnoId);
        UsuarioSesion.setCarrera(carreraId, inscripcionCarreraId, carreraNombre);

        lblCarrera.setText(carreraNombre != null ? carreraNombre : "Sin carrera activa");

        cargarMateriasDisponibles();
        cargarInscripcionesAlumno();
    }
}
