CREATE DATABASE parquimetros;
USE parquimetros;

/* ENTIDADES */

CREATE TABLE conductores (
    dni INT UNSIGNED NOT NULL,
    nombre VARCHAR(25) NOT NULL,
    apellido VARCHAR(25) NOT NULL,
    registro INT UNSIGNED NOT NULL,
    direccion VARCHAR(25) NOT NULL,
    telefono VARCHAR(15),

    CONSTRAINT pk_conductores 
        PRIMARY KEY (dni)
) ENGINE = InnoDB;

CREATE TABLE automoviles (
    patente CHAR(6) NOT NULL,
    marca VARCHAR(20) NOT NULL,
    modelo VARCHAR(20) NOT NULL,
    color VARCHAR(15) NOT NULL,
    dni INT UNSIGNED NOT NULL,

    CONSTRAINT pk_automoviles 
        PRIMARY KEY (patente),
    CONSTRAINT fk_automovil_conductor 
        FOREIGN KEY (dni) REFERENCES conductores (dni)        
) ENGINE = InnoDB;

CREATE TABLE tipos_tarjeta (
    tipo VARCHAR(25) NOT NULL,
    descuento DECIMAL(3, 2) UNSIGNED NOT NULL
        CHECK (descuento >= 0 AND descuento <= 1),

    CONSTRAINT pk_tipos_tarjeta PRIMARY KEY (tipo)
) ENGINE = InnoDB;

CREATE TABLE tarjetas (
    id_tarjeta  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    patente CHAR(6) NOT NULL,
    tipo VARCHAR(25) NOT NULL,
    saldo DECIMAL(5,2) NOT NULL,

    CONSTRAINT pk_tarjetas 
        PRIMARY KEY (id_tarjeta),
    CONSTRAINT fk_tarjeta_automovil
        FOREIGN KEY (patente) REFERENCES automoviles (patente),
    CONSTRAINT fk_tarjeta_tipo_tarjeta
        FOREIGN KEY (tipo) REFERENCES tipos_tarjeta (tipo)
) ENGINE = InnoDB;

CREATE TABLE recargas (
    id_tarjeta  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    saldo_anterior DECIMAL(5,2) NOT NULL,
    saldo_posterior DECIMAL(5,2) NOT NULL,

    CONSTRAINT pk_recargas
        PRIMARY KEY (id_tarjeta, fecha, hora),
    CONSTRAINT fk_recarga_tarjeta
        FOREIGN KEY (id_tarjeta) REFERENCES tarjetas (id_tarjeta)
) ENGINE = InnoDB;

CREATE TABLE inspectores (
    legajo INT UNSIGNED NOT NULL,
    dni INT UNSIGNED NOT NULL,
    nombre VARCHAR(25) NOT NULL,
    apellido VARCHAR(25) NOT NULL,
    password VARCHAR(32) NOT NULL,

    CONSTRAINT pk_inspector
        PRIMARY KEY (legajo)
) ENGINE = InnoDB;

CREATE TABLE ubicaciones (
    calle VARCHAR(25) NOT NULL,
    altura INT UNSIGNED NOT NULL,
    tarifa DECIMAL(5,2) UNSIGNED NOT NULL,

    CONSTRAINT pk_ubicacion
        PRIMARY KEY (calle, altura)
) ENGINE = InnoDB;

CREATE TABLE parquimetros (
    id_parq INT UNSIGNED NOT NULL,
    numero INT UNSIGNED NOT NULL,
    calle VARCHAR(25) NOT NULL,
    altura INT UNSIGNED NOT NULL,

    CONSTRAINT pk_parquimetro
        PRIMARY KEY (id_parq),
    CONSTRAINT fk_parquimetro_ubicacion
        FOREIGN KEY (calle, altura) REFERENCES ubicaciones (calle, altura)
) ENGINE = InnoDB;

/* RELACIONES */

CREATE TABLE estacionamientos (
    id_parq INT UNSIGNED NOT NULL,
    fecha_ent DATE NOT NULL,
    hora_ent TIME NOT NULL,
    id_tarjeta  INT UNSIGNED NOT NULL,
    fecha_sal DATE,
    hora_sal TIME,

    CONSTRAINT pk_estacionamientos
        PRIMARY KEY (id_parq, fecha_ent, hora_ent),
    CONSTRAINT fk_estacionamientos_tarjeta
        FOREIGN KEY (id_tarjeta) REFERENCES tarjetas (id_tarjeta),
    CONSTRAINT fk_estacionamientos_parquimetro
        FOREIGN KEY (id_parq) REFERENCES parquimetros (id_parq)
) ENGINE=InnoDB;

CREATE TABLE  accede (
    id_parq INT UNSIGNED NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    legajo INT UNSIGNED NOT NULL,

    CONSTRAINT pk_accede
        PRIMARY KEY (id_parq, fecha, hora),
    CONSTRAINT fk_accede_parquimetro
        FOREIGN KEY (id_parq) REFERENCES parquimetros (id_parq),
    CONSTRAINT fk_accede_inspector
        FOREIGN KEY (legajo) REFERENCES inspectores (legajo)
) ENGINE = InnoDB;

CREATE TABLE asociado_con (
    id_asociado_con INT UNSIGNED NOT NULL AUTO_INCREMENT,
    legajo INT UNSIGNED NOT NULL,
    calle VARCHAR(25) NOT NULL,
    altura INT UNSIGNED NOT NULL,
    dia ENUM('do', 'lu', 'ma', 'mi', 'ju', 'vi', 'sa') NOT NULL,
    turno ENUM('m', 't') NOT NULL,

    CONSTRAINT pk_asociado_con
        PRIMARY KEY (id_asociado_con),
    CONSTRAINT fk_asociado_con_inspector
        FOREIGN KEY (legajo) REFERENCES inspectores (legajo),
    CONSTRAINT fk_asociado_con_ubicacion
        FOREIGN KEY (calle, altura) REFERENCES ubicaciones (calle, altura)
) ENGINE = InnoDB;

CREATE TABLE multa (
    numero INT UNSIGNED NOT NULL AUTO_INCREMENT,
    patente CHAR(6) NOT NULL,
    id_asociado_con INT UNSIGNED  NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,

    CONSTRAINT pk_multa
        PRIMARY KEY (numero),
    CONSTRAINT fk_multa_automovil
        FOREIGN KEY (patente) REFERENCES automoviles (patente),
    CONSTRAINT fk_multa_id_asociado_con
        FOREIGN KEY (id_asociado_con) REFERENCES asociado_con (id_asociado_con)
) ENGINE = InnoDB;

/* VISTAS */

CREATE VIEW estacionados AS
SELECT P.calle, P.altura, Es.patente, Es.fecha_ent, Es.hora_ent
FROM parquimetros P
NATURAL JOIN
(        
    SELECT E.id_parq, T.id_tarjeta, T.patente, E.fecha_ent, E.hora_ent
    FROM tarjetas T
    NATURAL JOIN estacionamientos E
    WHERE E.fecha_sal IS NULL AND E.hora_sal IS NULL
) Es;

/* TRIGGER */

delimiter !
CREATE TRIGGER crate_recargas
AFTER UPDATE ON tarjetas
FOR EACH ROW
BEGIN
    IF NEW.saldo > OLD.saldo THEN
        INSERT INTO recargas VALUES (NEW.id_tarjeta, CURDATE(), CURTIME(), OLD.saldo, NEW.saldo);
    END IF;
END; !

delimiter ;

/* USUARIOS */

/* User admin */
CREATE USER 'admin'@'localhost'  IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON parquimetros.* TO 'admin'@'localhost' WITH GRANT OPTION;

/* User parquiemtro */
CREATE USER 'parquimetro'@'%'  IDENTIFIED BY 'parq';
GRANT ALL PRIVILEGES ON parquimetros.* TO 'parquimetro'@'%' WITH GRANT OPTION;

/* User venta */
CREATE USER 'venta'@'%'  IDENTIFIED BY 'venta';
GRANT SELECT, UPDATE, INSERT ON parquimetros.tarjetas TO 'venta'@'%';
GRANT SELECT ON parquimetros.tipos_tarjeta TO 'venta'@'%';
GRANT INSERT ON parquimetros.recargas TO 'venta'@'%';

/* User inspector */
CREATE USER 'inspector'@'%'  IDENTIFIED BY 'inspector';
GRANT SELECT ON parquimetros.inspectores TO 'inspector'@'%';
GRANT SELECT (patente) ON parquimetros.automoviles TO 'inspector'@'%';
GRANT SELECT ON parquimetros.estacionados TO 'inspector'@'%';
GRANT INSERT ON parquimetros.multa TO 'inspector'@'%';
GRANT SELECT, INSERT ON parquimetros.accede TO 'inspector'@'%';
GRANT SELECT ON parquimetros.parquimetros TO 'inspector'@'%';
GRANT SELECT ON parquimetros.asociado_con TO 'inspector'@'%';
GRANT SELECT ON parquimetros.ubicaciones TO 'inspector'@'%';