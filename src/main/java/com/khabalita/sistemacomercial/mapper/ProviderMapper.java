package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.dto.request.ProviderRequestDto;
import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.dto.response.ProviderResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderMapper {

    public ProviderResponseDto toDto(Provider entity) {
        if (entity == null) return null;
        return ProviderResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .taxId(entity.getTaxId())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .build();
    }

    public Provider toEntity(ProviderRequestDto providerRequestDto) {
        if (providerRequestDto == null) return null;
        Provider entity = new Provider();
        entity.setName(providerRequestDto.name());
        entity.setTaxId(providerRequestDto.taxId());
        entity.setAddress(providerRequestDto.address());
        entity.setPhone(providerRequestDto.phone());
        entity.setEmail(providerRequestDto.email());
        entity.setWebsite(providerRequestDto.website());
        return entity;
    }

    public List<ProviderResponseDto> providerResponseDtoList(List<Provider> providers) {
        if (providers == null) return List.of();
        return providers.stream()
                .map(this::toDto)
                .toList();
    }
}