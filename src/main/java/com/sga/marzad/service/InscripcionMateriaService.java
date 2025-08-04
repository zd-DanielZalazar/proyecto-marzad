package com.sga.marzad.service;

import com.sga.marzad.dao.InscripcionMateriaDAO;
import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;

import java.util.List;

public class InscripcionMateriaService {

    private final InscripcionMateriaDAO dao = new InscripcionMateriaDAO();

    /**
     * Lista materias disponibles para el alumno en una carrera.
     */
    public List<MateriaDisponible> obtenerMateriasDisponibles(int alumnoId, int carreraId) {
        return dao.materiasDisponibles(alumnoId, carreraId);
    }

    /**
     * Intenta inscribir al alumno en la materia (valida inscripto y correlativas).
     */
    public String inscribirAlumnoAMateria(int alumnoId, int materiaId, int inscripcionCarreraId) {
        // 1. ¿Ya está inscripto?
        if (dao.existeInscripcionActiva(alumnoId, materiaId)) {
            return "Ya está inscripto en esta materia.";
        }
        // 2. ¿Cumple correlativas?
        List<Integer> correlativas = dao.getCorrelativasIds(materiaId);
        for (int idCorr : correlativas) {
            if (!dao.materiaAprobada(alumnoId, idCorr)) {
                return "No cumple las correlativas necesarias.";
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
    }

    /**
     * Lista todas las inscripciones del alumno (para mostrar en la tabla).
     */
    public List<InscripcionMateria> obtenerInscripcionesPorAlumno(int alumnoId) {
        return dao.listarPorAlumno(alumnoId);
    }

    /**
     * Devuelve true si el alumno puede inscribirse a la materia (valida inscripto y correlativas).
     */
    public boolean puedeInscribirse(int alumnoId, int materiaId) {
        if (dao.existeInscripcionActiva(alumnoId, materiaId)) return false;

        List<Integer> correlativas = dao.getCorrelativasIds(materiaId);
        for (int idCorr : correlativas) {
            if (!dao.materiaAprobada(alumnoId, idCorr)) return false;
        }
        return true;
    }

    /**
     * Devuelve el nombre de la materia según su id (útil para mostrar en tablas).
     */
    public String obtenerNombreMateriaPorId(int materiaId) {
        return dao.obtenerNombreMateriaPorId(materiaId);
    }
}
