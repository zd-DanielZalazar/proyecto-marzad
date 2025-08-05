package com.sga.marzad.model;

import java.util.ArrayList;
import java.util.List;

public class NuevaCarreraWizardData {
    private String nombreCarrera;
    private String descripcionCarrera;
    private int duracionAnios;
    private List<MateriaWizard> materias;

    // *** AGREGAR ESTO ***
    private String nombrePlanEstudio;

    public NuevaCarreraWizardData() {
        materias = new ArrayList<>();
    }

    // Getters y setters principales
    public String getNombreCarrera() { return nombreCarrera; }
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    public String getDescripcionCarrera() { return descripcionCarrera; }
    public void setDescripcionCarrera(String descripcionCarrera) { this.descripcionCarrera = descripcionCarrera; }

    public int getDuracionAnios() { return duracionAnios; }
    public void setDuracionAnios(int duracionAnios) { this.duracionAnios = duracionAnios; }

    public List<MateriaWizard> getMaterias() { return materias; }
    public void setMaterias(List<MateriaWizard> materias) { this.materias = materias; }

    // *** AGREGAR ESTOS MÉTODOS ***
    public String getNombrePlanEstudio() {
        return nombrePlanEstudio;
    }

    public void setNombrePlanEstudio(String nombrePlanEstudio) {
        this.nombrePlanEstudio = nombrePlanEstudio;
    }

    // Clase interna para las materias agregadas en el wizard
    public static class MateriaWizard {
        private String nombre;
        private int anio;
        private int docenteId;
        private String dia;
        private String hora;
        // --- nuevos campos y métodos para compatibilidad con Paso4 ---
        private int id;
        private List<Integer> correlativas;
        private int cuatrimestre;
        private int creditos;

        public MateriaWizard() {
            correlativas = new ArrayList<>();
        }
        public MateriaWizard(String nombre, int anio, int docenteId, String dia, String hora) {
            this();
            this.nombre = nombre;
            this.anio = anio;
            this.docenteId = docenteId;
            this.dia = dia;
            this.hora = hora;
        }

        // -------- GETTERS (AGREGADOS) --------
        public String getNombre() { return nombre; }
        public int getAnio() { return anio; }
        public int getDocenteId() { return docenteId; }
        public String getDia() { return dia; }
        public String getHora() { return hora; }
        public int getId() { return id; }
        public List<Integer> getCorrelativas() { return correlativas; }
        public int getCuatrimestre() { return cuatrimestre; }
        public int getCreditos() { return creditos; }

        // -------- SETTERS (opcionales) --------
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setAnio(int anio) { this.anio = anio; }
        public void setDocenteId(int docenteId) { this.docenteId = docenteId; }
        public void setDia(String dia) { this.dia = dia; }
        public void setHora(String hora) { this.hora = hora; }
        public void setId(int id) { this.id = id; }
        public void setCorrelativas(List<Integer> correlativas) { this.correlativas = correlativas; }
        public void setCuatrimestre(int cuatrimestre) { this.cuatrimestre = cuatrimestre; }
        public void setCreditos(int creditos) { this.creditos = creditos; }
    }
}
