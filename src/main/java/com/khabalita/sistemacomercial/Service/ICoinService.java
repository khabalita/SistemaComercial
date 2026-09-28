package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.response.CoinResponseDto;

import java.util.List;

public interface ICoinService {

    List<CoinResponseDto> getAllCoins();
    CoinResponseDto getCoinById(Long id);
    CoinResponseDto getCoinByCode(String code);
}