package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.dto.request.BrandRequestDto;
import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.dto.response.BrandResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrandMapper {

    public BrandResponseDto toDto(Brand entity) {
        if (entity == null) return null;
        return BrandResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public Brand toEntity(BrandRequestDto dto) {
        if (dto == null) return null;
        Brand entity = new Brand();
        entity.setName(dto.name());
        return entity;
    }

    public List<BrandResponseDto> brandResponseDtoList(List<Brand> brands) {
        if (brands == null) return List.of();
        return brands.stream()
                .map(this::toDto)
                .toList();
    }

}