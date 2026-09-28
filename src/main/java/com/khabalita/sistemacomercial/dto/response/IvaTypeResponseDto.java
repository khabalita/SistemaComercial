package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record IvaTypeResponseDto(

        Long id,
        String description,
        BigDecimal percentage)
{ }
