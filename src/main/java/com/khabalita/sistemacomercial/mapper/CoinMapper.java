package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.dto.response.CoinResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoinMapper {

    public CoinResponseDto toDto(Coin entity) {
        if (entity == null) return null;
        return CoinResponseDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .presentValue(entity.getPresentValue())
                .build();
    }

    public List<CoinResponseDto> CoinResponseDtoList(List<Coin> coins) {
        if (coins == null) return List.of();
        return coins.stream()
                .map(this::toDto)
                .toList();
    }

}