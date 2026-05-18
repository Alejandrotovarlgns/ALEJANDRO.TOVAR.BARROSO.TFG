package org.example.service;

import org.example.entity.Producto;
import org.example.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // LEER (Listar todos)
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // CREAR o ACTUALIZAR
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    // BORRAR
    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }

    // BUSCAR por ID (Para editar)
    public Producto obtenerPorId(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }
}