package com.khabalita.sistemacomercial.Service;

import java.math.BigDecimal;

record ProductImportRow(
        int rowNumber,
        String name,
        String internalCode,
        String barCode,
        String providerCode,
        BigDecimal cost,
        BigDecimal salePrice,
        Integer stock,
        Integer minStock,
        String categoryName,
        String brandName,
        String providerName) {
}
