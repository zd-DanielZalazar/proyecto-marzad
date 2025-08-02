package com.sga.marzad.controller;

import com.sga.marzad.utils.ConexionBD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegistroController implements Initializable {

    @FXML private StackPane contenedorRegistro;

    @FXML private VBox vboxTipoUsuario, vboxAlumno, vboxDocente;

    // Alumno
    @FXML private TextField txtNombreAlumno, txtApellidoAlumno, txtDniAlumno, txtCorreoAlumno;
    @FXML private DatePicker dpFechaNacAlumno;
    @FXML private ComboBox<String> comboGeneroAlumno;

    // Docente
    @FXML private TextField txtNombreDocente, txtApellidoDocente, txtDniDocente, txtCorreoDocente;
    @FXML private ComboBox<String> comboGeneroDocente;
    @FXML private DatePicker dpFechaNacDocente; // NUEVO

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboGeneroAlumno.setItems(FXCollections.observableArrayList("F", "M", "Otro"));
        comboGeneroDocente.setItems(FXCollections.observableArrayList("F", "M", "Otro"));
    }

    @FXML
    private void mostrarFormularioAlumno() {
        vboxTipoUsuario.setVisible(false);
        vboxAlumno.setVisible(true);
    }

    @FXML
    private void mostrarFormularioDocente() {
        vboxTipoUsuario.setVisible(false);
        vboxDocente.setVisible(true);
    }

    @FXML
    private void volver() {
        vboxAlumno.setVisible(false);
        vboxDocente.setVisible(false);
        vboxTipoUsuario.setVisible(true);
    }

    @FXML
    private void registrarAlumno() {
        String nombre = txtNombreAlumno.getText().trim();
        String apellido = txtApellidoAlumno.getText().trim();
        String dni = txtDniAlumno.getText().trim();
        String correo = txtCorreoAlumno.getText().trim();
        LocalDate fechaNac = dpFechaNacAlumno.getValue();
        String genero = comboGeneroAlumno.getValue();

        if (!validarCampos(nombre, apellido, dni, correo, genero)) return;

        try (Connection conn = ConexionBD.getConnection()) {
            String username = generarUsername(conn, nombre, apellido);
            int usuarioId = insertarUsuario(conn, username, dni, "ALUMNO");

            if (usuarioId != -1) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO alumnos (usuario_id, nombre, apellido, dni, correo, fecha_nac, genero)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """);
                ps.setInt(1, usuarioId);
                ps.setString(2, nombre);
                ps.setString(3, apellido);
                ps.setString(4, dni);
                ps.setString(5, correo);
                ps.setDate(6, fechaNac != null ? Date.valueOf(fechaNac) : null);
                ps.setString(7, genero);
                ps.executeUpdate();

                mostrarAlerta("Registro exitoso", Alert.AlertType.INFORMATION);
                volver();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void registrarDocente() {
        String nombre = txtNombreDocente.getText().trim();
        String apellido = txtApellidoDocente.getText().trim();
        String dni = txtDniDocente.getText().trim();
        String correo = txtCorreoDocente.getText().trim();
        String genero = comboGeneroDocente.getValue();
        LocalDate fechaNac = dpFechaNacDocente.getValue();

        if (!validarCampos(nombre, apellido, dni, correo, genero)) return;

        try (Connection conn = ConexionBD.getConnection()) {
            String username = generarUsername(conn, nombre, apellido);
            int usuarioId = insertarUsuario(conn, username, dni, "DOCENTE");

            if (usuarioId != -1) {
                String legajo = generarLegajo(conn);

                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO docentes (usuario_id, nombre, apellido, legajo, correo, genero, dni, fecha_nacimiento)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """);
                ps.setInt(1, usuarioId);
                ps.setString(2, nombre);
                ps.setString(3, apellido);
                ps.setString(4, legajo);
                ps.setString(5, correo);
                ps.setString(6, genero);
                ps.setString(7, dni);
                ps.setDate(8, fechaNac != null ? Date.valueOf(fechaNac) : null);
                ps.executeUpdate();

                mostrarAlerta("Registro exitoso", Alert.AlertType.INFORMATION);
                volver();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validarCampos(String nombre, String apellido, String dni, String correo, String genero) {
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || correo.isEmpty() || genero == null) {
            mostrarAlerta("Por favor complete todos los campos obligatorios", Alert.AlertType.WARNING);
            return false;
        }
        if (!dni.matches("\\d+")) {
            mostrarAlerta("El DNI debe ser numérico", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private int insertarUsuario(Connection conn, String username, String password, String rol) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO usuarios (username, hash_password, rol)
            VALUES (?, ?, ?)
        """, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        ps.setString(2, password); // en texto plano
        ps.setString(3, rol);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    private String generarUsername(Connection conn, String nombre, String apellido) throws SQLException {
        String base = (nombre.substring(0, 1) + apellido.split(" ")[0]).toLowerCase();
        String username = base;
        int intento = 1;

        while (existeUsername(conn, username)) {
            if (nombre.length() > intento) {
                username = (nombre.substring(0, intento + 1) + apellido.split(" ")[0]).toLowerCase();
            } else {
                username = base + intento;
            }
            intento++;
        }
        return username;
    }

    private boolean existeUsername(Connection conn, String username) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    private String generarLegajo(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) + 1000 FROM docentes");
        rs.next();
        return "L" + rs.getInt(1);
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Registro");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
