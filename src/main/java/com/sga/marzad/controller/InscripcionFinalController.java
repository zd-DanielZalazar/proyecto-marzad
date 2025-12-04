package com.sga.marzad.controller;

import com.sga.marzad.dao.ExamenFinalDAO;
import com.sga.marzad.dao.InscripcionFinalDAO;
import com.sga.marzad.model.ExamenFinal;
import com.sga.marzad.model.InscripcionFinal;
import com.sga.marzad.utils.UsuarioSesion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class InscripcionFinalController {

    @FXML private Label lblCarrera;

    @FXML private TableView<ExamenFinal> tablaFinales;
    @FXML private TableColumn<ExamenFinal, String> colMateria;
    @FXML private TableColumn<ExamenFinal, String> colFecha;
    @FXML private TableColumn<ExamenFinal, String> colAula;
    @FXML private TableColumn<ExamenFinal, Number> colCupos;
    @FXML private TableColumn<ExamenFinal, Void> colAccionFinal;

    @FXML private TableView<InscripcionFinal> tablaMisFinales;
    @FXML private TableColumn<InscripcionFinal, String> colMiMateria;
    @FXML private TableColumn<InscripcionFinal, String> colMiFechaExamen;
    @FXML private TableColumn<InscripcionFinal, String> colMiFechaInsc;
    @FXML private TableColumn<InscripcionFinal, String> colMiEstado;
    @FXML private TableColumn<InscripcionFinal, Void> colMiAccion;

    private final ExamenFinalDAO examenFinalDAO = new ExamenFinalDAO();
    private final InscripcionFinalDAO inscripcionFinalDAO = new InscripcionFinalDAO();

    @FXML
    public void initialize() {
        lblCarrera.setText(UsuarioSesion.getCarreraNombre() != null
                ? UsuarioSesion.getCarreraNombre()
                : "Carrera no asignada");
        configurarTablas();
        recargarDatos();
    }

    private void configurarTablas() {
        tablaFinales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaMisFinales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materiaNombre"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFormateada"));
        colAula.setCellValueFactory(new PropertyValueFactory<>("aula"));
        colCupos.setCellValueFactory(new PropertyValueFactory<>("cupoDisponible"));

        colAccionFinal.setCellFactory(crearBotonInscribirFactory());

        colMiMateria.setCellValueFactory(new PropertyValueFactory<>("materiaNombre"));
        colMiFechaExamen.setCellValueFactory(new PropertyValueFactory<>("fechaExamenFormateada"));
        colMiFechaInsc.setCellValueFactory(new PropertyValueFactory<>("fechaInscripcionFormateada"));
        colMiEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colMiAccion.setCellFactory(crearBotonCancelarFactory());
    }

    private Callback<TableColumn<ExamenFinal, Void>, TableCell<ExamenFinal, Void>> crearBotonInscribirFactory() {
        return column -> new TableCell<>() {
            private final Button button = new Button("Inscribirme");
            {
                button.getStyleClass().add("btn-primary");
                button.setOnAction(e -> {
                    ExamenFinal examen = getTableView().getItems().get(getIndex());
                    inscribirse(examen);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                ExamenFinal examen = getTableView().getItems().get(getIndex());
                button.setDisable(!examen.tieneCupo());
                setGraphic(button);
            }
        };
    }

    private Callback<TableColumn<InscripcionFinal, Void>, TableCell<InscripcionFinal, Void>> crearBotonCancelarFactory() {
        return column -> new TableCell<>() {
            private final Button button = new Button("Cancelar");
            {
                button.getStyleClass().add("btn-danger");
                button.setOnAction(e -> {
                    InscripcionFinal inscripcion = getTableView().getItems().get(getIndex());
                    cancelar(inscripcion);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                InscripcionFinal inscripcion = getTableView().getItems().get(getIndex());
                button.setDisable(!"ACTIVA".equalsIgnoreCase(inscripcion.getEstado()));
                setGraphic(button);
            }
        };
    }

    private void recargarDatos() {
        Integer alumnoId = UsuarioSesion.getAlumnoId();
        Integer carreraId = UsuarioSesion.getCarreraId();
        if (alumnoId == null || carreraId == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Falta informacion de la carrera activa.");
            return;
        }

        tablaFinales.getItems().setAll(examenFinalDAO.listarDisponibles(carreraId, alumnoId));
        tablaMisFinales.getItems().setAll(inscripcionFinalDAO.listarPorAlumno(alumnoId));
    }

    private void inscribirse(ExamenFinal examenFinal) {
        Integer alumnoId = UsuarioSesion.getAlumnoId();
        if (alumnoId == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se encontro el alumno en la sesion.");
            return;
        }
        if (inscripcionFinalDAO.existeInscripcionActiva(alumnoId, examenFinal.getId())) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Ya estas inscripto en este final.");
            return;
        }
        boolean ok = inscripcionFinalDAO.insertar(alumnoId, examenFinal.getId());
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Inscripcion a final realizada.");
            recargarDatos();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo inscribir en el final seleccionado.");
        }
    }

    private void cancelar(InscripcionFinal inscripcion) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar inscripcion");
        confirm.setHeaderText("Confirmas la baja del final?");
        confirm.setContentText(inscripcion.getMateriaNombre() + " - " + inscripcion.getFechaExamenFormateada());
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                boolean ok = inscripcionFinalDAO.cancelar(inscripcion.getId());
                if (ok) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Inscripcion cancelada.");
                    recargarDatos();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo cancelar la inscripcion.");
                }
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
