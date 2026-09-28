package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;


@Builder
public record CategoryResponseDto(
        Long id,
        String name)
{ }
