package org.example.entity;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;

    public ItemCarrito() {}

    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    // Métodos útiles para calcular el total de este producto
    public double getSubtotal() {
        if (producto != null && producto.getPrecio() != null) {
            return producto.getPrecio() * cantidad;
        }
        return 0.0;
    }

    // Getters y Setters
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}