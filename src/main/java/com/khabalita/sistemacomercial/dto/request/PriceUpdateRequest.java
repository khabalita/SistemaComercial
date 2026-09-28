package com.khabalita.sistemacomercial.dto.request;

import java.math.BigDecimal;

public record PriceUpdateRequest(Long brandId, String adjustmentType, BigDecimal value) {
}
