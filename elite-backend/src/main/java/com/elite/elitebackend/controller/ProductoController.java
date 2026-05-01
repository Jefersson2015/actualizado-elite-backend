package com.elite.elitebackend.controller;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.elite.elitebackend.model.Producto;
import com.elite.elitebackend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public List<Producto> listar(){
        return productoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Producto buscarPorId(@PathVariable Integer id){
        return productoRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto){
        return productoRepository.save(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Integer id, @RequestBody Producto producto){
        producto.setId(id);
        return productoRepository.save(producto);
    }

    @DeleteMapping("/{id}")
    public void eliminar (@PathVariable Integer id){
        productoRepository.deleteById(id);
    }

    // POST /api/productos/{id}/imagen
    @PostMapping("/{id}/imagen")
    public Producto subirImagen(
            @PathVariable Integer id,
            @RequestParam("imagen") MultipartFile file) throws Exception {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

        if (!file.isEmpty()) {
            // Crear carpeta uploads si no existe
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            java.nio.file.Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            // Generar nombre único para el archivo
            String fileExtension = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Guardar archivo
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar producto con la ruta de la imagen
            producto.setImagen("/uploads/" + fileName);
            productoRepository.save(producto);
        }

        return producto;
    }
}