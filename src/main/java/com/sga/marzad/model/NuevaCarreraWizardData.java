package com.sga.marzad.model;

import java.util.ArrayList;
import java.util.List;

public class NuevaCarreraWizardData {
    // Datos de la carrera
    private String nombreCarrera;
    private String descripcionCarrera;
    private int duracionAnios;

    // Materias a crear
    public static class MateriaWizard {
        public String nombre;
        public int anio;
        public int docenteId;
        public String dia;
        public String hora;
        public List<Integer> correlativas = new ArrayList<>(); // id de materias del wizard
    }

    private List<MateriaWizard> materias = new ArrayList<>();

    public String getNombreCarrera() { return nombreCarrera; }
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }
    public String getDescripcionCarrera() { return descripcionCarrera; }
    public void setDescripcionCarrera(String descripcionCarrera) { this.descripcionCarrera = descripcionCarrera; }
    public int getDuracionAnios() { return duracionAnios; }
    public void setDuracionAnios(int duracionAnios) { this.duracionAnios = duracionAnios; }
    public List<MateriaWizard> getMaterias() { return materias; }
}
