package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

@Builder
public record ProviderResponseDto(
        Long id,
        String name,
        String taxId,
        String address,
        String phone,
        String email,
        String website)
{ }
