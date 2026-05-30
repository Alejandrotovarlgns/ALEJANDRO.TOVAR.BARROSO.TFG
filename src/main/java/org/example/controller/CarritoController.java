package org.example.controller;

import org.example.entity.ItemCarrito;
import org.example.entity.Producto;
import org.example.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private ProductoService productoService;

    // Obtener o inicializar el carrito en la sesión del usuario
    private List<ItemCarrito> obtenerCarrito(HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    // 1. Ver el contenido del carrito
    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        double total = carrito.stream().mapToDouble(ItemCarrito::getSubtotal).sum();

        model.addAttribute("items", carrito);
        model.addAttribute("total", total);
        return "carrito";
    }

    // 2. Añadir un producto con su talla seleccionada al carrito
    @PostMapping("/add")
    public String añadirAlCarrito(@RequestParam("tallaId") Integer tallaId, HttpSession session) {
        Producto producto = productoService.obtenerPorId(tallaId);

        if (producto != null && producto.getStock() > 0) {
            List<ItemCarrito> carrito = obtenerCarrito(session);

            // Si el producto con esa talla exacta ya estaba en el carrito, le sumamos 1 a la cantidad
            boolean existe = false;
            for (ItemCarrito item : carrito) {
                if (item.getProducto().getId().equals(tallaId)) {
                    item.setCantidad(item.getCantidad() + 1);
                    existe = true;
                    break;
                }
            }

            // Si es nuevo en el carrito, lo agregamos
            if (!existe) {
                carrito.add(new ItemCarrito(producto, 1));
            }
        }
        return "redirect:/inventario?añadidoAlCarrito=true";
    }

    // 3. Procesar la compra de TODO el carrito de golpe (Resta de stock real)
    @PostMapping("/comprar")
    public String procesarCompra(HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarrito(session);

        if (carrito.isEmpty()) {
            return "redirect:/carrito?errorVacio=true";
        }

        // Primero verificamos que haya stock suficiente para TODOS los artículos elegidos
        for (ItemCarrito item : carrito) {
            Producto prodReal = productoService.obtenerPorId(item.getProducto().getId());
            if (prodReal == null || prodReal.getStock() < item.getCantidad()) {
                return "redirect:/carrito?errorStock=" + item.getProducto().getNombre();
            }
        }

        // Si todo está correcto, restamos el stock en la base de datos MySQL
        for (ItemCarrito item : carrito) {
            Producto prodReal = productoService.obtenerPorId(item.getProducto().getId());
            prodReal.setStock(prodReal.getStock() - item.getCantidad());
            productoService.guardar(prodReal);
        }

        // Vaciamos el carrito de la sesión tras la compra exitosa
        session.removeAttribute("carrito");

        return "redirect:/inventario?compraExitosa=true";
    }

    // 4. Vaciar el carrito de forma manual o quitar un elemento
    @GetMapping("/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable("id") Integer id, HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        carrito.removeIf(item -> item.getProducto().getId().equals(id));
        return "redirect:/carrito";
    }
}