package com.duoc.productos.controller;

import com.duoc.productos.dto.ProductoEntradaDTO;
import com.duoc.productos.dto.ProductoSalidaDTO;
import com.duoc.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// El controlador recibe la solicitud HTTP y decide el código de estado de la respuesta.
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    @GetMapping
    public List<ProductoSalidaDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProductoSalidaDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // @Valid ejecuta las validaciones del DTO de entrada antes de llegar al service.
    @PostMapping
    public ResponseEntity<ProductoSalidaDTO> crear(@Valid @RequestBody ProductoEntradaDTO entrada) {
        ProductoSalidaDTO creado = service.crear(entrada);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public ProductoSalidaDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProductoEntradaDTO entrada) {
        return service.actualizar(id, entrada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
