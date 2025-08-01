package com.sga.marzad.viewmodel;

import com.sga.marzad.dao.AlumnoDAO;
import com.sga.marzad.model.Alumno;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AlumnoViewModel {
    private final AlumnoDAO dao = new AlumnoDAO();
    private final ObservableList<Alumno> alumnos = FXCollections.observableArrayList();

    public AlumnoViewModel() {
        loadAlumnos();
    }

    /** Devuelve la lista observable para enlazar a una TableView, por ejemplo */
    public ObservableList<Alumno> getAlumnos() {
        return alumnos;
    }

    /** Recarga desde base de datos todos los alumnos */
    public void loadAlumnos() {
        alumnos.setAll(dao.findAll());
    }

    /**
     * Inserta un nuevo alumno.
     * @param a el alumno a crear (sin id aún)
     * @return true si se guardó correctamente
     */
    public boolean crearAlumno(Alumno a) {
        boolean ok = dao.insert(a);
        if (ok) {
            alumnos.add(a);
        }
        return ok;
    }

    /**
     * Actualiza un alumno existente.
     * @param a el alumno con cambios (debe tener id)
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarAlumno(Alumno a) {
        boolean ok = dao.update(a);
        if (ok) {
            // refresca la lista entera o individualmente
            loadAlumnos();
        }
        return ok;
    }

    /**
     * Elimina un alumno de la base y de la lista.
     * @param a el alumno a borrar
     * @return true si la eliminación fue exitosa
     */
    public boolean eliminarAlumno(Alumno a) {
        boolean ok = dao.delete(a.getId());
        if (ok) {
            alumnos.remove(a);
        }
        return ok;
    }
}
