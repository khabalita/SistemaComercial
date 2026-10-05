package com.khabalita.sistemacomercial.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CustomerAccountPaymentRequest(
        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El importe no es válido")
        BigDecimal amount,

        @NotBlank(message = "El concepto es obligatorio")
        @Size(max = 500, message = "El concepto no puede superar los 500 caracteres")
        String concept) {
}
