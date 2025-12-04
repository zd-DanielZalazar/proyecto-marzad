package com.sga.marzad.controller;

import com.sga.marzad.dao.ExamenFinalDAO;
import com.sga.marzad.dao.InscripcionFinalDAO;
import com.sga.marzad.dao.MateriaDAO;
import com.sga.marzad.dao.CarreraDAO;
import com.sga.marzad.model.AlumnoFinalInscripto;
import com.sga.marzad.model.Carrera;
import com.sga.marzad.model.ExamenFinal;
import com.sga.marzad.model.Materia;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AdminFinalesController {

    @FXML private ComboBox<Carrera> comboCarrera;
    @FXML private ComboBox<Materia> comboMateria;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtAula;
    @FXML private TextField txtCupo;
    @FXML private TableView<ExamenFinal> tablaMesas;
    @FXML private TableColumn<ExamenFinal, String> colMateria;
    @FXML private TableColumn<ExamenFinal, String> colFecha;
    @FXML private TableColumn<ExamenFinal, String> colAula;
    @FXML private TableColumn<ExamenFinal, Number> colCupo;
    @FXML private TableColumn<ExamenFinal, Number> colInscriptos;

    @FXML private TableView<AlumnoFinalInscripto> tablaInscriptos;
    @FXML private TableColumn<AlumnoFinalInscripto, String> colAlumno;
    @FXML private TableColumn<AlumnoFinalInscripto, String> colDni;
    @FXML private TableColumn<AlumnoFinalInscripto, String> colEstado;
    @FXML private TableColumn<AlumnoFinalInscripto, Void> colAccionAlumno;

    private final MateriaDAO materiaDAO = new MateriaDAO();
    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final ExamenFinalDAO examenFinalDAO = new ExamenFinalDAO();
    private final InscripcionFinalDAO inscripcionFinalDAO = new InscripcionFinalDAO();
    private final DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        try {
            configurarCombos();
            configurarTablas();
            recargarMesas();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar la vista: " + e.getMessage());
        }
    }

    private void configurarCombos() {
        comboCarrera.setItems(FXCollections.observableArrayList(carreraDAO.obtenerCarrerasHabilitadas()));
        comboCarrera.setConverter(new StringConverter<>() {
            @Override public String toString(Carrera c) { return c == null ? "" : c.getNombre(); }
            @Override public Carrera fromString(String s) { return null; }
        });
        comboCarrera.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> cargarMateriasPorCarrera(newVal));

        cargarMateriasPorCarrera(null);
        comboMateria.setConverter(new StringConverter<>() {
            @Override public String toString(Materia m) { return m == null ? "" : m.getNombre(); }
            @Override public Materia fromString(String s) { return null; }
        });
    }

    private void cargarMateriasPorCarrera(Carrera carrera) {
        if (carrera == null) {
            comboMateria.setItems(FXCollections.observableArrayList(materiaDAO.obtenerTodasHabilitadas()));
        } else {
            comboMateria.setItems(FXCollections.observableArrayList(materiaDAO.obtenerMateriasPorCarrera(carrera.getId())));
        }
        comboMateria.getSelectionModel().clearSelection();
    }

    private void configurarTablas() {
        tablaMesas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colMateria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMateriaNombre()));
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaFormateada()));
        colAula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAula()));
        colCupo.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCupo()));
        colInscriptos.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getInscriptos()));
        tablaMesas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> mostrarEnFormulario(newVal));

        tablaInscriptos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colAlumno.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCompleto()));
        colDni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDni()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colAccionAlumno.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Anular");
            {
                btn.getStyleClass().add("btn-danger");
                btn.setOnAction(e -> {
                    AlumnoFinalInscripto afi = getTableView().getItems().get(getIndex());
                    ExamenFinal mesa = tablaMesas.getSelectionModel().getSelectedItem();
                    if (mesa != null && afi != null) {
                        if (inscripcionFinalDAO.eliminarInscripcionPorAlumno(mesa.getId(), afi.getAlumnoId())) {
                            recargarInscriptos(mesa);
                            recargarMesas();
                        } else {
                            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo anular la inscripcion.");
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void mostrarEnFormulario(ExamenFinal mesa) {
        if (mesa == null) {
            limpiarFormulario();
            tablaInscriptos.getItems().clear();
            return;
        }
        comboMateria.getItems().stream()
                .filter(m -> m.getId() == mesa.getMateriaId())
                .findFirst().ifPresent(m -> comboMateria.getSelectionModel().select(m));
        seleccionarCarreraDeMateria(mesa.getMateriaId());
        if (mesa.getFecha() != null) {
            dpFecha.setValue(mesa.getFecha().toLocalDate());
            txtHora.setText(mesa.getFecha().toLocalTime().format(horaFmt));
        } else {
            dpFecha.setValue(null);
            txtHora.clear();
        }
        txtAula.setText(mesa.getAula());
        txtCupo.setText(String.valueOf(mesa.getCupo()));
        recargarInscriptos(mesa);
    }

    private void recargarMesas() {
        tablaMesas.setItems(FXCollections.observableArrayList(examenFinalDAO.listarTodos()));
    }

    private void recargarInscriptos(ExamenFinal mesa) {
        if (mesa == null) {
            tablaInscriptos.getItems().clear();
            return;
        }
        tablaInscriptos.setItems(FXCollections.observableArrayList(
                inscripcionFinalDAO.listarAlumnosPorExamen(mesa.getId())
        ));
    }

    private void seleccionarCarreraDeMateria(int materiaId) {
        Integer carreraId = materiaDAO.obtenerCarreraIdPorMateria(materiaId);
        if (carreraId == null) return;
        comboCarrera.getItems().stream()
                .filter(c -> c.getId() == carreraId)
                .findFirst()
                .ifPresent(c -> {
                    comboCarrera.getSelectionModel().select(c);
                    cargarMateriasPorCarrera(c);
                    comboMateria.getItems().stream()
                            .filter(m -> m.getId() == materiaId)
                            .findFirst()
                            .ifPresent(m -> comboMateria.getSelectionModel().select(m));
                });
    }

    @FXML
    private void onNuevo() {
        tablaMesas.getSelectionModel().clearSelection();
        limpiarFormulario();
    }

    @FXML
    private void onGuardar() {
        Materia materia = comboMateria.getValue();
        LocalDate fecha = dpFecha.getValue();
        String horaTxt = txtHora.getText();
        String aula = txtAula.getText();
        int cupo;

        if (materia == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione una materia.");
            return;
        }
        if (fecha == null || horaTxt == null || horaTxt.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Complete fecha y hora.");
            return;
        }
        LocalTime hora;
        try {
            hora = LocalTime.parse(horaTxt.trim(), horaFmt);
        } catch (DateTimeParseException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Hora invalida (formato HH:mm).");
            return;
        }
        try {
            cupo = Integer.parseInt(txtCupo.getText().trim());
            if (cupo <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cupo debe ser un numero mayor a cero.");
            return;
        }
        ExamenFinal seleccionada = tablaMesas.getSelectionModel().getSelectedItem();
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
        boolean ok;
        if (seleccionada == null) {
            ok = examenFinalDAO.crearMesa(materia.getId(), fechaHora, aula, cupo);
        } else {
            ok = examenFinalDAO.actualizarMesa(seleccionada.getId(), fechaHora, aula, cupo);
            if (!ok && cupo < seleccionada.getInscriptos()) {
                mostrarAlerta(Alert.AlertType.ERROR, "No se puede reducir el cupo por debajo de inscriptos actuales.");
                return;
            }
        }
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Mesa guardada.");
            recargarMesas();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar la mesa.");
        }
    }

    @FXML
    private void onEliminar() {
        ExamenFinal mesa = tablaMesas.getSelectionModel().getSelectedItem();
        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione una mesa.");
            return;
        }
        if (mesa.getInscriptos() > 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se puede eliminar una mesa con inscriptos.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Eliminar la mesa seleccionada?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean ok = examenFinalDAO.eliminarMesa(mesa.getId());
                if (ok) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Mesa eliminada.");
                    recargarMesas();
                    limpiarFormulario();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar la mesa.");
                }
            }
        });
    }

    @FXML
    private void onInscribirAlumno() {
        ExamenFinal mesa = tablaMesas.getSelectionModel().getSelectedItem();
        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione una mesa.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Inscribir alumno");
        dialog.setHeaderText("Ingrese ID de alumno a inscribir");
        dialog.setContentText("Alumno ID:");
        dialog.showAndWait().ifPresent(txt -> {
            try {
                int alumnoId = Integer.parseInt(txt.trim());
                boolean ok = inscripcionFinalDAO.inscribirAdmin(alumnoId, mesa.getId());
                if (ok) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Alumno inscripto.");
                    recargarInscriptos(mesa);
                    recargarMesas();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se pudo inscribir. Verifique regularidad/cupo o inscripcion previa.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "ID de alumno invalido.");
            }
        });
    }

    private void limpiarFormulario() {
        comboMateria.getSelectionModel().clearSelection();
        dpFecha.setValue(null);
        txtHora.clear();
        txtAula.clear();
        txtCupo.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String msg) {
        Alert alert = new Alert(tipo, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
