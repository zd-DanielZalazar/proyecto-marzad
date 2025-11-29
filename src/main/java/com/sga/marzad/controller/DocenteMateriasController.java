package com.sga.marzad.controller;

import com.sga.marzad.model.AlumnoNotasDocente;
import com.sga.marzad.model.AsistenciaAlumnoRow;
import com.sga.marzad.model.Materia;
import com.sga.marzad.service.DocenteService;
import com.sga.marzad.utils.UsuarioSesion;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.geometry.Pos;
import javafx.util.StringConverter;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.time.format.DateTimeFormatter;
import com.sga.marzad.model.AsistenciaMatrizResult;
import com.sga.marzad.model.AsistenciaMatrizRow;
import java.util.Comparator;

import java.time.LocalDate;

public class DocenteMateriasController {

    @FXML private ComboBox<Materia> comboMaterias;

    @FXML private TableView<AlumnoNotasDocente> tablaAlumnos;
    @FXML private TableColumn<AlumnoNotasDocente, String> colNombre;
    @FXML private TableColumn<AlumnoNotasDocente, String> colDni;
    @FXML private TableColumn<AlumnoNotasDocente, String> colCorreo;
    @FXML private TableColumn<AlumnoNotasDocente, String> colEstado;
    @FXML private TableColumn<AlumnoNotasDocente, Double> colParcial1;
    @FXML private TableColumn<AlumnoNotasDocente, Double> colRecup1;
    @FXML private TableColumn<AlumnoNotasDocente, Double> colParcial2;
    @FXML private TableColumn<AlumnoNotasDocente, Double> colRecup2;
    @FXML private TableColumn<AlumnoNotasDocente, Double> colFinal;
    @FXML private TableColumn<AlumnoNotasDocente, Void> colAcciones;

    @FXML private Label lblAlumnoNombre;
    @FXML private Label lblAlumnoDni;
    @FXML private Label lblAlumnoEstado;

    @FXML private TableView<AsistenciaAlumnoRow> tablaAsistencias;
    @FXML private TableColumn<AsistenciaAlumnoRow, String> colAsistAlumno;
    @FXML private TableColumn<AsistenciaAlumnoRow, String> colAsistDni;
    @FXML private TableColumn<AsistenciaAlumnoRow, Boolean> colAsistPresente;
    @FXML private TableColumn<AsistenciaAlumnoRow, Boolean> colAsistAusente;
    @FXML private TableColumn<AsistenciaAlumnoRow, Number> colAsistPresentes;
    @FXML private TableColumn<AsistenciaAlumnoRow, Number> colAsistTotal;
    @FXML private DatePicker dateAsistencia;
    @FXML private CheckBox chkMarcarTodos;

    private final DocenteService service = new DocenteService();
    private int docenteId = 0;

    @FXML
    public void initialize() {
        if (UsuarioSesion.getDocenteId() != null) {
            docenteId = UsuarioSesion.getDocenteId();
        }
        configurarTablasNotas();
        configurarTablaAsistencias();
        cargarMateriasDocente();

        comboMaterias.setOnAction(e -> cargarDatosMateria());
        tablaAlumnos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> actualizarDetalleAlumno(newSel));
        dateAsistencia.setValue(LocalDate.now());
        dateAsistencia.valueProperty().addListener((obs, oldVal, newVal) -> cargarAsistenciasDiarias());
    }

    private void configurarTablasNotas() {
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colDni.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDni()));
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreo()));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstadoInscripcion()));

        setEditableDoubleCol(colParcial1, AlumnoNotasDocente::parcial1Property, "PARCIAL_1");
        setEditableDoubleCol(colRecup1, AlumnoNotasDocente::recup1Property, "RECUP_1");
        setEditableDoubleCol(colParcial2, AlumnoNotasDocente::parcial2Property, "PARCIAL_2");
        setEditableDoubleCol(colRecup2, AlumnoNotasDocente::recup2Property, "RECUP_2");
        setEditableDoubleCol(colFinal, AlumnoNotasDocente::finalProperty, "FINAL");

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnBorrar = new Button("Borrar notas");
            {
                btnBorrar.getStyleClass().add("btn-danger");
                btnBorrar.setOnAction(event -> {
                    AlumnoNotasDocente alumno = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Seguro que desea borrar todas las notas de este alumno?", ButtonType.YES, ButtonType.NO);
                    alert.setHeaderText(null);
                    alert.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.YES) {
                            service.eliminarTodasLasNotas(alumno.getInscripcionId(), docenteId);
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
        tablaAlumnos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarTablaAsistencias() {
        colAsistAlumno.setCellValueFactory(data -> data.getValue().nombreCompletoProperty());
        colAsistDni.setCellValueFactory(data -> data.getValue().dniProperty());
        colAsistPresente.setCellValueFactory(data -> data.getValue().presenteProperty());
        colAsistPresente.setCellFactory(col -> new TableCell<>() {
            private final CheckBox check = new CheckBox();
            {
                check.setOnAction(evt -> {
                    AsistenciaAlumnoRow row = getTableView().getItems().get(getIndex());
                    row.setPresente(check.isSelected());
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    check.setSelected(getTableView().getItems().get(getIndex()).isPresente());
                    setGraphic(check);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colAsistAusente.setCellValueFactory(data -> data.getValue().ausenteProperty());
        colAsistAusente.setCellFactory(col -> new TableCell<>() {
            private final CheckBox check = new CheckBox();
            {
                check.setOnAction(evt -> {
                    AsistenciaAlumnoRow row = getTableView().getItems().get(getIndex());
                    row.setAusente(check.isSelected());
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    check.setSelected(getTableView().getItems().get(getIndex()).isAusente());
                    setGraphic(check);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colAsistPresentes.setCellValueFactory(data -> data.getValue().totalPresentesProperty());
        colAsistTotal.setCellValueFactory(data -> data.getValue().totalClasesProperty());
        tablaAsistencias.setEditable(true);
        tablaAsistencias.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        chkMarcarTodos.setSelected(false);
    }

    private void cargarMateriasDocente() {
        var materias = service.obtenerMateriasPorDocente(docenteId);
        comboMaterias.setItems(FXCollections.observableArrayList(materias));
        comboMaterias.getSelectionModel().clearSelection();
        tablaAlumnos.getItems().clear();
        tablaAsistencias.getItems().clear();
    }

    private void cargarDatosMateria() {
        cargarAlumnosInscriptos();
        dateAsistencia.setValue(LocalDate.now());
        cargarAsistenciasDiarias();
    }

    private void setEditableDoubleCol(
            TableColumn<AlumnoNotasDocente, Double> col,
            java.util.function.Function<AlumnoNotasDocente, DoubleProperty> propertyGetter,
            String tipoNota) {
        col.setCellValueFactory(cellData -> propertyGetter.apply(cellData.getValue()).asObject());
        StringConverter<Double> intConverter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                if (value == null) return "";
                return String.valueOf(value.intValue());
            }
            @Override
            public Double fromString(String s) {
                if (s == null || s.isBlank()) return null;
                int val = Integer.parseInt(s.trim());
                return (double) val;
            }
        };
        col.setCellFactory(tc -> new TextFieldTableCell<>(intConverter));

        col.setOnEditCommit(event -> {
            AlumnoNotasDocente alumno = event.getRowValue();
            Double nuevaNota = event.getNewValue();

            if (nuevaNota == null || nuevaNota < 0 || nuevaNota > 10) {
                mostrarAlerta("La nota debe ser entre 0 y 10.");
                tablaAlumnos.refresh();
                return;
            }

            service.guardarNota(alumno.getInscripcionId(), docenteId, tipoNota, nuevaNota);
            cargarAlumnosInscriptos();
        });
    }

    private void cargarAlumnosInscriptos() {
        Materia materia = comboMaterias.getValue();
        if (materia == null) {
            tablaAlumnos.setItems(FXCollections.observableArrayList());
            lblAlumnoNombre.setText("-");
            lblAlumnoDni.setText("-");
            lblAlumnoEstado.setText("-");
            return;
        }
        ObservableList<AlumnoNotasDocente> lista = FXCollections.observableArrayList(
                service.obtenerAlumnosNotasPorMateria(docenteId, materia.getId())
        );
        tablaAlumnos.setItems(lista);
        var seleccion = tablaAlumnos.getSelectionModel().getSelectedItem();
        if (seleccion != null && lista.contains(seleccion)) {
            tablaAlumnos.getSelectionModel().select(seleccion);
            actualizarDetalleAlumno(seleccion);
        } else if (!lista.isEmpty()) {
            tablaAlumnos.getSelectionModel().selectFirst();
            actualizarDetalleAlumno(lista.get(0));
        } else {
            actualizarDetalleAlumno(null);
        }
    }

    private void actualizarDetalleAlumno(AlumnoNotasDocente alumno) {
        if (alumno == null) {
            lblAlumnoNombre.setText("Sin selección");
            lblAlumnoDni.setText("-");
            lblAlumnoEstado.setText("-");
        } else {
            lblAlumnoNombre.setText(alumno.getNombreCompleto());
            lblAlumnoDni.setText(alumno.getDni());
            lblAlumnoEstado.setText(alumno.getEstadoInscripcion());
        }
    }

    private void cargarAsistenciasDiarias() {
        Materia materia = comboMaterias.getValue();
        LocalDate fecha = dateAsistencia.getValue();
        if (materia == null || fecha == null) {
            tablaAsistencias.getItems().clear();
            return;
        }
        tablaAsistencias.setItems(FXCollections.observableArrayList(
                service.obtenerAsistenciaDiaria(docenteId, materia.getId(), fecha)
        ));
        chkMarcarTodos.setSelected(false);
    }

    @FXML
    private void onVerCuadroAsistencias() {
        Materia materia = comboMaterias.getValue();
        if (materia == null) {
            mostrarAlerta("Seleccione una materia para ver el cuadro de asistencias.");
            return;
        }
        AsistenciaMatrizResult matriz = service.obtenerMatrizAsistencias(docenteId, materia.getId());
        if (matriz.getFechas().isEmpty()) {
            mostrarAlerta("No hay asistencias registradas para esta materia.");
            return;
        }
        mostrarVentanaMatriz(matriz);
    }

    private void mostrarVentanaMatriz(AsistenciaMatrizResult matriz) {
        TableView<AsistenciaMatrizRow> tabla = new TableView<>();
        TableColumn<AsistenciaMatrizRow, String> colNombre = new TableColumn<>("Alumno");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        TableColumn<AsistenciaMatrizRow, String> colDni = new TableColumn<>("DNI");
        colDni.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDni()));
        tabla.getColumns().addAll(colNombre, colDni);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        matriz.getFechas().stream()
                .sorted(Comparator.naturalOrder())
                .forEach(fecha -> {
                    TableColumn<AsistenciaMatrizRow, String> col = new TableColumn<>(fmt.format(fecha));
                    col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValor(fecha)));
                    col.setPrefWidth(70);
                    tabla.getColumns().add(col);
                });

        TableColumn<AsistenciaMatrizRow, String> colPresentes = new TableColumn<>("Presentes");
        colPresentes.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPresentes())));
        TableColumn<AsistenciaMatrizRow, String> colClases = new TableColumn<>("Clases");
        colClases.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTotalClases())));
        tabla.getColumns().addAll(colPresentes, colClases);

        tabla.setItems(FXCollections.observableArrayList(matriz.getFilas()));
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        Stage stage = new Stage();
        stage.setTitle("Cuadro de asistencias");
        stage.initModality(Modality.NONE);
        stage.setScene(new Scene(tabla, 800, 400));
        stage.show();
    }

    @FXML
    private void onGuardarAsistencia() {
        LocalDate fecha = dateAsistencia.getValue();
        if (fecha == null) {
            mostrarAlerta("Seleccione la fecha de asistencia.");
            return;
        }
        for (AsistenciaAlumnoRow fila : tablaAsistencias.getItems()) {
            service.guardarAsistencia(fila.getInscripcionId(), docenteId, fecha, fila.isPresente());
        }
        Alert ok = new Alert(Alert.AlertType.INFORMATION, "Asistencia guardada correctamente.");
        ok.setHeaderText(null);
        ok.showAndWait();
        cargarAsistenciasDiarias();
    }

    @FXML
    private void onLimpiarAsistencia() {
        tablaAsistencias.getItems().forEach(row -> {
            row.setPresente(false);
            row.setAusente(false);
        });
        chkMarcarTodos.setSelected(false);
    }

    @FXML
    private void onMarcarTodos() {
        boolean marcado = chkMarcarTodos.isSelected();
        tablaAsistencias.getItems().forEach(row -> {
            row.setPresente(marcado);
            row.setAusente(false);
        });
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
