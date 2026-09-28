package com.khabalita.sistemacomercial.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProductRequestDto(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
        String name,

        @NotBlank(message = "El codigo interno es obligatorio")
        @Size(max = 50, message = "El codigo interno no puede superar los 50 caracteres")
        String internalCode,

        @Size(max = 50, message = "El codigo de barras no puede superar los 50 caracteres")
        String barCode,

        @Size(max = 50, message = "El codigo de proveedor no puede superar los 50 caracteres")
        String providerCode,

        @NotNull(message = "La moneda es obligatoria")
        Long coinId,

        @NotNull(message = "El costo es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
        BigDecimal cost,

        @NotNull(message = "El margen es obligatorio")
        @DecimalMin(value = "0.0", message = "El margen no puede ser negativo")
        BigDecimal margin,

        @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
        BigDecimal salePrice,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo al cargarlo manualmente")
        Integer stock,

        @Min(value = 0, message = "El stock minimo no puede ser negativo")
        Integer minStock,

        @NotNull(message = "El tipo de IVA es obligatorio")
        Long ivaTypeId,

        Long categoryId,

        Long brandId,

        @NotNull(message = "El proveedor es obligatorio")
        Long providerId)
{ }
