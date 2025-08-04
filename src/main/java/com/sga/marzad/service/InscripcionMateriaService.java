package com.sga.marzad.service;

import com.sga.marzad.dao.InscripcionMateriaDAO;
import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;

import java.util.List;

public class InscripcionMateriaService {

    private final InscripcionMateriaDAO dao = new InscripcionMateriaDAO();

    /**
     * Devuelve la lista de materias disponibles para inscribirse,
     * filtrando por alumno y carrera (según reglas del sistema).
     */
    public List<MateriaDisponible> obtenerMateriasDisponibles(int alumnoId, int carreraId) {
        try {
            return dao.materiasDisponibles(alumnoId, carreraId);
        } catch (Exception e) {
            // Loggear el error y devolver lista vacía
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Intenta inscribir al alumno en la materia seleccionada, validando:
     * - Que no esté inscripto ya.
     * - Que cumpla correlatividades.
     * Devuelve mensaje para mostrar al usuario.
     */
    public String inscribirAlumnoAMateria(int alumnoId, int materiaId, int inscripcionCarreraId) {
        try {
            // 1. ¿Ya está inscripto?
            if (dao.existeInscripcionActiva(alumnoId, materiaId)) {
                return "Ya está inscripto en esta materia.";
            }
            // 2. ¿Cumple correlativas?
            List<Integer> correlativas = dao.getCorrelativasIds(materiaId);
            for (int idCorr : correlativas) {
                if (!dao.materiaAprobada(alumnoId, idCorr)) {
                    String nombreCorr = dao.obtenerNombreMateriaPorId(idCorr);
                    return "Debe aprobar la correlativa: " + nombreCorr;
                }
            }
            // 3. Intentar inscribir
            InscripcionMateria insc = new InscripcionMateria(alumnoId, materiaId, inscripcionCarreraId);
            boolean ok = dao.insertar(insc);
            if (ok) {
                return "Inscripción realizada con éxito.";
            } else {
                return "No se pudo inscribir. Intente nuevamente.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error inesperado al inscribir: " + e.getMessage();
        }
    }

    /**
     * Lista todas las inscripciones del alumno (para mostrar en la tabla).
     */
    public List<InscripcionMateria> obtenerInscripcionesPorAlumno(int alumnoId) {
        try {
            return dao.listarPorAlumno(alumnoId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Devuelve true si el alumno puede inscribirse a la materia (valida inscripto y correlativas).
     */
    public boolean puedeInscribirse(int alumnoId, int materiaId) {
        try {
            if (dao.existeInscripcionActiva(alumnoId, materiaId)) return false;
            List<Integer> correlativas = dao.getCorrelativasIds(materiaId);
            for (int idCorr : correlativas) {
                if (!dao.materiaAprobada(alumnoId, idCorr)) return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Devuelve el nombre de la materia según su id (útil para mostrar en tablas).
     */
    public String obtenerNombreMateriaPorId(int materiaId) {
        try {
            return dao.obtenerNombreMateriaPorId(materiaId);
        } catch (Exception e) {
            e.printStackTrace();
            return "(error)";
        }
    }
}
