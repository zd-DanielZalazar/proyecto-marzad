package com.sga.marzad.controller;

import com.sga.marzad.Main;
import com.sga.marzad.model.Alumno;
import com.sga.marzad.model.Usuario;
import com.sga.marzad.service.CertificadoRegularService;
import com.sga.marzad.utils.ConexionBD;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // ------------- FXML variables -------------
    @FXML private Label lblReloj;
    @FXML private Label lblUsuario;
    @FXML private Label lblSistema;
    @FXML private MenuBar menuBar;
    @FXML private ImageView logoImage;
    @FXML private Label lblVersion;
    @FXML private Label lblAutor;
    @FXML private Label lblBdStatus;

    // ------------- Sesión actual -------------
    private Usuario usuarioActual;

    // ------------- Reloj -------------
    private final Locale locale = new Locale("es", "AR");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy - HH:mm", locale);

    // ------------- Inicialización -------------
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        iniciarReloj();
        cargarMenu();
        // Usuario se setea desde LoginController después del login
    }

    // Se llama desde LoginController tras login exitoso
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getUsername() + " (" + usuario.getRol() + ")");

    }

    // ------------- Botón certificado -------------
    @FXML
    private void onCertificadoAlumnoRegularClick() {
        try {
            int usuarioId = usuarioActual.getId();
            Alumno alumno = buscarAlumnoPorUsuarioId(usuarioId);

            if (alumno == null) {
                mostrarAlerta("Error", "No se encontraron datos del alumno.", Alert.AlertType.ERROR);
                return;
            }

            String nombre = alumno.getNombre();
            String apellido = alumno.getApellido();
            String dni = alumno.getDni();

            CertificadoRegularService.generarCertificado(nombre, apellido, dni);
            mostrarAlerta("Certificado generado", "Se descargó el certificado en la carpeta Descargas.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el certificado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ------------- Buscar alumno en DB -------------
    private Alumno buscarAlumnoPorUsuarioId(int usuarioId) {
        Alumno alumno = null;
        try (Connection conn = ConexionBD.getConnection()) {
            String sql = "SELECT * FROM alumnos WHERE usuario_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                alumno = new Alumno(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alumno;
    }

    // ------------- Menú y utilidades varias -------------

    private void iniciarReloj() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> actualizarFechaHora()),
                new KeyFrame(Duration.minutes(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void actualizarFechaHora() {
        String fechaFormateada = LocalDateTime.now().format(formatter);
        lblReloj.setText(capitalize(fechaFormateada));
    }

    private String capitalize(String texto) {
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    private void cargarMenu() {
        Menu inscripciones = new Menu("Inscripciones");
        MenuItem materias = new MenuItem("Inscripción a Materias");
        MenuItem examenes = new MenuItem("Inscripción a Exámenes Finales");

        materias.setOnAction(e -> abrirVentanaModal("/view/InscripcionMateriaView.fxml", "Inscripción a Materias"));

        examenes.setOnAction(e -> mostrarAlertaInfo("Esta vista aún no está disponible."));

        inscripciones.getItems().addAll(materias, examenes);

        Menu tramites = new Menu("Trámites");
        MenuItem certificados = new MenuItem("Descarga Certificado Alumno Regular");
        MenuItem estadoAcademico = new MenuItem("Estado Académico");
        MenuItem estadoClases = new MenuItem("Estado de Clases");

        certificados.setOnAction(e -> onCertificadoAlumnoRegularClick());

        estadoAcademico.setOnAction(e -> mostrarAlertaInfo("Estado académico en desarrollo."));
        estadoClases.setOnAction(e -> mostrarAlertaInfo("Vista para docentes."));

        tramites.getItems().addAll(certificados, estadoAcademico, estadoClases);

        Menu cuenta = new Menu("Cuenta");
        MenuItem perfil = new MenuItem("Mis Datos Personales");
        MenuItem cerrar = new MenuItem("Cerrar Sesión");

        perfil.setOnAction(e -> abrirVentanaPerfil());


        cerrar.setOnAction(e -> {
            // Cerrá la ventana principal actual
            Stage mainStage = (Stage) menuBar.getScene().getWindow();
            mainStage.close();
            // Volvé al login
            Main.goToLogin();
        });

        cuenta.getItems().addAll(perfil, cerrar);

        menuBar.getMenus().addAll(inscripciones, tramites, cuenta);
    }

    private void mostrarAlertaInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void abrirVentanaModal(String fxmlRuta, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlRuta));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaInfo("No se pudo abrir la vista.");
        }
    }


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    private void abrirVentanaPerfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PerfilView.fxml"));
            Parent root = loader.load();
            PerfilController perfilController = loader.getController();
            perfilController.setUsuarioActual(this.usuarioActual); // ← PASÁ EL USUARIO ACTUAL
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/estilos.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Mis Datos Personales");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaInfo("No se pudo abrir la vista de perfil.");
        }
    }

}
