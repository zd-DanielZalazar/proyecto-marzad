package com.sga.marzad.controller;

import com.sga.marzad.dao.CarreraDAO;
import com.sga.marzad.dao.DocenteDAO;
import com.sga.marzad.dao.MateriaDAO;
import com.sga.marzad.model.Carrera;
import com.sga.marzad.model.Docente;
import com.sga.marzad.model.Materia;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CarrerasMateriasController {

    @FXML private ListView<Carrera> listCarreras;
    @FXML private TextField tfCodigo, tfNombre;
    @FXML private TextArea taDescripcion;
    @FXML private Label lblCarreraEstado;
    @FXML private Button btnGuardarCarrera, btnDeshabilitarCarrera, btnAgregarMateria;
    @FXML private TableView<Materia> tablaMaterias;
    @FXML private TableColumn<Materia, String> colNombre;
    @FXML private TableColumn<Materia, Integer> colAnio, colCuatrimestre;
    @FXML private TableColumn<Materia, String> colCorrelativas, colDocente, colHorario;
    @FXML private TableColumn<Materia, Void> colAcciones;

    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final MateriaDAO materiaDAO = new MateriaDAO();

    private final ObservableList<Carrera> carrerasList = FXCollections.observableArrayList();
    private final ObservableList<Materia> materiasList = FXCollections.observableArrayList();
    private final ObservableList<Docente> docentesList = FXCollections.observableArrayList();

    private Carrera carreraSeleccionada = null;

    @FXML
    public void initialize() {
        carrerasList.addAll(carreraDAO.obtenerCarrerasHabilitadas());
        listCarreras.setItems(carrerasList);

        listCarreras.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            carreraSeleccionada = newVal;
            if (newVal != null) {
                cargarDatosCarrera(newVal);
                cargarMateriasDeCarrera(newVal.getId());
            } else {
                materiasList.clear();
                limpiarCamposCarrera();
            }
        });

        tablaMaterias.setEditable(true);
        docentesList.setAll(DocenteDAO.obtenerTodos());

        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        colNombre.setOnEditCommit(event -> {
            Materia mat = event.getRowValue();
            mat.setNombre(event.getNewValue());
            materiaDAO.actualizarMateria(mat);
        });

        colAnio.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getAnio()).asObject());
        colAnio.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colAnio.setOnEditCommit(event -> {
            Materia mat = event.getRowValue();
            mat.setAnio(event.getNewValue());
            materiaDAO.actualizarMateria(mat);
        });

        colCuatrimestre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCuatrimestre()).asObject());
        colCuatrimestre.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colCuatrimestre.setOnEditCommit(event -> {
            Materia mat = event.getRowValue();
            mat.setCuatrimestre(event.getNewValue());
            materiaDAO.actualizarMateria(mat);
        });

        colCorrelativas.setCellValueFactory(cellData -> {
            List<Materia> correlativas = cellData.getValue().getCorrelativas();
            String texto = (correlativas == null || correlativas.isEmpty())
                    ? ""
                    : correlativas.stream().map(Materia::getNombre).collect(Collectors.joining(", "));
            return new SimpleStringProperty(texto);
        });
        colCorrelativas.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Materia> combo = new ComboBox<>();
            {
                combo.setPrefWidth(130);
                combo.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(Materia object) {
                        return object == null ? "" : object.getNombre();
                    }
                    @Override
                    public Materia fromString(String string) {
                        return null;
                    }
                });
                combo.setOnAction(event -> {
                    Materia materia = getTableView().getItems().get(getIndex());
                    Materia seleccionada = combo.getSelectionModel().getSelectedItem();
                    if (seleccionada != null && materia != null) {
                        if (materia.getCorrelativas() == null) {
                            materia.setCorrelativas(FXCollections.observableArrayList());
                        }
                        boolean yaExiste = materia.getCorrelativas().stream().anyMatch(m -> m.getId() == seleccionada.getId());
                        if (!yaExiste) {
                            materia.getCorrelativas().add(seleccionada);
                            materiaDAO.agregarCorrelativa(materia.getId(), seleccionada.getId());
                            tablaMaterias.refresh();
                        }
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Materia materiaActual = (Materia) getTableRow().getItem();
                    List<Materia> posibles = materiasList.stream()
                            .filter(m -> m.getId() != materiaActual.getId())
                            .collect(Collectors.toList());
                    combo.setItems(FXCollections.observableArrayList(posibles));
                    setGraphic(combo);
                }
            }
        });

        colDocente.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDocenteNombre() != null ? cellData.getValue().getDocenteNombre() : ""));
        colDocente.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Docente> combo = new ComboBox<>();
            {
                combo.setItems(docentesList);
                combo.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(Docente object) {
                        return object == null ? "" : object.getNombre() + " " + object.getApellido();
                    }
                    @Override
                    public Docente fromString(String string) { return null; }
                });
                combo.setOnAction(e -> {
                    Materia materia = getTableView().getItems().get(getIndex());
                    Docente seleccionado = combo.getSelectionModel().getSelectedItem();
                    Integer docenteId = seleccionado != null ? seleccionado.getId() : null;
                    if (materiaDAO.asignarDocente(materia.getId(), docenteId)) {
                        materia.setDocenteId(docenteId);
                        materia.setDocenteNombre(seleccionado != null ? combo.getConverter().toString(seleccionado) : null);
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Materia materiaActual = (Materia) getTableRow().getItem();
                    if (materiaActual.getDocenteId() != null) {
                        docentesList.stream()
                                .filter(d -> d.getId() == materiaActual.getDocenteId())
                                .findFirst()
                                .ifPresent(d -> combo.getSelectionModel().select(d));
                    } else {
                        combo.getSelectionModel().clearSelection();
                    }
                    setGraphic(combo);
                }
            }
        });

        colHorario.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getHorario() != null ? cellData.getValue().getHorario() : ""));
        colHorario.setCellFactory(TextFieldTableCell.forTableColumn());
        colHorario.setOnEditCommit(event -> {
            Materia mat = event.getRowValue();
            mat.setHorario(event.getNewValue());
            materiaDAO.actualizarMateria(mat);
        });

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("Deshabilitar");
            {
                btnEliminar.setOnAction(event -> {
                    Materia mat = getTableView().getItems().get(getIndex());
                    if (mat != null) {
                        if (confirmar("Deshabilitar materia?", "La materia dejará de estar disponible.")) {
                            materiaDAO.deshabilitarMateria(mat.getId());
                            materiasList.remove(mat);
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        tablaMaterias.setItems(materiasList);

        btnGuardarCarrera.setOnAction(e -> {
            if (carreraSeleccionada == null) return;
            carreraSeleccionada.setNombre(tfNombre.getText());
            carreraSeleccionada.setDescripcion(taDescripcion.getText());
            boolean ok = carreraDAO.actualizarCarrera(carreraSeleccionada);
            mostrarAlerta(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                    ok ? "Guardado" : "Error", ok ? "Datos actualizados" : "No se pudo actualizar");
        });

        btnDeshabilitarCarrera.setOnAction(e -> {
            if (carreraSeleccionada == null) return;
            if (confirmar("Deshabilitar carrera?", "La carrera dejará de estar disponible.")) {
                carreraDAO.deshabilitarCarrera(carreraSeleccionada.getId());
                carrerasList.remove(carreraSeleccionada);
                materiasList.clear();
                limpiarCamposCarrera();
            }
        });

        btnAgregarMateria.setOnAction(e -> abrirDialogoNuevaMateria());
    }

    private void abrirDialogoNuevaMateria() {
        if (carreraSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona una carrera", "Debes elegir una carrera antes de agregar materias.");
            return;
        }
        Dialog<Materia> dialog = new Dialog<>();
        dialog.setTitle("Agregar materia");
        ButtonType guardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarType, ButtonType.CANCEL);

        TextField tfNombreMateria = new TextField();
        TextField tfAnio = new TextField();
        TextField tfCuatrimestre = new TextField();
        TextField tfHorario = new TextField();
        ComboBox<Docente> comboDocente = new ComboBox<>(docentesList);
        comboDocente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Docente object) {
                return object == null ? "" : object.getNombre() + " " + object.getApellido();
            }
            @Override
            public Docente fromString(String string) { return null; }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nombre:"), tfNombreMateria);
        grid.addRow(1, new Label("Año:"), tfAnio);
        grid.addRow(2, new Label("Cuatrimestre:"), tfCuatrimestre);
        grid.addRow(3, new Label("Docente:"), comboDocente);
        grid.addRow(4, new Label("Horario:"), tfHorario);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == guardarType) {
                String nombre = tfNombreMateria.getText().trim();
                String anioStr = tfAnio.getText().trim();
                String cuatriStr = tfCuatrimestre.getText().trim();
                if (nombre.isEmpty()) return null;
                int anio = parseEntero(anioStr, 1);
                int cuatri = parseEntero(cuatriStr, 1);
                Materia m = new Materia(0, 0, nombre, anio, cuatri, 0, true, null, null, tfHorario.getText().trim());
                Docente docSel = comboDocente.getSelectionModel().getSelectedItem();
                if (docSel != null) {
                    m.setDocenteId(docSel.getId());
                    m.setDocenteNombre(comboDocente.getConverter().toString(docSel));
                }
                return m;
            }
            return null;
        });

        Optional<Materia> result = dialog.showAndWait();
        result.ifPresent(m -> {
            Materia creada = materiaDAO.crearMateria(m.getNombre(), carreraSeleccionada.getId());
            if (creada != null) {
                creada.setAnio(m.getAnio());
                creada.setCuatrimestre(m.getCuatrimestre());
                creada.setHorario(m.getHorario());
                materiaDAO.actualizarMateria(creada);
                if (m.getDocenteId() != null) {
                    materiaDAO.asignarDocente(creada.getId(), m.getDocenteId());
                    creada.setDocenteId(m.getDocenteId());
                    creada.setDocenteNombre(m.getDocenteNombre());
                }
                materiasList.add(creada);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear", "Hubo un problema guardando la materia.");
            }
        });
    }

    private int parseEntero(String valor, int defaultValue) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void cargarDatosCarrera(Carrera carrera) {
        tfCodigo.setText(String.valueOf(carrera.getId()));
        tfNombre.setText(carrera.getNombre());
        taDescripcion.setText(carrera.getDescripcion());
        lblCarreraEstado.setText(carrera.isHabilitado() ? "(ACTIVA)" : "(INACTIVA)");
    }

    private void limpiarCamposCarrera() {
        tfCodigo.clear();
        tfNombre.clear();
        taDescripcion.clear();
        lblCarreraEstado.setText("");
    }

    private void cargarMateriasDeCarrera(int carreraId) {
        materiasList.clear();
        materiasList.addAll(materiaDAO.obtenerMateriasPorCarrera(carreraId));
        for (Materia m : materiasList) {
            List<Materia> correlativas = materiaDAO.obtenerCorrelativas(m.getId());
            m.setCorrelativas(correlativas);
        }
    }

    private boolean confirmar(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
