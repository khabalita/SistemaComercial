package com.khabalita.sistemacomercial.dto.response;

import lombok.Builder;

@Builder
public record CustomerResponseDto(
        Long id,
        String name,
        String taxId,
        String address,
        String city,
        String province,
        String postalCode,
        String phone,
        String email,
        String notes,
        Boolean active)
{ }
