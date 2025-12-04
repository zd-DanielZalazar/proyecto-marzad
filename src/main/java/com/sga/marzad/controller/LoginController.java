package com.sga.marzad.controller;

import com.sga.marzad.Main;
import com.sga.marzad.dao.AlumnoDAO;
import com.sga.marzad.dao.DocenteDAO;
import com.sga.marzad.dao.InscripcionCarreraDAO;
import com.sga.marzad.model.Alumno;
import com.sga.marzad.model.Docente;
import com.sga.marzad.model.InscripcionCarrera;
import com.sga.marzad.model.Usuario;
import com.sga.marzad.utils.UsuarioSesion;
import com.sga.marzad.viewmodel.LoginViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private TextField txtPass;

    private final LoginViewModel vm = new LoginViewModel();
    private final AlumnoDAO alumnoDao = new AlumnoDAO();
    private final DocenteDAO docenteDao = new DocenteDAO();
    private final InscripcionCarreraDAO inscCarreraDao = new InscripcionCarreraDAO();

    @FXML
    private void onLogin(ActionEvent e) {
        String user = txtUser.getText();
        String pass = txtPass.getText();
        Usuario u = vm.autenticar(user, pass);

        if (u != null) {
            UsuarioSesion.limpiarSesion();
            UsuarioSesion.setUsuario(u);

            if ("ALUMNO".equalsIgnoreCase(u.getRol())) {
                int alumnoId = asegurarAlumnoParaUsuario(u);
                if (alumnoId == -1) return;
                UsuarioSesion.setAlumnoId(alumnoId);

                // Buscar inscripcion activa a carrera
                InscripcionCarrera insc = inscCarreraDao.obtenerInscripcionActivaPorAlumno(alumnoId);
                if (insc != null) {
                    UsuarioSesion.setCarreraId(insc.getCarreraId());
                    UsuarioSesion.setInscripcionCarreraId(insc.getId());
                    UsuarioSesion.setCarreraNombre(insc.getNombreCarrera());
                }
            } else if ("DOCENTE".equalsIgnoreCase(u.getRol())) {
                int docenteId = asegurarDocenteParaUsuario(u);
                if (docenteId == -1) return;
                UsuarioSesion.setDocenteId(docenteId);
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Sistema de Gestion Academica");
                stage.show();

                // Cerrar ventana login actual
                Stage currentStage = (Stage) txtUser.getScene().getWindow();
                currentStage.close();

            } catch (IOException ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al abrir la vista principal.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Credenciales invalidas.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegistroView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Login");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /** Crea un alumno basico si no existe y devuelve su id */
    private int asegurarAlumnoParaUsuario(Usuario u) {
        int alumnoId = alumnoDao.obtenerIdPorUsuarioId(u.getId());
        if (alumnoId != -1) return alumnoId;

        Alumno nuevo = new Alumno(
                0,
                u.getId(),
                u.getUsername(),
                u.getUsername(),
                null,
                null,
                null,
                null,
                true
        );
        if (alumnoDao.insert(nuevo)) {
            return alumnoDao.obtenerIdPorUsuarioId(u.getId());
        }
        mostrarAlerta("No se encontro registro de alumno para este usuario.", Alert.AlertType.ERROR);
        return -1;
    }

    /** Crea un docente basico si no existe y devuelve su id */
    private int asegurarDocenteParaUsuario(Usuario u) {
        int docenteId = docenteDao.obtenerIdPorUsuarioId(u.getId());
        if (docenteId != -1) return docenteId;

        Docente nuevo = new Docente(
                0,
                u.getId(),
                u.getUsername(),
                u.getUsername(),
                "DOC" + u.getId(),
                u.getUsername() + "@mail.local",
                null,
                true
        );
        if (docenteDao.insert(nuevo)) {
            return docenteDao.obtenerIdPorUsuarioId(u.getId());
        }
        mostrarAlerta("No se encontro registro de docente para este usuario.", Alert.AlertType.ERROR);
        return -1;
    }
}
