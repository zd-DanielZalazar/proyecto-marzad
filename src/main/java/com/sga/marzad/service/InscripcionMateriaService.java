package com.sga.marzad.service;

import com.sga.marzad.dao.InscripcionMateriaDAO;
import com.sga.marzad.model.InscripcionMateria;
import com.sga.marzad.model.MateriaDisponible;

import java.util.List;

public class InscripcionMateriaService {

    private final InscripcionMateriaDAO dao = new InscripcionMateriaDAO();

    // Lista materias disponibles para el alumno en una carrera
    public List<MateriaDisponible> obtenerMateriasDisponibles(int alumnoId, int carreraId) {
        return dao.materiasDisponibles(alumnoId, carreraId);
    }

    // Inscribir alumno a materia (con chequeos mínimos)
    public String inscribirAlumnoAMateria(int alumnoId, int materiaId, int inscripcionCarreraId) {
        // 1. ¿Ya está inscripto?
        if (dao.existeInscripcionActiva(alumnoId, materiaId)) {
            return "Ya está inscripto en esta materia.";
        }

        // 2. Intentar inscribir
        InscripcionMateria insc = new InscripcionMateria(alumnoId, materiaId, inscripcionCarreraId);
        boolean ok = dao.insertar(insc);
        if (ok) {
            return "Inscripción realizada con éxito.";
        } else {
            return "No se pudo inscribir. Intente nuevamente.";
        }
    }

    // Listar inscripciones activas del alumno (para mostrar en la tabla de la UI)
    public List<InscripcionMateria> obtenerInscripcionesPorAlumno(int alumnoId) {
        return dao.listarPorAlumno(alumnoId);
    }
}
