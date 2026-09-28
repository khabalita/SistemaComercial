package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.dto.response.IvaTypeResponseDto;
import com.khabalita.sistemacomercial.Entities.IvaType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IvaTypeMapper {

    public IvaTypeResponseDto toDto(IvaType entity) {
        if (entity == null) return null;
        return IvaTypeResponseDto.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .percentage(entity.getPercentage())
                .build();
    }

    public List<IvaTypeResponseDto> ivaTypeResponseDtoList(List<IvaType> ivaTypes) {
        if (ivaTypes == null) return List.of();
        return ivaTypes.stream()
                .map(this::toDto)
                .toList();
    }

}