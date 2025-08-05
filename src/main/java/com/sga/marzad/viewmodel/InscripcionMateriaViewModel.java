package com.sga.marzad.viewmodel;

import com.sga.marzad.dao.InscripcionMateriaDAO;
import com.sga.marzad.model.MateriaDisponible;
import com.sga.marzad.model.InscripcionMateria;

import java.util.List;

public class InscripcionMateriaViewModel {
    private final InscripcionMateriaDAO inscripcionDao;

    public InscripcionMateriaViewModel() {
        this.inscripcionDao = new InscripcionMateriaDAO();
    }

    public List<MateriaDisponible> getMateriasDisponibles(int alumnoId, int carreraId) {
        return inscripcionDao.materiasDisponibles(alumnoId, carreraId);
    }

    public boolean correlativasAprobadas(int alumnoId, int materiaId) {
        List<Integer> correlativas = inscripcionDao.getCorrelativasIds(materiaId);
        if (correlativas.isEmpty()) return true;
        for (int correlativaId : correlativas) {
            if (!inscripcionDao.materiaAprobada(alumnoId, correlativaId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Realiza la inscripción y devuelve el resultado detallado.
     */
    public ResultadoInscripcion inscribir(int alumnoId, int materiaId, int inscripcionCarreraId) {
        try {
            if (inscripcionDao.existeInscripcionActiva(alumnoId, materiaId)) {
                return ResultadoInscripcion.YA_INSCRIPTO;
            }
            if (!correlativasAprobadas(alumnoId, materiaId)) {
                return ResultadoInscripcion.CORRELATIVA_NO_APROBADA;
            }
            InscripcionMateria nueva = new InscripcionMateria(0, alumnoId, materiaId, inscripcionCarreraId, null, "ACTIVA");
            boolean exito = inscripcionDao.insertar(nueva);
            return exito ? ResultadoInscripcion.OK : ResultadoInscripcion.ERROR_BD;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultadoInscripcion.ERROR_BD;
        }
    }
}
