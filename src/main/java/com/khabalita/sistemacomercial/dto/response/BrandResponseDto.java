package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

@Builder
public record BrandResponseDto(
        Long id,
        String name)
{ }
