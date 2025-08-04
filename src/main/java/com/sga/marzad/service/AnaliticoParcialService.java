package com.sga.marzad.service;

import com.sga.marzad.dao.AnaliticoParcialDAO;
import com.sga.marzad.dao.AnaliticoParcialDAO.SolicitudAnalitico;

import java.util.*;

public class AnaliticoParcialService {

    public static class Materia {
        private String nombre;

        public Materia(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    // --- Solicitar analítico parcial ---
    public void solicitarAnalitico(int alumnoId) {
        AnaliticoParcialDAO.registrarSolicitud(alumnoId);
    }

    // --- Obtener materias y correlativas ---
    public Map<String, List<String>> obtenerMateriasYCorrelativas(int alumnoId) {
        List<Materia> materias = AnaliticoParcialDAO.obtenerMateriasCursadas(alumnoId);
        return AnaliticoParcialDAO.obtenerCorrelativas(materias);
    }

    // --- Obtener solicitudes pendientes ---
    public List<SolicitudAnalitico> listarSolicitudesPendientes() {
        return AnaliticoParcialDAO.obtenerSolicitudesPendientes();
    }

    // --- Validar solicitud ---
    public void validarSolicitud(int solicitudId, String firma, String sello) {
        AnaliticoParcialDAO.validarSolicitud(solicitudId, firma, sello);
    }

    // --- Obtener datos completos para PDF ---
    public SolicitudAnalitico obtenerDatosSolicitud(int solicitudId) {
        return AnaliticoParcialDAO.obtenerSolicitudPorId(solicitudId);
    }
}