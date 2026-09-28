package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CoinResponseDto(

        Long id,
        String code,
        String name,
        String symbol,
        BigDecimal presentValue)
{ }
