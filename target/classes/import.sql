INSERT INTO categorias (nombre) VALUES ('Running');
INSERT INTO categorias (nombre) VALUES ('Casual');
INSERT INTO categorias (nombre) VALUES ('Baloncesto');
INSERT INTO usuarios (usuario, password, rol) VALUES ('admin', '$2a$10$NpbqI8XzysB.2DbiKWLg5.fGnfwXNbapXMYSkof5.IU3MFMYEkDXK', 'ADMIN');
INSERT INTO usuarios (usuario, password, rol) VALUES ('usuario', '$2a$10$NpbqI8XzysB.2DbiKWLg5.fGnfwXNbapXMYSkof5.IU3MFMYEkDXK', 'USER');


-- Las Nike Air Max (Casual -> ID 2)
INSERT INTO productos (nombre, marca, precio, talla, stock, id_categoria) VALUES ('Nike Air Max', 'Nike', 120.99, '42', 10, 2);
-- Las Adidas Ultraboost (Running -> ID 1)
INSERT INTO productos (nombre, marca, precio, talla, stock, id_categoria) VALUES ('Adidas Ultraboost', 'Adidas', 180.50, '44', 3, 1);
-- Las New Balance 550 (Casual -> ID 2)
INSERT INTO productos (nombre, marca, precio, talla, stock, id_categoria) VALUES ('New Balance 550', 'New Balance', 110.00, '41', 15, 2);

