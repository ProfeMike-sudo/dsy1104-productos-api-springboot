package com.duoc.productos.service;

import com.duoc.productos.dto.ProductoEntradaDTO;
import com.duoc.productos.dto.ProductoSalidaDTO;
import com.duoc.productos.exception.RecursoNoEncontradoException;
import com.duoc.productos.model.Producto;
import com.duoc.productos.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repository;

    public List<ProductoSalidaDTO> listar() {
        return repository.findAll().stream().map(this::aSalida).toList();
    }

    public ProductoSalidaDTO buscar(Long id) {
        return aSalida(obtener(id));
    }

    public ProductoSalidaDTO crear(ProductoEntradaDTO entrada) {
        Producto producto = new Producto();
        producto.setNombre(entrada.getNombre());
        producto.setPrecio(entrada.getPrecio());
        producto.setStock(entrada.getStock());
        // El servidor controla el id y la fecha: el cliente no los envía.
        producto.setFechaCreacion(LocalDateTime.now());
        return aSalida(repository.save(producto));
    }

    // PUT reemplaza los tres campos completos.
    public ProductoSalidaDTO actualizar(Long id, ProductoEntradaDTO entrada) {
        Producto producto = obtener(id);
        producto.setNombre(entrada.getNombre());
        producto.setPrecio(entrada.getPrecio());
        producto.setStock(entrada.getStock());
        return aSalida(repository.save(producto));
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado.");
        }
        repository.deleteById(id);
    }

    private Producto obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
    }

    private ProductoSalidaDTO aSalida(Producto producto) {
        return new ProductoSalidaDTO(producto.getId(), producto.getNombre(), producto.getPrecio(), producto.getStock());
    }
}
