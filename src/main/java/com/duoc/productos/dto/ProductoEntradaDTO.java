package com.duoc.productos.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

// DTO de entrada: lo que el cliente envía en POST y PUT. Solo declara tres campos:
// si llega un id o cualquier otro dato, Jackson lo ignora. El id lo genera el servidor.
@Data
@NoArgsConstructor
public class ProductoEntradaDTO {

    @NotNull(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres.")
    private String nombre;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0", message = "El precio debe ser un número entre 0 y 100000000.")
    @DecimalMax(value = "100000000", message = "El precio debe ser un número entre 0 y 100000000.")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio.")
    @Min(value = 0, message = "El stock debe ser un entero entre 0 y 1000000.")
    @Max(value = 1000000, message = "El stock debe ser un entero entre 0 y 1000000.")
    private Integer stock;

    // Los espacios a los lados no cuentan: se quitan antes de validar el largo.
    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.strip();
    }
}
