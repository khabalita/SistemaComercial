package com.khabalita.sistemacomercial.dto.response;

import com.khabalita.sistemacomercial.Entities.CustomerAccountMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerAccountMovementResponse(
        Long id,
        LocalDateTime movementDate,
        CustomerAccountMovementType type,
        BigDecimal amount,
        String concept,
        Long saleId,
        String saleNumber,
        String createdBy) {
}
