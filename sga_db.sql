-- =========================
-- BASE DE DATOS
-- =========================
DROP DATABASE IF EXISTS sga_db;
CREATE DATABASE sga_db;
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
    hash_password VARCHAR(255) NOT NULL,
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

-- =========================
-- CALIFICACIONES
-- =========================
CREATE TABLE calificaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    inscripcion_id INT NOT NULL,
    docente_id INT NOT NULL,
    nota DECIMAL(4,2) NOT NULL CHECK (nota BETWEEN 0 AND 10),
    fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    FOREIGN KEY (inscripcion_id) REFERENCES inscripciones(id),
    FOREIGN KEY (docente_id) REFERENCES docentes(id)
) ENGINE=InnoDB;

-- =========================
-- DATOS INICIALES
-- =========================
INSERT INTO roles (nombre) VALUES ('ADMIN'), ('DOCENTE'), ('ALUMNO');

INSERT INTO usuarios (username, hash_password, rol_id, habilitado)
VALUES ('admin', SHA2('admin123',256), 1, TRUE);

INSERT INTO docentes (usuario_id, nombre, apellido, legajo, correo)
VALUES (1, 'Super', 'Admin', 'ADM001', 'admin@sga.local');

-- ===========================
-- Insert de datos 
-- ===========================

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

USE sga_db;

-- =========================
-- USUARIOS DE PRUEBA
-- =========================
-- DOCENTE
INSERT IGNORE INTO usuarios (username, hash_password, rol_id, habilitado)
VALUES ('jlopez', SHA2('docente123',256), (SELECT id FROM roles WHERE nombre='DOCENTE'), TRUE);

SET @id_usuario_docente := LAST_INSERT_ID();

INSERT IGNORE INTO docentes (usuario_id, nombre, apellido, legajo, correo, habilitado)
VALUES (@id_usuario_docente, 'Juan', 'López', 'DOC001', 'jlopez@sga.local', TRUE);

-- ALUMNO 1
INSERT IGNORE INTO usuarios (username, hash_password, rol_id, habilitado)
VALUES ('dzalazar', SHA2('alumno123',256), (SELECT id FROM roles WHERE nombre='ALUMNO'), TRUE);

SET @id_usuario_alumno1 := LAST_INSERT_ID();

INSERT IGNORE INTO alumnos (usuario_id, nombre, apellido, dni, correo, fecha_nac, genero, habilitado)
VALUES (@id_usuario_alumno1, 'Daniel', 'Zalazar', '40111222', 'dzalazar@mail.com', '2000-05-10', 'M', TRUE);

-- ALUMNO 2
INSERT IGNORE INTO usuarios (username, hash_password, rol_id, habilitado)
VALUES ('qmarsico', SHA2('alumno123',256), (SELECT id FROM roles WHERE nombre='ALUMNO'), TRUE);

SET @id_usuario_alumno2 := LAST_INSERT_ID();

INSERT IGNORE INTO alumnos (usuario_id, nombre, apellido, dni, correo, fecha_nac, genero, habilitado)
VALUES (@id_usuario_alumno2, 'Quimey', 'Marsico', '40222333', 'qmarsico@mail.com', '1999-08-22', 'F', TRUE);

-- =========================
-- INSCRIPCIONES A CARRERAS
-- =========================
-- Daniel en Analista de Sistemas
INSERT IGNORE INTO inscripciones_carrera (alumno_id, carrera_id, estado)
VALUES (
  (SELECT id FROM alumnos WHERE usuario_id=@id_usuario_alumno1),
  (SELECT id FROM carreras WHERE nombre='Tecnicatura Superior en Análisis de Sistemas'),
  'APROBADA'
);

SET @id_insc_carrera1 := LAST_INSERT_ID();

-- Quimey en Analista Programador
INSERT IGNORE INTO inscripciones_carrera (alumno_id, carrera_id, estado)
VALUES (
  (SELECT id FROM alumnos WHERE usuario_id=@id_usuario_alumno2),
  (SELECT id FROM carreras WHERE nombre='Tecnicatura Superior en Analista Programador'),
  'APROBADA'
);

SET @id_insc_carrera2 := LAST_INSERT_ID();

-- =========================
-- INSCRIPCIONES A MATERIAS
-- =========================
-- Daniel inscripto en todas las materias de 1er año de Sistemas
INSERT IGNORE INTO inscripciones (alumno_id, materia_id, inscripcion_carrera_id, estado)
SELECT 
  (SELECT id FROM alumnos WHERE usuario_id=@id_usuario_alumno1),
  m.id,
  @id_insc_carrera1,
  'ACTIVA'
FROM materias m
JOIN planes_estudio p ON m.plan_id = p.id
JOIN carreras c ON p.carrera_id = c.id
WHERE c.nombre='Tecnicatura Superior en Análisis de Sistemas' AND m.anio=1;

-- Quimey inscripto en todas las materias de 1er año de Programador
INSERT IGNORE INTO inscripciones (alumno_id, materia_id, inscripcion_carrera_id, estado)
SELECT 
  (SELECT id FROM alumnos WHERE usuario_id=@id_usuario_alumno2),
  m.id,
  @id_insc_carrera2,
  'ACTIVA'
FROM materias m
JOIN planes_estudio p ON m.plan_id = p.id
JOIN carreras c ON p.carrera_id = c.id
WHERE c.nombre='Tecnicatura Superior en Analista Programador' AND m.anio=1;

-- =========================
-- DOCENTE ASIGNADO A MATERIAS
-- =========================
-- El docente Juan López dicta Elementos de Matemática y Programación 1 en Sistemas
INSERT IGNORE INTO materia_docente (materia_id, docente_id)
SELECT m.id, d.id
FROM materias m, docentes d
WHERE m.nombre IN ('Elementos de Matemática','Programación 1')
  AND d.usuario_id=@id_usuario_docente
  AND m.plan_id=@id_plan_sistemas;

-- =========================
-- CALIFICACIONES DE PRUEBA
-- =========================
-- Daniel obtiene notas en Elementos de Matemática y Lógica
INSERT IGNORE INTO calificaciones (inscripcion_id, docente_id, nota, observaciones)
SELECT i.id, d.id, 8.00, 'Buen rendimiento'
FROM inscripciones i
JOIN materias m ON i.materia_id=m.id
JOIN docentes d ON d.usuario_id=@id_usuario_docente
WHERE i.alumno_id=(SELECT id FROM alumnos WHERE usuario_id=@id_usuario_alumno1)
  AND m.nombre='Elementos de Matemática'
LIMIT 1;

INSERT IGNORE INTO calificaciones (inscripcion_id, docente_id, nota, observaciones)
SELECT i.id, d.id, 7.00, 'Cumple con lo esperado'
FROM inscripciones i
JOIN materias m ON i.materia_id=m.id
JOIN docentes d ON d.usuario_id=@id_usuario_docente
WHERE i.alumno_id=(SELECT id FROM alumnos WHERE usuario_id=@id_usuario_alumno1)
  AND m.nombre='Lógica y Estructura de Datos'
LIMIT 1;

select * from alumnos;
select * from docentes;

SHOW DATABASES;

UPDATE usuarios
SET hash_password = SHA2('1234',256)
WHERE username = 'dzalazar';

-- ===================================================
-- 1. Ajustar la columna hash_password -> password
-- ===================================================
ALTER TABLE usuarios 
CHANGE COLUMN hash_password password VARCHAR(255) NOT NULL;

-- ===================================================
-- 2. Limpiar usuarios de prueba viejos
-- (opcional: solo si querés empezar de cero con dzalazar)
-- ===================================================
DELETE FROM alumnos  WHERE usuario_id IN (SELECT id FROM usuarios WHERE username='dzalazar');
DELETE FROM usuarios WHERE username='dzalazar';

-- ===================================================
-- 3. Asegurar roles base
-- ===================================================
INSERT IGNORE INTO roles (nombre) VALUES ('ADMIN'), ('DOCENTE'), ('ALUMNO');

-- ===================================================
-- 4. Crear usuario de prueba (texto plano en password)
-- ===================================================
INSERT INTO usuarios (username, password, rol_id, habilitado)
VALUES ('dzalazar', 'alumno123', (SELECT id FROM roles WHERE nombre='ALUMNO'), TRUE);

-- Vincular alumno (si lo necesitás para probar el login y demás pantallas)
SET @uid := (SELECT id FROM usuarios WHERE username='dzalazar');
INSERT INTO alumnos (usuario_id, nombre, apellido, dni, correo, fecha_nac, genero, habilitado)
VALUES (@uid, 'Daniel', 'Zalazar', '40111222', 'dzalazar@mail.com', '2000-05-10', 'M', TRUE);

UPDATE usuarios
SET password = 'alumno123'
WHERE username = 'dzalazar';

UPDATE alumnos
SET habilitado = TRUE
WHERE usuario_id = (SELECT id FROM usuarios WHERE username='dzalazar');




