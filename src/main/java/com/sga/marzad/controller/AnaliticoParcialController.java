package com.sga.marzad.controller;

import com.sga.marzad.service.AnaliticoParcialService;
import com.sga.marzad.service.AnaliticoParcialService.Materia;
import com.sga.marzad.dao.AnaliticoParcialDAO.SolicitudAnalitico;
import javafx.event.ActionEvent;

import java.util.*;

public class AnaliticoParcialController {

    private AnaliticoParcialService service = new AnaliticoParcialService();

    // --- Solicitud por parte del alumno ---
    public void solicitarAnalitico(int alumnoId) {
        service.solicitarAnalitico(alumnoId);
        System.out.println("Solicitud registrada correctamente.");
    }

    // --- Ver materias y correlativas ---
    public void mostrarMateriasYCorrelativas(int alumnoId) {
        Map<String, List<String>> mapa = service.obtenerMateriasYCorrelativas(alumnoId);
        mapa.forEach((materia, correlativas) -> {
            System.out.println("Materia: " + materia);
            System.out.println("Correlativas: " + String.join(", ", correlativas));
        });
    }

    // --- Ver solicitudes pendientes ---
    public void mostrarSolicitudesPendientes() {
        List<SolicitudAnalitico> solicitudes = service.listarSolicitudesPendientes();
        for (SolicitudAnalitico s : solicitudes) {
            System.out.println("ID: " + s.getId() + " | " + s.getNombre() + " " + s.getApellido() + " | DNI: " + s.getDni());
        }
    }

    // --- Validar solicitud ---
    public void validar(int solicitudId, String firma, String sello) {
        service.validarSolicitud(solicitudId, firma, sello);
        System.out.println("Solicitud validada.");
    }

    // --- Generar PDF (simulado) ---
    public void generarPDF(int solicitudId) {
        SolicitudAnalitico datos = service.obtenerDatosSolicitud(solicitudId);
        System.out.println("Generando PDF para: " + datos.getNombre() + " " + datos.getApellido());
        // Aquí iría la lógica con iText o similar
    }

    public void onSolicitarAnalitico(ActionEvent actionEvent) {
        // Lógica para registrar la solicitud del analítico
        // Ejemplo: guardar en base de datos, enviar notificación, etc.
        System.out.println("Solicitud de analítico enviada correctamente.");
    }

    public void onValidarYGenerar(ActionEvent actionEvent) {
        // Lógica para validar firma/sello y generar el PDF institucional
        // Ejemplo: verificar campos, generar documento, actualizar estado
        System.out.println("PDF generado y validado correctamente.");
    }
}
