package com.duoc.productos.dto;

import lombok.*;
import java.math.BigDecimal;

// DTO de salida: el contrato de la respuesta. fechaCreacion queda fuera a propósito.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoSalidaDTO {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
}
