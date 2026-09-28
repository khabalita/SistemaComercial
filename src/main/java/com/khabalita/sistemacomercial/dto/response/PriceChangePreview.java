package com.khabalita.sistemacomercial.dto.response;

import java.math.BigDecimal;

public record PriceChangePreview(Long productId, String internalCode, String productName,
                                 BigDecimal previousPrice, BigDecimal newPrice) {
}
