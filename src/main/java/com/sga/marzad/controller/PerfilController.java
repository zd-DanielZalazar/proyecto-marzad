package com.sga.marzad.controller;

import com.sga.marzad.model.Usuario;
import com.sga.marzad.viewmodel.PerfilViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PerfilController implements Initializable {

    @FXML private StackPane stackPanePerfil;
    @FXML private TextField txtNombre, txtApellido, txtCorreo, txtDni, txtLegajo;
    @FXML private DatePicker datePickerFechaNac;
    @FXML private ComboBox<String> comboGenero;
    @FXML private Button btnEditar, btnGuardar, btnLimpiar, btnCancelar, btnCambiarPass;
    @FXML private Label lblFechaNac, lblDni, lblLegajo;

    private PerfilViewModel vm;
    private boolean datosCargados = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboGenero.getItems().setAll("F", "M", "Otro");
        deshabilitarEdicion();
        // NO crees el ViewModel aquí, espera a setUsuarioActual
    }

    /**
     * Este método debe llamarse DESPUÉS de cargar el FXML y ANTES de mostrar la ventana.
     */
    public void setUsuarioActual(Usuario usuario) {
        this.vm = new PerfilViewModel(usuario);
        vm.cargarPerfilActual();
        cargarDatos();
        datosCargados = true;
    }

    private void cargarDatos() {
        if (vm == null) return; // No hacer nada si aún no está inicializado

        txtNombre.setText(vm.getNombre());
        txtApellido.setText(vm.getApellido());
        txtCorreo.setText(vm.getCorreo());
        comboGenero.setValue(vm.getGenero());

        // Mostrar campos según rol
        if (vm.isAlumno()) {
            lblFechaNac.setVisible(true);
            datePickerFechaNac.setVisible(true);
            txtDni.setVisible(true);
            lblDni.setVisible(true);

            txtDni.setText(vm.getDni());
            datePickerFechaNac.setValue(vm.getFechaNac());
            lblLegajo.setVisible(false);
            txtLegajo.setVisible(false);

        } else if (vm.isDocente()) {
            lblLegajo.setVisible(true);
            txtLegajo.setVisible(true);
            txtLegajo.setText(vm.getLegajo());
            lblFechaNac.setVisible(false);
            datePickerFechaNac.setVisible(false);
            txtDni.setVisible(false);
            lblDni.setVisible(false);
        }
    }

    private void habilitarEdicion() {
        txtNombre.setEditable(true);
        txtApellido.setEditable(true);
        txtCorreo.setEditable(true);
        comboGenero.setDisable(false);

        if (vm.isAlumno()) datePickerFechaNac.setEditable(true);

        btnEditar.setVisible(false);
        btnGuardar.setVisible(true);
        btnLimpiar.setVisible(true);
        btnCancelar.setVisible(true);
    }

    private void deshabilitarEdicion() {
        txtNombre.setEditable(false);
        txtApellido.setEditable(false);
        txtCorreo.setEditable(false);
        comboGenero.setDisable(true);

        if (vm != null && vm.isAlumno()) datePickerFechaNac.setEditable(false);

        btnEditar.setVisible(true);
        btnGuardar.setVisible(false);
        btnLimpiar.setVisible(false);
        btnCancelar.setVisible(false);
    }

    @FXML
    private void onEditar() {
        if (!datosCargados) return;
        habilitarEdicion();
    }

    @FXML
    private void onCancelar() {
        if (!datosCargados) return;
        cargarDatos();
        deshabilitarEdicion();
    }

    @FXML
    private void onLimpiar() {
        if (!datosCargados) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea descartar todos los cambios y recargar los datos originales?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            cargarDatos();
            deshabilitarEdicion();
        }
    }

    @FXML
    private void onGuardar() {
        if (!datosCargados) return;
        // Validación de campos obligatorios
        if (txtNombre.getText().isBlank()) {
            mostrarAlerta("El campo nombre es obligatorio."); return;
        }
        if (txtApellido.getText().isBlank()) {
            mostrarAlerta("El campo apellido es obligatorio."); return;
        }
        if (txtCorreo.getText().isBlank()) {
            mostrarAlerta("El campo correo es obligatorio."); return;
        }
        if (comboGenero.getValue() == null) {
            mostrarAlerta("Debe seleccionar un género."); return;
        }
        if (vm.isAlumno() && datePickerFechaNac.getValue() == null) {
            mostrarAlerta("Debe seleccionar fecha de nacimiento."); return;
        }
        // Validación email formato
        if (!txtCorreo.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            mostrarAlerta("Formato de correo no válido."); return;
        }
        // Guardar
        boolean exito = vm.guardarPerfil(
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                txtCorreo.getText().trim(),
                comboGenero.getValue(),
                vm.isAlumno() ? datePickerFechaNac.getValue() : null
        );
        if (exito) {
            mostrarAlertaInfo("Datos guardados exitosamente.");
            deshabilitarEdicion();
            vm.cargarPerfilActual(); // Recarga del modelo
            cargarDatos();           // Recarga de la UI
        } else {
            mostrarAlerta("No se pudieron guardar los datos. Es posible que el correo ya exista.");
        }
    }

    @FXML
    private void onCambiarPassword() {
        if (!datosCargados) return;
        CambiarPasswordDialog dialog = new CambiarPasswordDialog(vm);
        dialog.showAndWait();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    private void mostrarAlertaInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
