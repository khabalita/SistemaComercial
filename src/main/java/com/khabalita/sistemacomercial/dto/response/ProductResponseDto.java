package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponseDto(

        Long id,
        String name,
        String internalCode,
        String barCode,
        String providerCode,
        Long coinId,
        String coinCode,
        BigDecimal cost,
        BigDecimal providerDiscountPercentageApplied,
        BigDecimal discountedCost,
        BigDecimal margin,
        BigDecimal salePrice,
        BigDecimal salePriceArsEquivalent,
        Integer stock,
        Integer minStock,
        Long ivaTypeId,
        String ivaTypeDescription,
        Long categoryId,
        String categoryName,
        Long brandId,
        String brandName,
        Long providerId,
        String providerName,
        boolean salePriceManualOverride,
        boolean hasImage)
{ }
