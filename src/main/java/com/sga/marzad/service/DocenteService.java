package com.sga.marzad.service;

import com.sga.marzad.model.AlumnoNotasDocente;
import com.sga.marzad.model.Materia;

import java.util.ArrayList;
import java.util.List;

public class DocenteService {

    // Devuelve la lista de materias a cargo del docente
    public List<Materia> obtenerMateriasPorDocente(int docenteId) {
        // MOCK: Simula dos materias a cargo
        List<Materia> materias = new ArrayList<>();
        materias.add(new Materia(1, "Matemática", 1));
        materias.add(new Materia(2, "Programación", 2));
        return materias;

        // TODO: Reemplazar por consulta real usando DAO
    }

    // Devuelve alumnos con sus notas en una materia
    public List<AlumnoNotasDocente> obtenerAlumnosNotasPorMateria(int materiaId) {
        // MOCK: Simula dos alumnos con algunas notas cargadas
        List<AlumnoNotasDocente> alumnos = new ArrayList<>();
        alumnos.add(new AlumnoNotasDocente(
                101, "Juan Pérez", "35123456", "juan@email.com", "ACTIVA",
                8.5, 0.0, 7.0, null, null));
        alumnos.add(new AlumnoNotasDocente(
                102, "Ana Gómez", "33999888", "ana@email.com", "ACTIVA",
                6.0, 5.0, null, null, null));
        return alumnos;

        // TODO: Reemplazar por consulta real usando DAO
    }

    // Guarda una nota para el alumno/materia/tipo (agrega o edita)
    public void guardarNota(int alumnoId, int materiaId, String tipoNota, Double valor) {
        // TODO: Implementar lógica para guardar en BD usando DAO/calificaciones
        System.out.println("Guardando " + tipoNota + ": " + valor +
                " para alumnoId=" + alumnoId + " materiaId=" + materiaId);
    }

    // Elimina todas las notas del alumno en una materia (puedes hacer métodos para borrar individual)
    public void eliminarTodasLasNotas(int alumnoId, int materiaId) {
        // TODO: Implementar lógica para eliminar de BD
        System.out.println("Eliminando todas las notas de alumnoId=" +
                alumnoId + " en materiaId=" + materiaId);
    }
}
