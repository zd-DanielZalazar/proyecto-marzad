-- =========================
-- RECREAR BASE DE DATOS
-- =========================
DROP DATABASE IF EXISTS sga_db;
CREATE DATABASE sga_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE sga_db;

-- =========================
-- ROLES Y USUARIOS
-- =========================
CREATE TABLE roles (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE usuarios (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          username VARCHAR(50) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,   -- texto plano
                          rol_id INT NOT NULL,
                          habilitado BOOLEAN NOT NULL DEFAULT TRUE,
                          creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB;

-- =========================
-- ALUMNOS
-- =========================
CREATE TABLE alumnos (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         usuario_id INT NOT NULL UNIQUE,
                         nombre VARCHAR(50) NOT NULL,
                         apellido VARCHAR(50) NOT NULL,
                         dni VARCHAR(20) UNIQUE,
                         correo VARCHAR(100),
                         fecha_nac DATE,
                         genero ENUM('F','M','Otro'),
                         habilitado BOOLEAN NOT NULL DEFAULT TRUE,
                         FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- =========================
-- DOCENTES
-- =========================
CREATE TABLE docentes (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          usuario_id INT NOT NULL UNIQUE,
                          nombre VARCHAR(50) NOT NULL,
                          apellido VARCHAR(50) NOT NULL,
                          legajo VARCHAR(20) UNIQUE,
                          correo VARCHAR(100),
                          genero ENUM('F','M','Otro'),
                          habilitado BOOLEAN NOT NULL DEFAULT TRUE,
                          FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- =========================
-- CARRERAS Y PLANES DE ESTUDIO
-- =========================
CREATE TABLE carreras (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL,
                          descripcion TEXT,
                          habilitado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE planes_estudio (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                carrera_id INT NOT NULL,
                                nombre VARCHAR(100) NOT NULL,
                                FOREIGN KEY (carrera_id) REFERENCES carreras(id)
) ENGINE=InnoDB;

-- =========================
-- MATERIAS Y CORRELATIVIDADES
-- =========================
CREATE TABLE materias (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          plan_id INT NOT NULL,
                          nombre VARCHAR(100) NOT NULL,
                          anio TINYINT NOT NULL,
                          cuatrimestre TINYINT NOT NULL,
                          creditos SMALLINT NOT NULL DEFAULT 0,
                          habilitado BOOLEAN NOT NULL DEFAULT TRUE,
                          FOREIGN KEY (plan_id) REFERENCES planes_estudio(id)
) ENGINE=InnoDB;

CREATE TABLE correlatividades (
                                  materia_id INT NOT NULL,
                                  correlativa_id INT NOT NULL,
                                  PRIMARY KEY (materia_id, correlativa_id),
                                  FOREIGN KEY (materia_id) REFERENCES materias(id),
                                  FOREIGN KEY (correlativa_id) REFERENCES materias(id)
) ENGINE=InnoDB;

-- =========================
-- RELACIÓN DOCENTE - MATERIA
-- =========================
CREATE TABLE materia_docente (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 materia_id INT NOT NULL,
                                 docente_id INT NOT NULL,
                                 UNIQUE (materia_id, docente_id),
                                 FOREIGN KEY (materia_id) REFERENCES materias(id),
                                 FOREIGN KEY (docente_id) REFERENCES docentes(id)
) ENGINE=InnoDB;

-- =========================
-- INSCRIPCIONES
-- =========================
CREATE TABLE inscripciones_carrera (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       alumno_id INT NOT NULL,
                                       carrera_id INT NOT NULL,
                                       fecha_insc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       estado ENUM('PENDIENTE','APROBADA','RECHAZADA','EGRESADO') NOT NULL DEFAULT 'PENDIENTE',
                                       UNIQUE(alumno_id, carrera_id),
                                       FOREIGN KEY (alumno_id) REFERENCES alumnos(id),
                                       FOREIGN KEY (carrera_id) REFERENCES carreras(id)
) ENGINE=InnoDB;

CREATE TABLE inscripciones (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               alumno_id INT NOT NULL,
                               materia_id INT NOT NULL,
                               inscripcion_carrera_id INT NOT NULL,
                               fecha_insc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               estado ENUM('ACTIVA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
                               FOREIGN KEY (alumno_id) REFERENCES alumnos(id),
                               FOREIGN KEY (materia_id) REFERENCES materias(id),
                               FOREIGN KEY (inscripcion_carrera_id) REFERENCES inscripciones_carrera(id)
) ENGINE=InnoDB;

CREATE TABLE examenes_finales (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  materia_id INT NOT NULL,
                                  fecha DATETIME NOT NULL,
                                  aula VARCHAR(50),
                                  cupo INT NOT NULL DEFAULT 30,
                                  estado ENUM('PUBLICADO','CERRADO') NOT NULL DEFAULT 'PUBLICADO',
                                  FOREIGN KEY (materia_id) REFERENCES materias(id)
) ENGINE=InnoDB;

CREATE TABLE inscripciones_finales (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       alumno_id INT NOT NULL,
                                       examen_final_id INT NOT NULL,
                                       fecha_insc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       estado ENUM('ACTIVA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
                                       UNIQUE(alumno_id, examen_final_id),
                                       FOREIGN KEY (alumno_id) REFERENCES alumnos(id),
                                       FOREIGN KEY (examen_final_id) REFERENCES examenes_finales(id)
) ENGINE=InnoDB;

-- =========================
-- CALIFICACIONES
-- =========================
CREATE TABLE calificaciones (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                inscripcion_id INT NOT NULL,
                                docente_id INT NOT NULL,
                                tipo ENUM('PARCIAL_1','RECUP_1','PARCIAL_2','RECUP_2','FINAL') NOT NULL,
                                nota DECIMAL(4,2) NOT NULL CHECK (nota BETWEEN 0 AND 10),
                                fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                observaciones TEXT,
                                UNIQUE(inscripcion_id, tipo),
                                FOREIGN KEY (inscripcion_id) REFERENCES inscripciones(id),
                                FOREIGN KEY (docente_id) REFERENCES docentes(id)
) ENGINE=InnoDB;

CREATE TABLE asistencias (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             inscripcion_id INT NOT NULL,
                             docente_id INT NOT NULL,
                             fecha DATE NOT NULL,
                             presente BOOLEAN NOT NULL,
                             UNIQUE(inscripcion_id, fecha),
                             FOREIGN KEY (inscripcion_id) REFERENCES inscripciones(id),
                             FOREIGN KEY (docente_id) REFERENCES docentes(id)
) ENGINE=InnoDB;

CREATE TABLE notificaciones_usuario (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        usuario_id INT NOT NULL,
                                        asunto VARCHAR(100) NOT NULL,
                                        mensaje TEXT NOT NULL,
                                        fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- =========================
-- DATOS INICIALES
-- =========================
INSERT INTO roles (nombre) VALUES ('ADMIN'), ('DOCENTE'), ('ALUMNO');

-- Usuario admin (texto plano)
INSERT INTO usuarios (username, password, rol_id, habilitado)
VALUES ('admin', 'admin123', (SELECT id FROM roles WHERE nombre='ADMIN'), TRUE);

INSERT INTO docentes (usuario_id, nombre, apellido, legajo, correo, genero)
VALUES (1, 'Super', 'Admin', 'ADM001', 'admin@sga.local', 'M');

-- Usuario docente
INSERT INTO usuarios (username, password, rol_id, habilitado)
VALUES ('jlopez', 'docente123', (SELECT id FROM roles WHERE nombre='DOCENTE'), TRUE);

SET @id_usuario_docente := LAST_INSERT_ID();

INSERT INTO docentes (usuario_id, nombre, apellido, legajo, correo, genero)
VALUES (@id_usuario_docente, 'Juan', 'López', 'DOC001', 'jlopez@sga.local', 'M');

-- Usuario alumno 1
INSERT INTO usuarios (username, password, rol_id, habilitado)
VALUES ('dzalazar', 'alumno123', (SELECT id FROM roles WHERE nombre='ALUMNO'), TRUE);

SET @id_usuario_alumno1 := LAST_INSERT_ID();

INSERT INTO alumnos (usuario_id, nombre, apellido, dni, correo, fecha_nac, genero)
VALUES (@id_usuario_alumno1, 'Daniel', 'Zalazar', '40111222', 'dzalazar@mail.com', '2000-05-10', 'M');

-- Usuario alumno 2
INSERT INTO usuarios (username, password, rol_id, habilitado)
VALUES ('qmarsico', 'alumno123', (SELECT id FROM roles WHERE nombre='ALUMNO'), TRUE);

SET @id_usuario_alumno2 := LAST_INSERT_ID();

INSERT INTO alumnos (usuario_id, nombre, apellido, dni, correo, fecha_nac, genero)
VALUES (@id_usuario_alumno2, 'Quimey', 'Marsico', '40222333', 'qmarsico@mail.com', '1999-08-22', 'F');

-- =========================
-- CARRERA: Tecnicatura Superior en Análisis de Sistemas
-- =========================
INSERT INTO carreras (nombre, descripcion, habilitado)
VALUES ('Tecnicatura Superior en Análisis de Sistemas',
        'Carrera orientada al análisis, diseño y desarrollo de sistemas informáticos.',
        TRUE);

SET @id_carrera_sistemas := LAST_INSERT_ID();

INSERT INTO planes_estudio (carrera_id, nombre)
VALUES (@id_carrera_sistemas, 'Plan Analista de Sistemas');

SET @id_plan_sistemas := LAST_INSERT_ID();

-- Materias 1er año
INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES
                                                                                     (@id_plan_sistemas, 'Elementos de Matemática', 1, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Lógica y Estructura de Datos', 1, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Elementos de Informática', 1, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Inglés Técnico', 1, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Matemática Aplicada', 1, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Arquitectura y Sistemas Operativos', 1, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Programación 1', 1, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Comunicación Oral y Escrita', 1, 2, 0, TRUE);

-- Materias 2do año
INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES
                                                                                     (@id_plan_sistemas, 'Estadística', 2, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Análisis de Sistemas', 2, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Programación 2', 2, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Economía de la Empresa', 2, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Base de Datos 1', 2, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Metodología de la Investigación', 2, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Sistemas de Información', 2, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Inglés Técnico 2', 2, 2, 0, TRUE);

-- Materias 3er año
INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES
                                                                                     (@id_plan_sistemas, 'Redes y Comunicaciones', 3, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Proyecto Final', 3, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Base de Datos 2', 3, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Gestión de Recursos Humanos', 3, 1, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Seguridad Informática', 3, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Legislación y Ética Profesional', 3, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Sistemas de Gestión', 3, 2, 0, TRUE),
                                                                                     (@id_plan_sistemas, 'Formulación y Evaluación de Proyectos', 3, 2, 0, TRUE);

-- =========================
-- CARRERA: Tecnicatura Superior en Analista Programador
-- =========================
INSERT INTO carreras (nombre, descripcion, habilitado)
VALUES ('Tecnicatura Superior en Analista Programador',
        'Carrera orientada al desarrollo de aplicaciones y programación avanzada.',
        TRUE);

SET @id_carrera_prog := LAST_INSERT_ID();

INSERT INTO planes_estudio (carrera_id, nombre)
VALUES (@id_carrera_prog, 'Plan Analista Programador');

SET @id_plan_prog := LAST_INSERT_ID();

-- Materias 1er año
INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES
                                                                                     (@id_plan_prog, 'Elementos de Matemática', 1, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Lógica y Estructura de Datos', 1, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Elementos de Informática', 1, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Inglés Técnico', 1, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Matemática Aplicada', 1, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Arquitectura y Sistemas Operativos', 1, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Programación 1', 1, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Comunicación Oral y Escrita', 1, 2, 0, TRUE);

-- Materias 2do año
INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES
                                                                                     (@id_plan_prog, 'Estadística', 2, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Análisis de Sistemas', 2, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Programación 2', 2, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Economía de la Empresa', 2, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Base de Datos 1', 2, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Metodología de la Investigación', 2, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Sistemas de Información', 2, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Inglés Técnico 2', 2, 2, 0, TRUE);

-- Materias 3er año
INSERT INTO materias (plan_id, nombre, anio, cuatrimestre, creditos, habilitado) VALUES
                                                                                     (@id_plan_prog, 'Redes y Comunicaciones', 3, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Proyecto Final', 3, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Base de Datos 2', 3, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Gestión de Recursos Humanos', 3, 1, 0, TRUE),
                                                                                     (@id_plan_prog, 'Seguridad Informática', 3, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Legislación y Ética Profesional', 3, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Sistemas de Gestión', 3, 2, 0, TRUE),
                                                                                     (@id_plan_prog, 'Formulación y Evaluación de Proyectos', 3, 2, 0, TRUE);


-- =========================
-- CORRELATIVIDADES (ambas carreras)
-- =========================

-- PROGRAMACIÓN 2 requiere PROGRAMACIÓN 1
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m2.id, m1.id
FROM materias m1, materias m2
WHERE m1.nombre='Programación 1' AND m2.nombre='Programación 2';

-- BASE DE DATOS 1 requiere PROGRAMACIÓN 1 y MATEMÁTICA APLICADA
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Base de Datos 1' AND c.nombre='Programación 1';

INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Base de Datos 1' AND c.nombre='Matemática Aplicada';

-- ANÁLISIS DE SISTEMAS requiere PROGRAMACIÓN 1 y LÓGICA Y ESTRUCTURA DE DATOS
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Análisis de Sistemas' AND c.nombre='Programación 1';

INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Análisis de Sistemas' AND c.nombre='Lógica y Estructura de Datos';

-- BASE DE DATOS 2 requiere BASE DE DATOS 1
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Base de Datos 2' AND c.nombre='Base de Datos 1';

-- SISTEMAS DE INFORMACIÓN requiere ANÁLISIS DE SISTEMAS
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Sistemas de Información' AND c.nombre='Análisis de Sistemas';

-- PROYECTO FINAL requiere SISTEMAS DE INFORMACIÓN, BASE DE DATOS 2 y PROGRAMACIÓN 2
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Proyecto Final' AND c.nombre='Sistemas de Información';

INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Proyecto Final' AND c.nombre='Base de Datos 2';

INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Proyecto Final' AND c.nombre='Programación 2';

-- SEGURIDAD INFORMÁTICA requiere REDES Y COMUNICACIONES
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Seguridad Informática' AND c.nombre='Redes y Comunicaciones';

-- SISTEMAS DE GESTIÓN requiere SISTEMAS DE INFORMACIÓN
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Sistemas de Gestión' AND c.nombre='Sistemas de Información';

-- FORMULACIÓN Y EVALUACIÓN DE PROYECTOS requiere PROYECTO FINAL
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Formulación y Evaluación de Proyectos' AND c.nombre='Proyecto Final';

-- ARQUITECTURA Y SISTEMAS OPERATIVOS requiere ELEMENTOS DE INFORMÁTICA
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Arquitectura y Sistemas Operativos' AND c.nombre='Elementos de Informática';

-- LÓGICA Y ESTRUCTURA DE DATOS requiere ELEMENTOS DE MATEMÁTICA
INSERT IGNORE INTO correlatividades (materia_id, correlativa_id)
SELECT m.id, c.id
FROM materias m, materias c
WHERE m.nombre='Lógica y Estructura de Datos' AND c.nombre='Elementos de Matemática';

-- =========================
-- EXAMENES FINALES
-- =========================
INSERT INTO examenes_finales (materia_id, fecha, aula, cupo)
SELECT id, '2025-02-18 09:00:00', 'Aula 101', 40
FROM materias WHERE nombre='Programaci��n 1' LIMIT 1;

INSERT INTO examenes_finales (materia_id, fecha, aula, cupo)
SELECT id, '2025-02-19 14:00:00', 'Aula 202', 35
FROM materias WHERE nombre='Base de Datos 1' LIMIT 1;

INSERT INTO examenes_finales (materia_id, fecha, aula, cupo)
SELECT id, '2025-02-25 17:00:00', 'Laboratorio 3', 25
FROM materias WHERE nombre='Proyecto Final' LIMIT 1;

INSERT INTO inscripciones_finales (alumno_id, examen_final_id, estado)
SELECT a.id, e.id, 'ACTIVA'
FROM alumnos a
JOIN usuarios u ON u.id = a.usuario_id
JOIN examenes_finales e ON e.materia_id = (SELECT id FROM materias WHERE nombre='Programaci��n 1' LIMIT 1)
WHERE u.username = 'dzalazar'
LIMIT 1;

-- Relaci�n docente-materia y cursada de ejemplo
INSERT INTO materia_docente (materia_id, docente_id)
SELECT m.id, d.id
FROM materias m
JOIN docentes d ON d.legajo = 'DOC001'
WHERE m.nombre IN ('Programaci��n 1','Base de Datos 1')
ON DUPLICATE KEY UPDATE docente_id = docente_id;

INSERT INTO inscripciones_carrera (alumno_id, carrera_id, estado)
SELECT a.id, c.id, 'APROBADA'
FROM alumnos a
JOIN usuarios u ON u.id = a.usuario_id
JOIN carreras c ON c.nombre = 'Tecnicatura Superior en Anǭlisis de Sistemas'
WHERE u.username IN ('dzalazar','qmarsico')
ON DUPLICATE KEY UPDATE estado = VALUES(estado);

INSERT INTO inscripciones (alumno_id, materia_id, inscripcion_carrera_id, estado)
SELECT a.id, m.id, ic.id, 'ACTIVA'
FROM alumnos a
JOIN usuarios u ON u.id = a.usuario_id
JOIN inscripciones_carrera ic ON ic.alumno_id = a.id
JOIN materias m ON m.nombre = 'Programaci��n 1'
WHERE u.username IN ('dzalazar','qmarsico')
  AND NOT EXISTS (
    SELECT 1 FROM inscripciones i WHERE i.alumno_id = a.id AND i.materia_id = m.id
)
UNION ALL
SELECT a.id, m.id, ic.id, 'ACTIVA'
FROM alumnos a
JOIN usuarios u ON u.id = a.usuario_id
JOIN inscripciones_carrera ic ON ic.alumno_id = a.id
JOIN materias m ON m.nombre = 'Base de Datos 1'
WHERE u.username = 'dzalazar'
  AND NOT EXISTS (
    SELECT 1 FROM inscripciones i WHERE i.alumno_id = a.id AND i.materia_id = m.id
);

-- Usuarios de prueba:
-- admin / admin123
-- jlopez / docente123
-- dzalazar / 1234
-- qmarsico / alumno123
