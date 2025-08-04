package com.sga.marzad.controller;

import com.sga.marzad.dao.CarreraDAO;
import com.sga.marzad.dao.MateriaDAO;
import com.sga.marzad.model.Carrera;
import com.sga.marzad.model.Materia;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class CarrerasMateriasController {

    @FXML private ListView<Carrera> listCarreras;
    @FXML private TextField tfCodigo, tfNombre;
    @FXML private TextArea taDescripcion;
    @FXML private Label lblCarreraEstado;
    @FXML private Button btnGuardarCarrera, btnDeshabilitarCarrera, btnAgregarMateria;
    @FXML private TableView<Materia> tablaMaterias;
    @FXML private TableColumn<Materia, String> colNombre;
    @FXML private TableColumn<Materia, Integer> colAnio, colCuatrimestre, colCreditos;
    @FXML private TableColumn<Materia, String> colCorrelativas;
    @FXML private TableColumn<Materia, Void> colAcciones;

    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final MateriaDAO materiaDAO = new MateriaDAO();

    private ObservableList<Carrera> carrerasList = FXCollections.observableArrayList();
    private ObservableList<Materia> materiasList = FXCollections.observableArrayList();

    private Carrera carreraSeleccionada = null;

    @FXML
    public void initialize() {
        // Listar carreras habilitadas
        carrerasList.addAll(carreraDAO.obtenerCarrerasHabilitadas());
        listCarreras.setItems(carrerasList);

        listCarreras.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            carreraSeleccionada = newVal;
            if (newVal != null) {
                cargarDatosCarrera(newVal);
                cargarMateriasDeCarrera(newVal.getId());
            }
        });

        // Configurar tabla de materias editable
        tablaMaterias.setEditable(true);

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

        colCreditos.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCreditos()).asObject());
        colCreditos.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colCreditos.setOnEditCommit(event -> {
            Materia mat = event.getRowValue();
            mat.setCreditos(event.getNewValue());
            materiaDAO.actualizarMateria(mat);
        });

        // --- Correlativas: Selector desplegable ---
        colCorrelativas.setCellValueFactory(cellData -> {
            List<Materia> correlativas = cellData.getValue().getCorrelativas();
            String texto = (correlativas == null || correlativas.isEmpty())
                    ? ""
                    : correlativas.stream().map(Materia::getNombre).collect(Collectors.joining(", "));
            return new SimpleStringProperty(texto);
        });
        colCorrelativas.setCellFactory(col -> new TableCell<Materia, String>() {
            private final ComboBox<Materia> combo = new ComboBox<>();
            {
                combo.setPrefWidth(130);
                combo.setConverter(new StringConverter<Materia>() {
                    @Override
                    public String toString(Materia object) {
                        return object == null ? "" : object.getNombre();
                    }
                    @Override
                    public Materia fromString(String string) {
                        return null; // no usado
                    }
                });
                combo.setOnAction(event -> {
                    Materia materia = getTableView().getItems().get(getIndex());
                    Materia seleccionada = combo.getSelectionModel().getSelectedItem();
                    if (seleccionada != null && materia != null) {
                        // Evitar duplicados
                        if (materia.getCorrelativas() == null)
                            materia.setCorrelativas(FXCollections.observableArrayList());
                        if (!materia.getCorrelativas().contains(seleccionada)) {
                            materia.getCorrelativas().add(seleccionada);
                            // Aquí deberías llamar a un método para guardar la relación en la base:
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
                    // Todas las materias del mismo plan (excepto la actual)
                    List<Materia> posibles = materiasList.stream()
                            .filter(m -> m.getId() != materiaActual.getId())
                            .collect(Collectors.toList());
                    combo.setItems(FXCollections.observableArrayList(posibles));
                    setGraphic(combo);
                }
            }
        });

        // --- Acciones: eliminar materia ---
        colAcciones.setCellFactory(col -> new TableCell<Materia, Void>() {
            private final Button btnEliminar = new Button("Deshabilitar");
            {
                btnEliminar.setOnAction(event -> {
                    Materia mat = getTableView().getItems().get(getIndex());
                    if (mat != null) {
                        if (confirmar("¿Deshabilitar materia?", "La materia dejará de estar disponible.")) {
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

        // --- Guardar cambios en carrera ---
        btnGuardarCarrera.setOnAction(e -> {
            if (carreraSeleccionada == null) return;
            carreraSeleccionada.setNombre(tfNombre.getText());
            carreraSeleccionada.setDescripcion(taDescripcion.getText());
            boolean ok = carreraDAO.actualizarCarrera(carreraSeleccionada);
            mostrarAlerta(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                    ok ? "¡Guardado!" : "Error", ok ? "Datos actualizados" : "No se pudo actualizar");
        });

        btnDeshabilitarCarrera.setOnAction(e -> {
            if (carreraSeleccionada == null) return;
            if (confirmar("¿Deshabilitar carrera?", "La carrera dejará de estar disponible.")) {
                carreraDAO.deshabilitarCarrera(carreraSeleccionada.getId());
                carrerasList.remove(carreraSeleccionada);
                materiasList.clear();
                limpiarCamposCarrera();
            }
        });

        btnAgregarMateria.setOnAction(e -> {
            if (carreraSeleccionada == null) return;
            // Podés mostrar un pequeño dialog para capturar datos básicos
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Agregar Materia");
            dialog.setHeaderText("Ingrese el nombre de la nueva materia:");
            dialog.setContentText("Nombre:");
            dialog.showAndWait().ifPresent(nombre -> {
                if (!nombre.isBlank()) {
                    Materia nueva = materiaDAO.crearMateria(nombre, carreraSeleccionada.getId());
                    if (nueva != null) {
                        materiasList.add(nueva);
                    }
                }
            });
        });
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
        // Para cada materia, podrías cargar sus correlativas si querés mostrarlas en la tabla
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
