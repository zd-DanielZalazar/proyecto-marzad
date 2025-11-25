package com.sga.marzad.controller;

import com.sga.marzad.dao.AlumnoDAO;
import com.sga.marzad.dao.AnaliticoDAO;
import com.sga.marzad.model.Alumno;
import com.sga.marzad.model.AnaliticoMateria;
import com.sga.marzad.utils.UsuarioSesion;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AnaliticoParcialController implements Initializable {

    @FXML private Label lblAlumno;
    @FXML private Label lblCarrera;
    @FXML private Label lblPromedio;
    @FXML private TableView<AnaliticoMateria> tablaAnalitico;
    @FXML private TableColumn<AnaliticoMateria, String> colMateria;
    @FXML private TableColumn<AnaliticoMateria, String> colAnio;
    @FXML private TableColumn<AnaliticoMateria, String> colCuatrimestre;
    @FXML private TableColumn<AnaliticoMateria, String> colNota;
    @FXML private TableColumn<AnaliticoMateria, String> colCondicion;
    @FXML private TableColumn<AnaliticoMateria, String> colFecha;

    private final AnaliticoDAO analiticoDAO = new AnaliticoDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabla();
        cargarDatos();
    }

    private void configurarTabla() {
        tablaAnalitico.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colMateria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreMateria()));
        colAnio.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAnio())));
        colCuatrimestre.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCuatrimestre())));
        colNota.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNotaTexto()));
        colCondicion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCondicion()));
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaFormateada()));
    }

    private void cargarDatos() {
        Integer alumnoId = UsuarioSesion.getAlumnoId();
        if (alumnoId == null) {
            mostrarError("No se encontró un alumno asociado a la sesión actual.");
            return;
        }
        Alumno alumno = UsuarioSesion.getUsuarioId() != null
                ? AlumnoDAO.buscarPorUsuarioId(UsuarioSesion.getUsuarioId())
                : null;
        lblAlumno.setText(alumno != null
                ? alumno.getNombre() + " " + alumno.getApellido()
                : UsuarioSesion.getUserName());
        lblCarrera.setText(UsuarioSesion.getCarreraNombre() != null
                ? UsuarioSesion.getCarreraNombre()
                : "Carrera sin asignar");

        List<AnaliticoMateria> registros = analiticoDAO.listarPorAlumno(alumnoId);
        tablaAnalitico.getItems().setAll(registros);
        lblPromedio.setText(calcularPromedio(registros));
    }

    private String calcularPromedio(List<AnaliticoMateria> registros) {
        double suma = 0;
        int cantidad = 0;
        for (AnaliticoMateria item : registros) {
            if ("APROBADA".equalsIgnoreCase(item.getCondicion()) && item.getNota() != null) {
                suma += item.getNota();
                cantidad++;
            }
        }
        return cantidad == 0 ? "Promedio: -" : String.format("Promedio: %.2f", suma / cantidad);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
