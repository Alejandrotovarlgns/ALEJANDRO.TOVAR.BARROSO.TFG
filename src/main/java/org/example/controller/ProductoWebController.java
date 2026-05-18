package org.example.controller;

import org.example.entity.Producto;
import org.example.service.ProductoService;
import org.example.service.CategoriaService; // Importamos el nuevo servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class ProductoWebController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService; // Inyectamos el servicio de categorIas

    @GetMapping("/inventario")
    public String listar(Model model) {
        List<Producto> todosLosProductos = productoService.obtenerTodos();

        // Filtramos para obtener solo un modelo Unico por combinaciOn de Marca y Nombre (evita duplicar tarjetas)
        List<Producto> listaAgrupada = todosLosProductos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        p -> (p.getMarca() + "-" + p.getNombre()).toLowerCase(),
                        p -> p,
                        (existente, reemplazo) -> existente // Si se repite el modelo, se queda con la primera ocurrencia
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        // 'lista' sirve para pintar las tarjetas Unicas en el catAlogo principal
        model.addAttribute("lista", listaAgrupada);

        // 'listaCompleta' sirve para que la tabla de la tarjeta desglose todas las tallas y stocks reales
        model.addAttribute("listaCompleta", todosLosProductos);

        return "inventario";
    }

    // Muestra el formulario para crear un nuevo producto enviando los existentes y las categorIas
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());

        // Enviamos la lista de zapatillas actuales para poder ver el stock por talla y autocompletar
        List<Producto> productosExistentes = productoService.obtenerTodos();
        model.addAttribute("productosExistentes", productosExistentes);

        // Enviamos las categorías reales de la base de datos para el nuevo desplegable
        model.addAttribute("categorias", categoriaService.obtenerTodas());

        return "formulario-producto";
    }

    // Guarda el producto (con lOgica de suma de stock corregida y robusta)
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("producto") Producto producto) {

        // Limpiamos los espacios en blanco que puedan venir del formulario para que la comparación sea exacta
        if (producto.getNombre() != null) producto.setNombre(producto.getNombre().trim());
        if (producto.getMarca() != null) producto.setMarca(producto.getMarca().trim());
        if (producto.getTalla() != null) producto.setTalla(producto.getTalla().trim());

        // Si es un producto nuevo desde el formulario (id es null), comprobamos si ya existe el mismo modelo y talla
        if (producto.getId() == null) {
            List<Producto> todos = productoService.obtenerTodos();
            Producto productoRepetido = null;

            for (Producto p : todos) {
                if (p.getNombre() != null && p.getNombre().equalsIgnoreCase(producto.getNombre()) &&
                        p.getMarca() != null && p.getMarca().equalsIgnoreCase(producto.getMarca()) &&
                        p.getTalla() != null && p.getTalla().equalsIgnoreCase(producto.getTalla())) {

                    productoRepetido = p;
                    break;
                }
            }

            // SI YA EXISTE ESA TALLA: Sumamos el stock al registro existente
            if (productoRepetido != null) {
                int stockActual = productoRepetido.getStock() != null ? productoRepetido.getStock() : 0;
                int stockNuevo = producto.getStock() != null ? producto.getStock() : 0;

                productoRepetido.setStock(stockActual + stockNuevo);

                // Actualizamos tambiEn la categorIa por si acaso se cambiO en el formulario
                if (producto.getCategoria() != null) {
                    productoRepetido.setCategoria(producto.getCategoria());
                }

                productoService.guardar(productoRepetido);
                return "redirect:/inventario";
            }
        }

        // SI ES EDICIOIN O UNA TALLA NUEVA EN DISTINTA LINEA: Se guarda normalmente creando un registro nuevo
        productoService.guardar(producto);
        return "redirect:/inventario";
    }

    // Muestra el formulario para editar incluyendo las categorIas
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model) {
        Producto p = productoService.obtenerPorId(id);
        model.addAttribute("producto", p);

        // TambiEn los pasamos aquI por si se necesita consultar mientras se edita
        model.addAttribute("productosExistentes", productoService.obtenerTodos());

        // Enviamos las categorIas aquI tambiEn para permitir cambiarla durante la ediciOn
        model.addAttribute("categorias", categoriaService.obtenerTodas());

        return "formulario-producto";
    }

    // Elimina el producto
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id) {
        productoService.eliminar(id);
        return "redirect:/inventario";
    }

    // --- NUEVO METODO PARA EL QR ---
    // Este mEtodo es pUblico (gracias al cambio en SecurityConfig)
    @GetMapping("/producto/detalle/{id}")
    public String verDetallePublico(@PathVariable("id") Integer id, Model model) {
        Producto p = productoService.obtenerPorId(id);
        model.addAttribute("p", p);
        return "detalle-producto";
    }
    // --- RUTA PARA MOSTRAR EL LOGIN PERSONALIZADO ---
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}