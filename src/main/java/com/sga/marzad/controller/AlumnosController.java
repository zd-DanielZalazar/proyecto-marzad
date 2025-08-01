package com.sga.marzad.controller;

import com.sga.marzad.model.Alumno;
import com.sga.marzad.viewmodel.AlumnoViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

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
        // Configurar columnas
        colId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        colUsuario.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getUsuarioId()).asObject());
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        colApellido.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getApellido()));
        colDni.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDni()));
        colCorreo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCorreo()));
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFechaNac()));
        colGenero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGenero()));
        colHabilitado.setCellValueFactory(cell -> new SimpleBooleanProperty(cell.getValue().isHabilitado()));
        colHabilitado.setCellFactory(CheckBoxTableCell.forTableColumn(colHabilitado));

        // Poner datos en la tabla
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
            String nombre  = txtNombre.getText();
            String apellido= txtApellido.getText();
            String dni     = txtDni.getText();
            String correo  = txtCorreo.getText();
            LocalDate fnac = dpFechaNac.getValue();
            String genero  = txtGenero.getText();
            boolean hab    = cbHabilitado.isSelected();

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
                new Alert(Alert.AlertType.INFORMATION, "Guardado exitoso").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Error al guardar").showAndWait();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Usuario ID debe ser numérico").showAndWait();
        }
    }

    @FXML
    private void onDelete() {
        Alumno sel = tableAlumnos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            boolean ok = vm.eliminarAlumno(sel);
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Eliminado exitoso").showAndWait();
                clearForm();
            } else {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar").showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Seleccione un alumno").showAndWait();
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
}
