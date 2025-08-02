package com.sga.marzad.controller;

import com.sga.marzad.Main;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Label lblReloj;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblSistema;

    @FXML
    private MenuBar menuBar;

    @FXML
    private ImageView logoImage;

    // Estos son opcionales si los usás en el footer
    @FXML
    private Label lblVersion;

    @FXML
    private Label lblAutor;

    @FXML
    private Label lblBdStatus;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        iniciarReloj();
        cargarDatosUsuario("Juan", "DOCENTE"); // o parámetro real
        cargarMenu(); // ← Esto estaba faltando
    }

    private final Locale locale = new Locale("es", "AR");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy - HH:mm", locale);


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

    private void cargarDatosUsuario(String nombre, String rol) {
        lblUsuario.setText(nombre + " (" + rol + ")");
    }
    private void cargarMenu() {
        // Íconos se pueden agregar con Ikonli más adelante
        Menu inscripciones = new Menu("Inscripciones");
        MenuItem materias = new MenuItem("Inscripción a Materias");
        MenuItem examenes = new MenuItem("Inscripción a Exámenes Finales");

        materias.setOnAction(e -> abrirVentanaModal("/view/AlumnosView.fxml", "Inscripción a Materias"));
        examenes.setOnAction(e -> mostrarAlertaInfo("Esta vista aún no está disponible."));

        inscripciones.getItems().addAll(materias, examenes);

        Menu tramites = new Menu("Trámites");
        MenuItem certificados = new MenuItem("Descarga de Certificados");
        MenuItem estadoAcademico = new MenuItem("Estado Académico");
        MenuItem estadoClases = new MenuItem("Estado de Clases");

        certificados.setOnAction(e -> mostrarAlertaInfo("Certificados aún no disponibles."));
        estadoAcademico.setOnAction(e -> mostrarAlertaInfo("Estado académico en desarrollo."));
        estadoClases.setOnAction(e -> mostrarAlertaInfo("Vista para docentes."));

        tramites.getItems().addAll(certificados, estadoAcademico, estadoClases);

        Menu cuenta = new Menu("Cuenta");
        MenuItem perfil = new MenuItem("Mis Datos Personales");
        MenuItem cerrar = new MenuItem("Cerrar Sesión");

        perfil.setOnAction(e -> mostrarAlertaInfo("Perfil en construcción."));
        cerrar.setOnAction(e -> Main.goToLogin());

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


}
