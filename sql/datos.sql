/* CARGA DATOS DE PREUBA */

START TRANSACTION;

INSERT INTO conductores
VALUES 
    (12345678, 'Juan', 'Perez', 987654, 'Calle A 123', '123456789'),
    (23456789, 'Maria', 'Gomez', 876543, 'Calle B 456', '987654321'),
    (34567890, 'Carlos', 'Lopez', 765432, 'Calle C 789', '456789012'),
    (45678901, 'Laura', 'Martinez', 654321, 'Calle D 012', '321098765'),
    (56789012, 'Pedro', 'Rodriguez', 543210, 'Calle E 345', '210987654'),
    (43000000, 'Agustín', 'Oyarzún', 130411, 'Avenida Alem 1253', '291234567'),
    (44598123, 'Marcia', 'Ana', 223583, '12 de Octubre 630', '2928478304'),
    (30328345, 'Mario', 'Neta', 042069, 'Mitre 834', '2934566789'),
    (19106712, 'Armando Esteban', 'Quito', 131304, 'Avenida Alem 1015', '2914566543'),
    (23712364, 'Dolores', 'De Cuello', 849204, 'Calle A 129', '2919878912'),
    (27182818, 'Leonhard', 'Euler', 284590, 'Calle E 271', '4523536028');

INSERT INTO automoviles
VALUES 
    ('ABC123', 'Toyota', 'Corolla', 'Azul', 12345678),
    ('DEF456', 'Ford', 'Focus', 'Rojo', 23456789),
    ('EUL271', 'Ford', 'Fiesta', 'Verde', 27182818),
    ('FEO145', 'Citroen', 'C4', 'Naranja', 30328345),
    ('GHI789', 'Honda', 'Civic', 'Blanco', 34567890),
    ('JKL012', 'Chevrolet', 'Cruze', 'Negro', 45678901),
    ('LOA134', 'Nissan', 'Altima', 'Blanco', 27182818),
    ('MNO345', 'Nissan', 'Altima', 'Gris', 56789012),
    ('OMG134', 'BMW', 'Z4', 'Rojo', 30328345),
    ('RIV912', 'DeLorean', 'DMC', 'Plateado', 43000000),
    ('UFO313', 'OVNI', 'J4Z', 'Plateado', 44598123),
    ('WOA758', 'Volkswagen', 'Fusca', 'Celeste', 23712364),
    ('WTH987', 'Volkswagen', 'Amarok', 'Azul', 30328345);    

INSERT INTO tipos_tarjeta
VALUES
    ('estudiantil', 0.15),
    ('pensionados', 0.30),
    ('general', 0.00);

INSERT INTO tarjetas (patente, tipo, saldo)
VALUES
    ('ABC123', 'general', 100.00),
    ('DEF456', 'pensionados', 400.00),
    ('GHI789', 'pensionados', 255.00),
    ('JKL012', 'estudiantil', 10.00),
    ('MNO345', 'estudiantil', 123.50),
    ('RIV912', 'pensionados', 912.18),
    ('WTH987', 'general', 500.00),
    ('WTH987', 'general', 00.20),
    ('OMG134', 'general', 679.34),
    ('EUL271', 'estudiantil', 003.14),
    ('EUL271', 'estudiantil', 340.18),
    ('LOA134', 'estudiantil', 500.25);

INSERT INTO recargas 
VALUES
    (2, '2023-09-6', '15:00:00', 400.00, 700.00),
    (4, '2023-09-7', '18:00:00', 255.00, 500.00),
    (6, '2023-08-10', '18:50:21', 000.18, 300.18),
    (6, '2023-09-13', '12:37:56', 012.18, 912.18);

INSERT INTO inspectores
VALUES 
    (1001, 11111111, 'Carlos', 'González', md5('c4rl05P@ss')),
    (1002, 22222222, 'Laura', 'Rodríguez',  md5('l@urit4P@ss')),
    (1003, 33333333, 'Diego', 'López',  md5('d13g0P@ss')),
    (1004, 44444444, 'Sofía', 'Martínez',  md5('s0f14P@ss')),
    (1005, 55555555, 'Lucas', 'Hernández',  md5('luc45P@ss')),
    (1006, 66666661, 'Elsa', 'Polindo',  md5('els4P@ss')),
    (8023, 30586789, 'Elmer', 'Curio',  md5('hgM3rcury')),
    (0873, 21899021, 'Zacarias', 'Flores',  md5('messiT3amo')),
    (1993, 30097123, 'Elvio', 'Linista',  md5('elvi0@pass')),
    (1008, 46699893, 'Ely', 'Minado',  md5('Sandia2020'));

INSERT INTO ubicaciones
VALUES 
    ('Avenida 1', 100, 3.50),
    ('Calle 2', 200, 2.75),
    ('Boulevard 3', 300, 4.25),
    ('Avenida Principal', 400, 3.00),
    ('Brown', 200, 5.00),
    ('Callejón 4', 500, 2.50),
    ('Avenida Arias', 2000, 2.00);

INSERT INTO asociado_con (legajo, calle, altura, dia, turno)
VALUES
    (1001,'Avenida 1', 100, 'lu', 'm'),
    (8023, 'Avenida 1', 100, 'lu', 't'),
    (1002,'Calle 2', 200, 'ma', 'm'),
    (1003,'Boulevard 3', 300, 'mi', 'm'),
    (1004,'Avenida Principal', 400, 'ju', 't'),
    (1005,'Callejón 4', 500, 'vi', 't'),
    (1008, 'Avenida Arias', 2000, 'sa', 'm'),
    (1008, 'Avenida Arias', 2000, 'sa', 't'),
    (1993, 'Boulevard 3', 300, 'sa', 't'),
    (1001, 'Calle 2', 200, 'mi', 't'),
    (1006, 'Brown', 200, 'vi', 'm'),
    (0873, 'Brown', 200, 'ma', 't'),
    (1006, 'Avenida Principal', 400, 'lu', 'm'),
    (1006, 'Callejón 4', 500, 'mi', 't'),
    (1008, 'Calle 2', 200, 'lu', 'm');

INSERT INTO parquimetros
VALUES 
    (1, 125, 'Avenida 1', 100),
    (3, 178, 'Avenida 1', 100),
    (4, 213, 'Calle 2', 200),
    (5, 290, 'Calle 2', 200),
    (6, 318, 'Boulevard 3', 300),
    (7, 340, 'Boulevard 3', 300),
    (8, 478, 'Avenida Principal', 400),
    (9, 415, 'Avenida Principal', 400),
    (10, 564, 'Callejón 4', 500),
    (11, 501, 'Callejón 4', 500),
    (22, 296, 'Brown', 200),
    (23, 204, 'Brown', 200),
    (67, 2003, 'Avenida Arias', 2000),
    (69, 2098, 'Avenida Arias', 2000);

INSERT INTO estacionamientos (id_parq, fecha_ent, hora_ent, id_tarjeta, fecha_sal, hora_sal) 
VALUES
    (5, '2023-09-14', '14:00:00', 3, '2023-09-14', '15:55:50'),
    (69, '2023-10-31', '17:55:43', 5, '2023-10-31', '20:30:13'),
    (1, '2023-09-14', '11:55:32', 7, '2023-09-14', '17:15:32'),
    (69, '2023-10-30', '17:47:01', 5, '2023-10-30', '20:32:54'),
    (10, '2023-09-13', '07:53:13', 9, '2023-09-14', '12:34:56'),
    (3, '2023-09-09', '10:11:44', 6, '2023-09-09', '13:50:08');

COMMIT;