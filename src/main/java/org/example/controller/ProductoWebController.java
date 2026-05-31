package org.example.controller;

import org.example.entity.Producto;
import org.example.entity.Usuario;
import org.example.service.ProductoService;
import org.example.service.CategoriaService;
import org.example.service.ProductoPdfService;
import org.example.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class ProductoWebController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductoPdfService pdfService;

    @Autowired
    private UsuarioService usuarioService;

    // --- NUEVA RUTA RAÍZ: Redirige automáticamente al inventario para evitar el error 404 ---
    @GetMapping("/")
    public String raiz() {
        return "redirect:/inventario";
    }

    @GetMapping("/inventario")
    public String listar(Model model) {
        List<Producto> todosLosProductos = productoService.obtenerTodos();

        // Filtramos para obtener solo un modelo único por combinación de Marca y Nombre (evita duplicar tarjetas)
        List<Producto> listaAgrupada = todosLosProductos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        p -> (p.getMarca() + "-" + p.getNombre()).toLowerCase(),
                        p -> p,
                        (existente, reemplazo) -> existente
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        // 'lista' sirve para pintar las tarjetas únicas en el catálogo principal
        model.addAttribute("lista", listaAgrupada);

        // 'listaCompleta' sirve para que la tabla de la tarjeta desglose todas las tallas y stocks reales
        model.addAttribute("listaCompleta", todosLosProductos);

        return "inventario";
    }

    // Muestra el formulario para crear un nuevo producto enviando los existentes y las categorías
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

    // Guarda el producto (Soporta creación, edición o generación de nuevas variantes de talla)
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("producto") Producto producto) {

        // Limpiamos los espacios en blanco que puedan venir del formulario para que la comparación sea exacta
        if (producto.getNombre() != null) producto.setNombre(producto.getNombre().trim());
        if (producto.getMarca() != null) producto.setMarca(producto.getMarca().trim());
        if (producto.getTalla() != null) producto.setTalla(producto.getTalla().trim());

        // Comprobamos si ya existe exactamente este modelo con esta misma talla en la base de datos
        List<Producto> todos = productoService.obtenerTodos();
        Producto productoExistente = null;

        for (Producto p : todos) {
            if (p.getNombre() != null && p.getNombre().equalsIgnoreCase(producto.getNombre()) &&
                    p.getMarca() != null && p.getMarca().equalsIgnoreCase(producto.getMarca()) &&
                    p.getTalla() != null && p.getTalla().equalsIgnoreCase(producto.getTalla())) {

                productoExistente = p;
                break;
            }
        }

        // CASO 1: Si ya existe esa talla exacta en la BD (Evitamos duplicar registros de la misma talla)
        if (productoExistente != null) {
            // Si viene de "/nuevo" (id es null), actuamos sumando el stock como hacías originalmente
            if (producto.getId() == null) {
                int stockActual = productoExistente.getStock() != null ? productoExistente.getStock() : 0;
                int stockNuevo = producto.getStock() != null ? producto.getStock() : 0;
                productoExistente.setStock(stockActual + stockNuevo);
            } else {
                // Si viene de una edición, el usuario está fijando el stock manualmente a un valor concreto
                productoExistente.setStock(producto.getStock());
            }

            // Actualizamos los campos generales por si sufrieron modificaciones en el formulario
            productoExistente.setPrecio(producto.getPrecio());
            if (producto.getCategoria() != null) {
                productoExistente.setCategoria(producto.getCategoria());
            }

            productoService.guardar(productoExistente);
            return "redirect:/inventario";
        }

        // CASO 2: La combinación de modelo + talla es nueva.
        if (producto.getId() != null) {
            Producto original = productoService.obtenerPorId(producto.getId());
            if (original != null && !original.getTalla().equalsIgnoreCase(producto.getTalla())) {
                producto.setId(null);
                producto.setConsultas(0);
            }
        }

        productoService.guardar(producto);
        return "redirect:/inventario";
    }

    // Muestra el formulario para editar incluyendo variantes del mismo modelo
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model) {
        Producto p = productoService.obtenerPorId(id);
        model.addAttribute("producto", p);

        // Buscamos todas las tallas que ya existen para esta misma zapatilla (Misma marca y nombre)
        List<Producto> variantesMismoModelo = productoService.obtenerTodos().stream()
                .filter(prod -> prod.getNombre() != null && prod.getNombre().equalsIgnoreCase(p.getNombre()) &&
                        prod.getMarca() != null && prod.getMarca().equalsIgnoreCase(p.getMarca()))
                .collect(Collectors.toList());

        model.addAttribute("variantesTallas", variantesMismoModelo);
        model.addAttribute("productosExistentes", productoService.obtenerTodos());
        model.addAttribute("categorias", categoriaService.obtenerTodas());

        return "formulario-producto";
    }

    // --- OPCIÓN 1: ELIMINAR UNA TALLA ESPECÍFICA ---
    @PostMapping("/productos/eliminar-talla")
    public String eliminarTallaEspecifica(@RequestParam("tallaId") Integer tallaId) {
        productoService.eliminar(tallaId);
        return "redirect:/inventario";
    }

    // --- OPCIÓN 2: ELIMINAR EL PRODUCTO ENTERO (TODAS SUS TALLAS) ---
    @PostMapping("/productos/eliminar-completo/{id}")
    public String eliminarProductoCompleto(@PathVariable("id") Integer id) {
        Producto p = productoService.obtenerPorId(id);

        if (p != null) {
            List<Producto> todasLasTallas = productoService.obtenerTodos().stream()
                    .filter(prod -> prod.getNombre() != null && prod.getNombre().equalsIgnoreCase(p.getNombre()) &&
                            prod.getMarca() != null && prod.getMarca().equalsIgnoreCase(p.getMarca()))
                    .collect(Collectors.toList());

            for (Producto variante : todasLasTallas) {
                productoService.eliminar(variante.getId());
            }
        }
        return "redirect:/inventario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id) {
        productoService.eliminar(id);
        return "redirect:/inventario";
    }

    // --- DETALLE: Cuenta consultas AUTOMÁTICAMENTE SÓLO si escanean el QR real desde fuera ---
    @GetMapping("/producto/detalle/{id}")
    public String verDetallePublico(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {

        Producto p = productoService.obtenerPorId(id);

        if (p != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean esPersonalInterno = false;

            if (auth != null && auth.isAuthenticated()) {
                esPersonalInterno = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_USER"));
            }

            // Si es un escaneo real externo por móvil (Anónimo/Cliente), sumamos analítica
            if (!esPersonalInterno) {
                int consultasActuales = p.getConsultas() != null ? p.getConsultas() : 0;
                p.setConsultas(consultasActuales + 1);
                productoService.guardar(p);
            }

            List<Producto> todasLasTallas = productoService.obtenerTodos().stream()
                    .filter(prod -> prod.getNombre() != null && prod.getNombre().equalsIgnoreCase(p.getNombre()) &&
                            prod.getMarca() != null && prod.getMarca().equalsIgnoreCase(p.getMarca()))
                    .collect(Collectors.toList());

            model.addAttribute("p", p);
            model.addAttribute("variantesTallas", todasLasTallas);
        }

        return "detalle-producto";
    }

    // --- RUTA PARA MOSTRAR EL LOGIN PERSONALIZADO ---
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // --- ENDPOINT: PROCESAR COMPRA (RESTAR STOCK) ---
    @PostMapping("/productos/comprar")
    public String comprarProducto(
            @RequestParam("tallaId") Integer tallaId,
            @RequestParam(value = "origen", required = false) String origen) {

        Producto varianteTalla = productoService.obtenerPorId(tallaId);

        if (varianteTalla != null) {
            int stockActual = varianteTalla.getStock() != null ? varianteTalla.getStock() : 0;

            if (stockActual > 0) {
                // Restamos la unidad en base de datos
                varianteTalla.setStock(stockActual - 1);
                productoService.guardar(varianteTalla);

                return "redirect:/inventario?compraExitosa=true";
            } else {
                return "redirect:/inventario?errorStock=true";
            }
        }
        return "redirect:/inventario";
    }

    // --- ENDPOINT: RECIBE LAS GRÁFICAS DEL FRONTEND Y ENVÍA EL PDF POR CORREO ---
    @PostMapping("/productos/exportar/pdf")
    @ResponseBody
    public ResponseEntity<String> exportarYEnviarAPdf(
            @RequestParam(value = "grafico1", required = false) String grafico1,
            @RequestParam(value = "grafico2", required = false) String grafico2) {
        try {
            List<Producto> listaProductos = productoService.obtenerTodos();
            pdfService.generarYEnviarPorCorreo(listaProductos, grafico1, grafico2);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ERROR: " + e.getMessage());
        }
    }

    // --- ENDPOINT SECUNDARIO: DESCARGA DIRECTA TRADICIONAL (SIN GRÁFICOS) ---
    @GetMapping("/productos/exportar/pdf-directo")
    public void exportarAPdfDirecto(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");

        String cabeceraClave = "Content-Disposition";
        String cabeceraValor = "attachment; filename=inventario_tienda.pdf";
        response.setHeader(cabeceraClave, cabeceraValor);

        List<Producto> listaProductos = productoService.obtenerTodos();
        pdfService.exportarInventarioPdf(listaProductos, response);
    }
}