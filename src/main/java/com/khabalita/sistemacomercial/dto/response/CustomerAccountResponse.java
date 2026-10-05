package com.khabalita.sistemacomercial.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CustomerAccountResponse(
        Long customerId,
        String customerName,
        BigDecimal balance,
        List<CustomerAccountMovementResponse> movements) {
}
