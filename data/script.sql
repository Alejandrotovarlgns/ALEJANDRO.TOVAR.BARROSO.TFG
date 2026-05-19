-- 1. Creamos la base de datos desde cero por si acaso
CREATE DATABASE IF NOT EXISTS empresa;
USE empresa;

-- 2. Limpieza de seguridad por si existen tablas corruptas
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS categorias;
SET FOREIGN_KEY_CHECKS = 1;

-- 3. Crear tabla categorias
CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255)
);

-- 4. Crear tabla usuarios (con los campos exactos que Hibernate te generó: 'usuario' y 'rol')
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(255) NOT NULL
);

-- 5. Crear tabla productos
CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(255),
    nombre VARCHAR(255),
    precio DOUBLE,
    stock INT,
    id_categoria INT,
    CONSTRAINT fk_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id)
);

-- 6. Insertamos las categorías base
INSERT INTO categorias (nombre) VALUES ('Running'), ('Casual'), ('Basket'), ('Training'), ('Skate');

-- 7. Insertamos los usuarios con las contraseñas CIFRADAS en BCrypt
-- Admin (pass: admin123) -> Con el rol ADMIN limpio
INSERT INTO usuarios (usuario, nombre, password, rol) VALUES
('admin', 'Administrador', '$2a$10$8.UnVuG9HHgffUDAlk8q6Ou5HEMF6vYZPuRQCAp.rz20EnrG46DDe', 'ROLE_ADMIN');

-- Operario (pass: user123) -> Con el rol USER limpio
INSERT INTO usuarios (usuario, nombre, password, rol) VALUES
('operario', 'Operario', '$2a$10$Vp9Z8.0P6fD/6pWvH8Y4ueYV.S/lY6wKkXG0iWqD9.X6H2Y2rXf7G', 'ROLE_USER');

-- 8. Insertamos productos iniciales
INSERT INTO productos (nombre, marca, precio, stock, id_categoria) VALUES
('New Balance 550', 'New Balance', 120.00, 15, 2),
('Nike Air Jordan 1', 'Nike', 180.00, 8, 3),
('Adidas Ultraboost', 'Adidas', 190.00, 20, 1),
('Vans Old Skool', 'Vans', 75.00, 30, 5);


USE empresa;

-- Actualizamos los dos usuarios con el hash exacto de la contraseña '1234'
UPDATE usuarios
SET password = '$2a$10$NpbqI8XzysB.2DbiKWLg5.fGnfwXNbapXMYSkof5.IU3MFMYEkDXK'
WHERE usuario IN ('admin', 'operario');
USE empresa;

-- Actualizamos el rol de admin para que lleve el prefijo que busca Spring Security
UPDATE usuarios SET rol = 'ROLE_ADMIN' WHERE usuario = 'admin';

-- Actualizamos el rol de operario para que lleve el prefijo correspondiente
UPDATE usuarios SET rol = 'ROLE_USER' WHERE usuario = 'operario';

USE empresa;

-- Dejamos los roles limpios para que Spring los procese de forma nativa
UPDATE usuarios SET rol = 'ADMIN' WHERE usuario = 'admin';
UPDATE usuarios SET rol = 'USER' WHERE usuario = 'operario';
USE empresa;

-- 1. Añadimos la columna talla a la tabla productos que ya existe
ALTER TABLE productos ADD COLUMN talla VARCHAR(50);

USE empresa;

-- Actualizamos las tallas de forma segura usando el ID de cada producto
UPDATE productos SET talla = '42' WHERE id = 1;
UPDATE productos SET talla = '44' WHERE id = 2;
UPDATE productos SET talla = '41' WHERE id = 3;
UPDATE productos SET talla = '43' WHERE id = 4;

ALTER TABLE productos ADD COLUMN consultas INT DEFAULT 0;