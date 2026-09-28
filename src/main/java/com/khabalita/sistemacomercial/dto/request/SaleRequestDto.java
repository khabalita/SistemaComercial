package com.khabalita.sistemacomercial.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;
import java.math.BigDecimal;

@Builder
public record SaleRequestDto(

        Long customerId,

        @Size(max = 2000, message = "Las notas no pueden superar los 2000 caracteres")
        String notes,

        BigDecimal generalDiscountPercent,

        @NotEmpty(message = "La venta debe tener al menos un item")
        @Valid
        List<ItemRequestDto> items)
{ }
