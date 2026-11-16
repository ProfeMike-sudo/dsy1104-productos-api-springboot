package com.duoc.productos.repository;

import com.duoc.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Spring Data genera el SQL de findAll, findById, save, existsById y deleteById.
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
