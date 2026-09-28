package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SaleResponseDto(

        Long id,
        String number,
        LocalDateTime date,
        Long customerId,
        String customerName,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal generalDiscountPercent,
        BigDecimal generalDiscountAmount,
        BigDecimal ivaAmount,
        BigDecimal total,
        String notes,
        String customerAddress,
        String customerCity,
        List<ItemResponseDto> items)
{ }
