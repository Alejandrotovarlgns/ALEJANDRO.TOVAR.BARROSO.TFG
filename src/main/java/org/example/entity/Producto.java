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

    // Añadimos columnDefinition para que la base de datos asigne 0 automáticamente si el script SQL no lo incluye
    @Column(name = "consultas", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer consultas = 0;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
}
