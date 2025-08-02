-- 1) Base de datos
CREATE DATABASE IF NOT EXISTS sga_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE sga_db;

-- 2) Usuarios y roles (autenticación + seguridad)
CREATE TABLE usuarios (
                          id             INT AUTO_INCREMENT PRIMARY KEY,
                          username       VARCHAR(50) NOT NULL UNIQUE,
                          hash_password  VARCHAR(255) NOT NULL,
                          rol            ENUM('ADMIN','DOCENTE','ALUMNO') NOT NULL,
                          habilitado     BOOLEAN       NOT NULL DEFAULT TRUE,
                          creado_en      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          actualizado_en DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3) Alumnos
CREATE TABLE alumnos (
                         id               INT AUTO_INCREMENT PRIMARY KEY,
                         usuario_id       INT          NOT NULL UNIQUE,
                         nombre           VARCHAR(50)  NOT NULL,
                         apellido         VARCHAR(50)  NOT NULL,
                         dni              VARCHAR(20)  UNIQUE,
                         correo           VARCHAR(100),
                         fecha_nac        DATE,
                         genero           ENUM('F','M','Otro'),
                         habilitado       BOOLEAN      NOT NULL DEFAULT TRUE,
                         FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- 4) Docentes
CREATE TABLE docentes (
                          id               INT AUTO_INCREMENT PRIMARY KEY,
                          usuario_id       INT          NOT NULL UNIQUE,
                          nombre           VARCHAR(50)  NOT NULL,
                          apellido         VARCHAR(50)  NOT NULL,
                          legajo           VARCHAR(20)  UNIQUE,
                          correo           VARCHAR(100),
                          habilitado       BOOLEAN      NOT NULL DEFAULT TRUE,
                          FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- 5) Carreras y planes de estudio
CREATE TABLE carreras (
                          id          INT AUTO_INCREMENT PRIMARY KEY,
                          nombre      VARCHAR(100) NOT NULL,
                          descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE planes_estudio (
                                id          INT AUTO_INCREMENT PRIMARY KEY,
                                carrera_id  INT          NOT NULL,
                                nombre      VARCHAR(100) NOT NULL,
                                FOREIGN KEY (carrera_id) REFERENCES carreras(id)
) ENGINE=InnoDB;

-- 6) Materias y correlatividades
CREATE TABLE materias (
                          id               INT AUTO_INCREMENT PRIMARY KEY,
                          plan_id          INT          NOT NULL,
                          nombre           VARCHAR(100) NOT NULL,
                          anio             TINYINT      NOT NULL,
                          cuatrimestre     TINYINT      NOT NULL,
                          creditos         SMALLINT     NOT NULL DEFAULT 0,
                          FOREIGN KEY (plan_id) REFERENCES planes_estudio(id)
) ENGINE=InnoDB;

CREATE TABLE correlatividades (
                                  materia_id      INT NOT NULL,
                                  correlativa_id  INT NOT NULL,
                                  PRIMARY KEY (materia_id, correlativa_id),
                                  FOREIGN KEY (materia_id)     REFERENCES materias(id),
                                  FOREIGN KEY (correlativa_id) REFERENCES materias(id)
) ENGINE=InnoDB;

-- 7) Inscripciones (Alumnos → Materias)
CREATE TABLE inscripciones (
                               id              INT AUTO_INCREMENT PRIMARY KEY,
                               alumno_id       INT NOT NULL,
                               materia_id      INT NOT NULL,
                               fecha_insc      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               estado          ENUM('ACTIVA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
                               FOREIGN KEY (alumno_id)  REFERENCES alumnos(id),
                               FOREIGN KEY (materia_id) REFERENCES materias(id)
) ENGINE=InnoDB;

-- 8) Calificaciones (por inscripciones, cargadas por docentes)
CREATE TABLE calificaciones (
                                id               INT AUTO_INCREMENT PRIMARY KEY,
                                inscripcion_id   INT          NOT NULL,
                                docente_id       INT          NOT NULL,
                                nota             DECIMAL(4,2) NOT NULL CHECK (nota BETWEEN 0 AND 10),
                                fecha_carga      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                observaciones    TEXT,
                                FOREIGN KEY (inscripcion_id) REFERENCES inscripciones(id),
                                FOREIGN KEY (docente_id)     REFERENCES docentes(id)
) ENGINE=InnoDB;

-- 9) Usuario de prueba: "admin" / "1234" (bcrypt)
INSERT INTO usuarios(username, hash_password, rol)
VALUES (
           'admin',
           '1234',
           'ADMIN'
       );
ALTER TABLE docentes
    ADD COLUMN dni VARCHAR(15) NOT NULL,
ADD COLUMN genero VARCHAR(10),
ADD COLUMN fecha_nacimiento DATE;

SELECT * FROM docentes;

describe docentes;
DELETE FROM usuarios WHERE username = 'zeuci';
