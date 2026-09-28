package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ItemResponseDto(

        Long id,
        Long productId,
        String productName,
        String productInternalCode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineDiscount,
        BigDecimal lineTotal)
{ }
