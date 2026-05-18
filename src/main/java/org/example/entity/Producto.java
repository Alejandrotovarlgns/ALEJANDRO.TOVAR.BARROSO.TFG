package org.example.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "productos")
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String marca;
    private Double precio;
    private Integer stock;
    private String talla;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
}
