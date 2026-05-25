package org.example.repository;

import org.example.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {


    List<Producto> findByNombre(String nombre);
}