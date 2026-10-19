package com.elite.elitebackend.controller;

import com.elite.elitebackend.model.*;
import com.elite.elitebackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoDetalleRepository pedidoDetalleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Estados que puede tener un pedido
    private static final List<String> ESTADOS_VALIDOS =
            List.of("pendiente", "pagado", "enviado", "entregado", "cancelado");

    // Lista todos los pedidos, del más reciente al más antiguo
    @GetMapping
    public List<Pedido> listar() {
        return pedidoRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
    }

    @GetMapping("/{id}")
    public Pedido buscarPorId(@PathVariable Integer id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    @Transactional
    @PostMapping
    public Pedido crear(@RequestBody PedidoRequest request) {
        System.out.println("=== CREANDO PEDIDO ===");
        System.out.println("ID Usuario: " + request.getIdUsuario());
        System.out.println("Total: " + request.getTotal());
        System.out.println("Items: " + request.getItems().size());

        // 1. Buscar el usuario
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getIdUsuario()));

        // 2. Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTotal(request.getTotal());
        pedido.setEstado("pendiente");
        pedido.setFecha(LocalDateTime.now());

        // 3. Guardar pedido para obtener el ID
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        System.out.println("Pedido guardado con ID: " + pedidoGuardado.getId());

        // 4. Crear los detalles del pedido
        List<PedidoDetalle> detalles = new ArrayList<>();
        for (ItemCarrito item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getIdProducto()));

            // Validación de stock ANTES de descontar
            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                        ". Disponible: " + producto.getStock() + ", Solicitado: " + item.getCantidad());
            }

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setPedido(pedidoGuardado);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio() * item.getCantidad());

            detalles.add(detalle);

            // Descontar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
            System.out.println("Producto: " + producto.getNombre() + " - Stock actualizado: " + producto.getStock());
        }

        // 6. Guardar detalles
        pedidoDetalleRepository.saveAll(detalles);
        System.out.println("Detalles guardados: " + detalles.size());

        pedidoGuardado.setDetalles(detalles);
        return pedidoGuardado;
    }

    // PUT /api/pedidos/{id}/estado   body: {"estado": "pagado"}
    // Permite al administrador confirmar manualmente un pedido y avanzar su estado.
    @Transactional
    @PutMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");

        if (nuevoEstado == null || !ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no valido: " + nuevoEstado);
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + id));

        String estadoActual = pedido.getEstado();

        if ("cancelado".equals(estadoActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un pedido cancelado no se puede modificar");
        }
        if ("entregado".equals(estadoActual) && "cancelado".equals(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un pedido entregado no se puede cancelar");
        }

        // Si se cancela, los productos vuelven al inventario
        if ("cancelado".equals(nuevoEstado)) {
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }
}