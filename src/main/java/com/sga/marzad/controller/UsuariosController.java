package com.sga.marzad.controller;

import com.sga.marzad.dao.UsuarioDAO;
import com.sga.marzad.dao.AlumnoDAO;
import com.sga.marzad.dao.DocenteDAO;
import com.sga.marzad.model.Rol;
import com.sga.marzad.model.Usuario;
import com.sga.marzad.model.Alumno;
import com.sga.marzad.model.Docente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class UsuariosController implements Initializable {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colHabilitado;
    @FXML private TableColumn<Usuario, String> colCreado;
    @FXML private TableColumn<Usuario, String> colActualizado;

    @FXML private TextField txtId;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Rol> comboRol;
    @FXML private CheckBox chkHabilitado;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final DocenteDAO docenteDAO = new DocenteDAO();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabla();
        cargarRoles();
        recargarUsuarios(null);
        tableUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> mostrarEnFormulario(newSel));
        limpiarFormulario();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colHabilitado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().isHabilitado() ? "Si" : "No"));
        colCreado.setCellValueFactory(cell -> new SimpleStringProperty(formatearFecha(cell.getValue().getCreadoEn())));
        colActualizado.setCellValueFactory(cell -> new SimpleStringProperty(formatearFecha(cell.getValue().getActualizadoEn())));
        tableUsuarios.setItems(usuarios);
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(dtf) : "";
    }

    private void cargarRoles() {
        comboRol.setItems(FXCollections.observableArrayList(usuarioDAO.findRoles()));
    }

    private void recargarUsuarios(Integer seleccionarId) {
        usuarios.setAll(usuarioDAO.findAll());
        if (seleccionarId != null) {
            usuarios.stream()
                    .filter(u -> u.getId() == seleccionarId)
                    .findFirst()
                    .ifPresent(u -> tableUsuarios.getSelectionModel().select(u));
        }
    }

    private void mostrarEnFormulario(Usuario u) {
        if (u == null) {
            limpiarFormulario();
            return;
        }
        txtId.setText(String.valueOf(u.getId()));
        txtUsername.setText(u.getUsername());
        txtPassword.clear(); // por seguridad no mostramos password
        chkHabilitado.setSelected(u.isHabilitado());
        seleccionarRol(u.getRol());
    }

    private void seleccionarRol(String rolNombre) {
        comboRol.getItems().stream()
                .filter(r -> r.getNombre().equalsIgnoreCase(rolNombre))
                .findFirst()
                .ifPresentOrElse(
                        r -> comboRol.getSelectionModel().select(r),
                        () -> comboRol.getSelectionModel().clearSelection()
                );
    }

    private void limpiarFormulario() {
        txtId.clear();
        txtUsername.clear();
        txtPassword.clear();
        comboRol.getSelectionModel().clearSelection();
        chkHabilitado.setSelected(true);
    }

    @FXML
    private void onNuevo() {
        tableUsuarios.getSelectionModel().clearSelection();
        limpiarFormulario();
        txtUsername.requestFocus();
    }

    @FXML
    private void onGuardar() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        Rol rol = comboRol.getValue();
        boolean habilitado = chkHabilitado.isSelected();

        if (username.isEmpty() || rol == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Complete usuario y rol.");
            return;
        }

        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();
        boolean esNuevo = seleccionado == null;

        if (usuarioDAO.usernameExists(username, esNuevo ? null : seleccionado.getId())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ya existe un usuario con ese nombre.");
            return;
        }

        if (esNuevo) {
            if (password == null || password.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Ingrese una contrasenia para el nuevo usuario.");
                return;
            }
            Usuario nuevo = new Usuario(0, username, password, rol.getNombre(), habilitado, null, null);
            Usuario creado = usuarioDAO.insert(nuevo);
            if (creado != null) {
                asegurarPerfilBasico(creado);
                recargarUsuarios(creado.getId());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario creado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el usuario.");
            }
        } else {
            String passAGuardar = (password == null || password.isBlank()) ? seleccionado.getPassword() : password;
            seleccionado.setUsername(username);
            seleccionado.setPassword(passAGuardar);
            seleccionado.setRol(rol.getNombre());
            seleccionado.setHabilitado(habilitado);

            if (usuarioDAO.update(seleccionado)) {
                asegurarPerfilBasico(seleccionado);
                recargarUsuarios(seleccionado.getId());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario actualizado.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el usuario.");
            }
        }
    }

    @FXML
    private void onEliminar() {
        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un usuario a eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminacion");
        confirm.setHeaderText(null);
        confirm.setContentText("Se eliminara el usuario " + seleccionado.getUsername() + ". Continuar?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            if (usuarioDAO.delete(seleccionado.getId())) {
                recargarUsuarios(null);
                limpiarFormulario();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario eliminado.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar el usuario.");
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se puede eliminar: el usuario esta relacionado con otros datos.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Si el rol es ALUMNO o DOCENTE y no existe fila relacionada, crea un perfil basico
     * para que el login no falle por falta de registro asociado.
     */
    private void asegurarPerfilBasico(Usuario usuario) {
        String rolNombre = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        try {
            if ("ALUMNO".equals(rolNombre)) {
                if (AlumnoDAO.buscarPorUsuarioId(usuario.getId()) == null) {
                    Alumno nuevoAlumno = new Alumno(
                            0,
                            usuario.getId(),
                            usuario.getUsername(),
                            usuario.getUsername(),
                            null,
                            null,
                            null,
                            null,
                            usuario.isHabilitado()
                    );
                    alumnoDAO.insert(nuevoAlumno);
                }
            } else if ("DOCENTE".equals(rolNombre)) {
                if (DocenteDAO.buscarPorUsuarioId(usuario.getId()) == null) {
                    Docente nuevoDocente = new Docente(
                            0,
                            usuario.getId(),
                            usuario.getUsername(),
                            usuario.getUsername(),
                            "DOC" + usuario.getId(),
                            usuario.getUsername() + "@mail.local",
                            null,
                            usuario.isHabilitado()
                    );
                    docenteDAO.insert(nuevoDocente);
                }
            }
        } catch (Exception e) {
            // No interrumpir el flujo de UI si falla el relleno basico
        }
    }
}
