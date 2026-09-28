package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.Repositories.CoinRepository;
import com.khabalita.sistemacomercial.Service.Impl.CoinServiceImpl;
import com.khabalita.sistemacomercial.dto.response.CoinResponseDto;
import com.khabalita.sistemacomercial.mapper.CoinMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoinServiceImplTest {

    @Mock CoinRepository coinRepository;
    @Mock CoinMapper coinMapper;

    @InjectMocks CoinServiceImpl service;

    @Test
    void getAll_returnsMappedList() {
        when(coinRepository.findAll()).thenReturn(List.of(new Coin()));
        when(coinMapper.CoinResponseDtoList(any())).thenReturn(List.of(
                CoinResponseDto.builder().id(1L).code("ARS").name("Peso").build()));

        assertEquals(1, service.getAllCoins().size());
    }

    @Test
    void getById_existing_returnsDto() {
        Coin c = Coin.builder().code("ARS").name("Peso").presentValue(BigDecimal.ONE).build();
        c.setId(1L);
        when(coinRepository.findById(1L)).thenReturn(Optional.of(c));
        when(coinMapper.toDto(c)).thenReturn(CoinResponseDto.builder().id(1L).code("ARS").name("Peso").build());

        assertEquals("ARS", service.getCoinById(1L).code());
    }

    @Test
    void getById_notFound_throws() {
        when(coinRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getCoinById(99L));
    }

    @Test
    void getByCode_returnsDto() {
        Coin c = Coin.builder().code("USD").build();
        c.setId(1L);
        when(coinRepository.findByCodeIgnoreCase("usd")).thenReturn(c);
        when(coinMapper.toDto(c)).thenReturn(CoinResponseDto.builder().id(1L).code("USD").build());

        assertEquals("USD", service.getCoinByCode("usd").code());
    }
}
