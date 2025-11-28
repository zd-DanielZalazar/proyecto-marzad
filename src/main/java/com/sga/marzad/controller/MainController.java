package com.sga.marzad.controller;

import com.sga.marzad.Main;
import com.sga.marzad.model.Alumno;
import com.sga.marzad.model.Usuario;
import com.sga.marzad.utils.UsuarioSesion;
import com.sga.marzad.service.CertificadoRegularService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
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

    // ------------- Reloj -------------
    private final Locale locale = new Locale("es", "AR");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy - HH:mm", locale);

    // ------------- Inicialización -------------
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        iniciarReloj();

        Usuario u = UsuarioSesion.getUsuario();
        if (u != null) {
            lblUsuario.setText(u.getUsername() + " (" + u.getRol() + ")");
        }

        cargarMenu();
    }

    // ------------- Botón certificado -------------
    @FXML
    private void onCertificadoAlumnoRegularClick() {
        try {
            int usuarioId = UsuarioSesion.getUsuario().getId();
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
        menuBar.getMenus().clear();

        Usuario u = UsuarioSesion.getUsuario();
        String rol = u != null ? u.getRol() : "";
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
        boolean esAlumno = "ALUMNO".equalsIgnoreCase(rol);
        boolean esDocente = "DOCENTE".equalsIgnoreCase(rol);

        if (esAdmin) {
            MenuItem usuariosItem = new MenuItem("Gestionar usuarios");
            usuariosItem.setOnAction(e -> abrirVentanaModal("/view/UsuariosView.fxml", "Gestion de usuarios"));
            Menu usuariosMenu = new Menu("Usuarios");
            usuariosMenu.getItems().add(usuariosItem);
            // --------- MENÚ ADMIN ---------
            MenuItem altaCarreraMateriaItem = new MenuItem("Alta de carreras y materias");
            altaCarreraMateriaItem.setOnAction(e -> abrirVentanaModal("/view/AltaCarreraWizard.fxml", "Alta de carreras y materias"));

            MenuItem editarCarreraMateriaItem = new MenuItem("Editar carrera y materias");
            editarCarreraMateriaItem.setOnAction(e -> abrirVentanaModal("/view/CarrerasMateriasView.fxml", "Editar carrera y materias"));

            Menu adminMenu = new Menu("Carrera");
            adminMenu.getItems().addAll(altaCarreraMateriaItem, editarCarreraMateriaItem);

            menuBar.getMenus().add(0, adminMenu);
            menuBar.getMenus().add(0, usuariosMenu);
        }

        // --------- MENÚ INSCRIPCIONES ---------
        if (esAlumno) {
            Menu inscripciones = new Menu("Inscripciones");
            MenuItem materias = new MenuItem("Inscripción a Materias");
            MenuItem examenes = new MenuItem("Inscripción a Exámenes Finales");
            materias.setOnAction(e -> abrirVentanaInscripcionMateria());
            examenes.setOnAction(e -> abrirVentanaInscripcionFinal());
            inscripciones.getItems().addAll(materias, examenes);
            menuBar.getMenus().add(inscripciones);
        }

        // --------- MENÚ TRÁMITES ---------
        if (esAlumno) {
            Menu tramites = new Menu("Trámites");
            MenuItem certificados = new MenuItem("Descarga Certificado Alumno Regular");
            MenuItem analitico = new MenuItem("Analítico parcial");
            certificados.setOnAction(e -> onCertificadoAlumnoRegularClick());
            analitico.setOnAction(e -> abrirAnaliticoParcial());
            tramites.getItems().addAll(certificados, analitico);
            menuBar.getMenus().add(tramites);
        }

        // --------- MENÚ CUENTA ---------
        Menu cuenta = new Menu("Cuenta");
        MenuItem perfil = new MenuItem("Mis Datos Personales");
        MenuItem cerrar = new MenuItem("Cerrar Sesión");

        perfil.setOnAction(e -> abrirVentanaPerfil());
        cerrar.setOnAction(e -> {
            Stage mainStage = (Stage) menuBar.getScene().getWindow();
            mainStage.close();
            Main.goToLogin();
        });
        cuenta.getItems().addAll(perfil, cerrar);

        if (esDocente) {
            Menu docenteMenu = new Menu("Docente");
            MenuItem panelMaterias = new MenuItem("Notas y asistencia de cursada");
            panelMaterias.setOnAction(e -> abrirPanelDocente());
            MenuItem panelFinales = new MenuItem("Finales y alumnos inscriptos");
            panelFinales.setOnAction(e -> abrirPanelDocenteFinales());
            docenteMenu.getItems().addAll(panelMaterias, panelFinales);
            menuBar.getMenus().add(docenteMenu);
        }

        menuBar.getMenus().add(cuenta);
    }

    private void abrirVentanaInscripcionMateria() {
        if (UsuarioSesion.getCarreraId() == null) {
            mostrarAlerta("Atención", "No se detectó carrera activa. Contacte al administrador.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/InscripcionMateriaView.fxml"));
            Parent root = loader.load();
            InscripcionMateriaController controller = loader.getController();

            // ✅ Pasar datos desde la sesión al controller
            controller.setDatosAlumno(
                    UsuarioSesion.getAlumnoId(),
                    UsuarioSesion.getCarreraId(),
                    UsuarioSesion.getInscripcionCarreraId(),
                    UsuarioSesion.getCarreraNombre()
            );

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Inscripción a Materias");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaInfo("No se pudo abrir la vista de inscripción de materias.");
        }
    }

    private void abrirVentanaInscripcionFinal() {
        if (UsuarioSesion.getCarreraId() == null || UsuarioSesion.getAlumnoId() == null) {
            mostrarAlerta("Atención", "Debes tener una carrera activa para inscribirte a finales.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/InscripcionFinalView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Inscripción a Exámenes Finales");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaInfo("No se pudo abrir la vista de finales.");
        }
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

    private void abrirVentanaPerfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PerfilView.fxml"));
            Parent root = loader.load();
            PerfilController perfilController = loader.getController();
            perfilController.setUsuarioActual(UsuarioSesion.getUsuario());

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

    private void abrirAnaliticoParcial() {
        if (UsuarioSesion.getAlumnoId() == null) {
            mostrarAlerta("Atención", "Solo los alumnos pueden consultar el analítico.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AnaliticoParcialView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Analítico Parcial");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaInfo("No se pudo abrir el analítico parcial.");
        }
    }

    private void abrirPanelDocente() {
        if (UsuarioSesion.getDocenteId() == null) {
            mostrarAlerta("Atención", "Debes iniciar sesión como docente para acceder al panel.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DocenteMateriasView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Panel Docente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaInfo("No se pudo abrir el panel docente.");
        }
    }

    private void abrirPanelDocenteFinales() {
        if (UsuarioSesion.getDocenteId() == null) {
            mostrarAlerta("Atención", "Debes iniciar sesión como docente para acceder al panel.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DocenteFinalesView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Finales y alumnos");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaInfo("No se pudo abrir la vista de finales para docentes.");
        }
    }

    // ------------- Helpers -------------

    private void mostrarAlertaInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ⚠️ Buscar alumno sigue siendo necesario SOLO para el certificado
    private Alumno buscarAlumnoPorUsuarioId(int usuarioId) {
        try (var conn = com.sga.marzad.utils.ConexionBD.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM alumnos WHERE usuario_id = ?")) {
            stmt.setInt(1, usuarioId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return new Alumno(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


