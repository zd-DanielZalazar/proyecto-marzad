package com.sga.marzad.controller;

import com.sga.marzad.model.Alumno;
import com.sga.marzad.viewmodel.AlumnoViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.Optional;

public class AlumnosController {

    @FXML private TableView<Alumno> tableAlumnos;
    @FXML private TableColumn<Alumno, Integer> colId;
    @FXML private TableColumn<Alumno, Integer> colUsuario;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colApellido;
    @FXML private TableColumn<Alumno, String> colDni;
    @FXML private TableColumn<Alumno, String> colCorreo;
    @FXML private TableColumn<Alumno, LocalDate> colFecha;
    @FXML private TableColumn<Alumno, String> colGenero;
    @FXML private TableColumn<Alumno, Boolean> colHabilitado;

    @FXML private TextField txtUsuarioId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDni;
    @FXML private TextField txtCorreo;
    @FXML private DatePicker dpFechaNac;
    @FXML private TextField txtGenero;
    @FXML private CheckBox cbHabilitado;

    private final AlumnoViewModel vm = new AlumnoViewModel();

    @FXML
    public void initialize() {
        // Configurar columnas con PropertyValueFactory
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaNac"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));

        colHabilitado.setCellValueFactory(cell -> new SimpleBooleanProperty(cell.getValue().isHabilitado()));
        colHabilitado.setCellFactory(CheckBoxTableCell.forTableColumn(colHabilitado));

        tableAlumnos.setItems(vm.getAlumnos());

        // Listener de selección
        tableAlumnos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> showSelected(newSel)
        );
    }

    private void showSelected(Alumno a) {
        if (a == null) {
            clearForm();
        } else {
            txtUsuarioId.setText(String.valueOf(a.getUsuarioId()));
            txtNombre.setText(a.getNombre());
            txtApellido.setText(a.getApellido());
            txtDni.setText(a.getDni());
            txtCorreo.setText(a.getCorreo());
            dpFechaNac.setValue(a.getFechaNac());
            txtGenero.setText(a.getGenero());
            cbHabilitado.setSelected(a.isHabilitado());
        }
    }

    @FXML
    private void onNew() {
        tableAlumnos.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    private void onSave() {
        try {
            int usuarioId = Integer.parseInt(txtUsuarioId.getText());
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String dni = txtDni.getText().trim();
            String correo = txtCorreo.getText().trim();
            LocalDate fnac = dpFechaNac.getValue();
            String genero = txtGenero.getText().trim();
            boolean hab = cbHabilitado.isSelected();

            // Validación simple
            if (nombre.isBlank() || apellido.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Nombre y Apellido son obligatorios.");
                return;
            }

            Alumno sel = tableAlumnos.getSelectionModel().getSelectedItem();
            boolean ok;
            if (sel == null) {
                Alumno a = new Alumno(0, usuarioId, nombre, apellido, dni, correo, fnac, genero, hab);
                ok = vm.crearAlumno(a);
            } else {
                sel.setUsuarioId(usuarioId);
                sel.setNombre(nombre);
                sel.setApellido(apellido);
                sel.setDni(dni);
                sel.setCorreo(correo);
                sel.setFechaNac(fnac);
                sel.setGenero(genero);
                sel.setHabilitado(hab);
                ok = vm.actualizarAlumno(sel);
            }

            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Guardado exitoso.");
                tableAlumnos.getSelectionModel().clearSelection();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error al guardar.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Usuario ID debe ser numérico.");
        }
    }

    @FXML
    private void onDelete() {
        Alumno sel = tableAlumnos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar este alumno?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                boolean ok = vm.eliminarAlumno(sel);
                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION, "Eliminado exitosamente.");
                    tableAlumnos.getSelectionModel().clearSelection();
                    clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error al eliminar.");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Seleccione un alumno.");
        }
    }

    private void clearForm() {
        txtUsuarioId.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtDni.clear();
        txtCorreo.clear();
        dpFechaNac.setValue(null);
        txtGenero.clear();
        cbHabilitado.setSelected(false);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
