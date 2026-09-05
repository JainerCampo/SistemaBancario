CREATE DATABASE IF NOT EXISTS danibanca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE danibanca;

CREATE TABLE IF NOT EXISTS clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_identificacion ENUM('CC', 'TI', 'PAS') NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cuentas (
    numero_cuenta BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    tipo_cuenta ENUM('AHORROS', 'CORRIENTE') NOT NULL,
    saldo DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    estado ENUM('ACTIVO', 'INACTIVO', 'BLOQUEADO', 'SUSPENDIDO', 'CANCELADO') DEFAULT 'ACTIVO',
    fecha_apertura DATE NOT NULL,
    tasa_interes DECIMAL(5, 2) NULL,
    limite_sobregiro DECIMAL(18, 2) NULL,
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    INDEX idx_cuentas_cliente (cliente_id),
    INDEX idx_cuentas_estado (estado)
) ENGINE = InnoDB;

ALTER TABLE cuentas AUTO_INCREMENT = 1000000000;

CREATE TABLE IF NOT EXISTS movimientos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT UNSIGNED NOT NULL,
    tipo_movimiento ENUM('CONSIGNACION', 'RETIRO', 'TRANSFERENCIA_ENVIADA', 'TRANSFERENCIA_RECIBIDA', 'INTERESES_GENERADOS') NOT NULL,
    canal ENUM('CAJERO', 'TRANSFERENCIA', 'VENTANILLA', 'CORRESPONSAL_BANCARIO', 'SISTEMA') NOT NULL,
    monto DECIMAL(18, 2) NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cuenta_origen VARCHAR(20) NULL,
    cuenta_destino VARCHAR(20) NULL,
    CONSTRAINT fk_movimientos_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuentas(numero_cuenta),
    INDEX idx_movimientos_cuenta (cuenta_id),
    INDEX idx_movimientos_fecha (fecha)
) ENGINE = InnoDB;

-- Datos de prueba: 10 clientes, una cuenta y dos movimientos por cliente.
INSERT IGNORE INTO clientes (
    tipo_identificacion, documento, nombres, apellidos, fecha_nacimiento,
    direccion, telefono, correo
) VALUES
    ('CC', '1001001001', 'Ana', 'Martinez', '1988-04-12', 'Carrera 10 # 20-30', '3001001001', 'ana.martinez@example.com'),
    ('CC', '1001001002', 'Bruno', 'Ramirez', '1979-09-03', 'Calle 15 # 8-44', '3001001002', 'bruno.ramirez@example.com'),
    ('TI', '1001001003', 'Carla', 'Gomez', '2002-02-18', 'Avenida 6 # 45-12', '3001001003', 'carla.gomez@example.com'),
    ('PAS', '1001001004', 'Diego', 'Torres', '1985-11-27', 'Carrera 50 # 12-09', '3001001004', 'diego.torres@example.com'),
    ('CC', '1001001005', 'Elena', 'Rojas', '1993-06-08', 'Calle 72 # 14-21', '3001001005', 'elena.rojas@example.com'),
    ('CC', '1001001006', 'Fabian', 'Castro', '1990-01-25', 'Carrera 7 # 80-15', '3001001006', 'fabian.castro@example.com'),
    ('TI', '1001001007', 'Gabriela', 'Moreno', '2001-07-19', 'Calle 30 # 18-22', '3001001007', 'gabriela.moreno@example.com'),
    ('PAS', '1001001008', 'Hector', 'Vargas', '1982-10-11', 'Avenida 19 # 100-08', '3001001008', 'hector.vargas@example.com'),
    ('CC', '1001001009', 'Isabel', 'Navarro', '1996-03-14', 'Calle 9 # 32-16', '3001001009', 'isabel.navarro@example.com'),
    ('CC', '1001001010', 'Jorge', 'Mendoza', '1975-12-02', 'Carrera 22 # 55-40', '3001001010', 'jorge.mendoza@example.com');

INSERT IGNORE INTO cuentas (
    numero_cuenta, cliente_id, tipo_cuenta, saldo, estado, fecha_apertura,
    tasa_interes, limite_sobregiro
)
SELECT datos.numero_cuenta, clientes.id, datos.tipo_cuenta, datos.saldo,
       datos.estado, datos.fecha_apertura, datos.tasa_interes,
       datos.limite_sobregiro
FROM (
    SELECT 1000000000 AS numero_cuenta, '1001001001' AS documento, 'AHORROS' AS tipo_cuenta, 1250000.00 AS saldo, 'ACTIVO' AS estado, '2026-01-10' AS fecha_apertura, 2.50 AS tasa_interes, NULL AS limite_sobregiro
    UNION ALL SELECT 1000000001, '1001001002', 'CORRIENTE', 2300000.00, 'ACTIVO', '2026-01-11', NULL, 1000000.00
    UNION ALL SELECT 1000000002, '1001001003', 'AHORROS', 800000.00, 'BLOQUEADO', '2026-01-12', 1.80, NULL
    UNION ALL SELECT 1000000003, '1001001004', 'CORRIENTE', 1450000.00, 'ACTIVO', '2026-01-13', NULL, 500000.00
    UNION ALL SELECT 1000000004, '1001001005', 'AHORROS', 3200000.00, 'ACTIVO', '2026-01-14', 3.00, NULL
    UNION ALL SELECT 1000000005, '1001001006', 'CORRIENTE', 950000.00, 'SUSPENDIDO', '2026-01-15', NULL, 750000.00
    UNION ALL SELECT 1000000006, '1001001007', 'AHORROS', 610000.00, 'ACTIVO', '2026-01-16', 2.20, NULL
    UNION ALL SELECT 1000000007, '1001001008', 'CORRIENTE', 4100000.00, 'ACTIVO', '2026-01-17', NULL, 1500000.00
    UNION ALL SELECT 1000000008, '1001001009', 'AHORROS', 1750000.00, 'ACTIVO', '2026-01-18', 2.75, NULL
    UNION ALL SELECT 1000000009, '1001001010', 'CORRIENTE', 500000.00, 'CANCELADO', '2026-01-19', NULL, 300000.00
) AS datos
JOIN clientes ON clientes.documento = datos.documento;

INSERT INTO movimientos (
    cuenta_id, tipo_movimiento, canal, monto, fecha, hora, descripcion,
    cuenta_origen, cuenta_destino
)
SELECT datos.cuenta_id, datos.tipo_movimiento, datos.canal, datos.monto,
       datos.fecha, datos.hora, datos.descripcion, datos.cuenta_origen,
       datos.cuenta_destino
FROM (
    SELECT 1000000000 AS cuenta_id, 'CONSIGNACION' AS tipo_movimiento, 'VENTANILLA' AS canal, 1500000.00 AS monto, '2026-01-10' AS fecha, '09:15:00' AS hora, 'Aporte inicial de ahorro' AS descripcion, NULL AS cuenta_origen, NULL AS cuenta_destino
    UNION ALL SELECT 1000000000, 'RETIRO', 'CAJERO', 250000.00, '2026-01-20', '14:30:00', 'Retiro para gastos mensuales', NULL, NULL
    UNION ALL SELECT 1000000001, 'CONSIGNACION', 'VENTANILLA', 3000000.00, '2026-01-11', '10:00:00', 'Consignacion de nomina', NULL, NULL
    UNION ALL SELECT 1000000001, 'RETIRO', 'CAJERO', 700000.00, '2026-01-21', '16:10:00', 'Pago de obligaciones', NULL, NULL
    UNION ALL SELECT 1000000002, 'CONSIGNACION', 'CORRESPONSAL_BANCARIO', 1000000.00, '2026-01-12', '11:20:00', 'Ahorro mensual', NULL, NULL
    UNION ALL SELECT 1000000002, 'RETIRO', 'CAJERO', 200000.00, '2026-01-22', '08:45:00', 'Retiro de efectivo', NULL, NULL
    UNION ALL SELECT 1000000003, 'CONSIGNACION', 'TRANSFERENCIA', 2000000.00, '2026-01-13', '12:05:00', 'Transferencia recibida', '1000000007', '1000000003'
    UNION ALL SELECT 1000000003, 'RETIRO', 'VENTANILLA', 550000.00, '2026-01-23', '13:40:00', 'Retiro en ventanilla', NULL, NULL
    UNION ALL SELECT 1000000004, 'CONSIGNACION', 'VENTANILLA', 4000000.00, '2026-01-14', '09:35:00', 'Aporte de inversion', NULL, NULL
    UNION ALL SELECT 1000000004, 'INTERESES_GENERADOS', 'SISTEMA', 200000.00, '2026-01-31', '23:59:00', 'Intereses generados del mes', NULL, NULL
    UNION ALL SELECT 1000000005, 'CONSIGNACION', 'CORRESPONSAL_BANCARIO', 1200000.00, '2026-01-15', '10:50:00', 'Consignacion de salario', NULL, NULL
    UNION ALL SELECT 1000000005, 'RETIRO', 'CAJERO', 250000.00, '2026-01-24', '17:25:00', 'Retiro usando sobregiro', NULL, NULL
    UNION ALL SELECT 1000000006, 'CONSIGNACION', 'TRANSFERENCIA', 750000.00, '2026-01-16', '15:15:00', 'Transferencia de ahorro', '1000000008', '1000000006'
    UNION ALL SELECT 1000000006, 'RETIRO', 'CAJERO', 140000.00, '2026-01-25', '09:10:00', 'Retiro de efectivo', NULL, NULL
    UNION ALL SELECT 1000000007, 'CONSIGNACION', 'VENTANILLA', 5000000.00, '2026-01-17', '11:45:00', 'Consignacion empresarial', NULL, NULL
    UNION ALL SELECT 1000000007, 'RETIRO', 'TRANSFERENCIA', 900000.00, '2026-01-26', '14:05:00', 'Pago a proveedor', '1000000007', '1000000003'
    UNION ALL SELECT 1000000008, 'CONSIGNACION', 'VENTANILLA', 2000000.00, '2026-01-18', '08:30:00', 'Ahorro programado', NULL, NULL
    UNION ALL SELECT 1000000008, 'RETIRO', 'CAJERO', 250000.00, '2026-01-27', '18:00:00', 'Retiro para compras', NULL, NULL
    UNION ALL SELECT 1000000009, 'CONSIGNACION', 'VENTANILLA', 800000.00, '2026-01-19', '13:15:00', 'Fondo inicial', NULL, NULL
    UNION ALL SELECT 1000000009, 'RETIRO', 'CAJERO', 300000.00, '2026-01-28', '10:25:00', 'Retiro de fondos', NULL, NULL
) AS datos
WHERE EXISTS (SELECT 1 FROM cuentas WHERE cuentas.numero_cuenta = datos.cuenta_id);
