package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.Repositories.CoinRepository;
import com.khabalita.sistemacomercial.Service.ICoinService;
import com.khabalita.sistemacomercial.dto.response.CoinResponseDto;
import com.khabalita.sistemacomercial.mapper.CoinMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements ICoinService {

    private final CoinRepository coinRepository;
    private final CoinMapper coinMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CoinResponseDto> getAllCoins() {
        return coinMapper.CoinResponseDtoList(coinRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public CoinResponseDto getCoinById(Long id) {
        Coin coin = coinRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Coin not found" + id));
        return coinMapper.toDto(coin);
    }

    @Override
    @Transactional(readOnly = true)
    public CoinResponseDto getCoinByCode(String code) {
        return coinMapper.toDto(coinRepository.findByCodeIgnoreCase(code));
    }

    @Override
    @Transactional
    @Audited("USD_RATE_UPDATE")
    public CoinResponseDto updatePresentValue(String code, BigDecimal presentValue) {
        if (presentValue == null || presentValue.signum() <= 0) {
            throw new IllegalArgumentException("La cotización debe ser mayor a 0");
        }
        BigDecimal normalized = presentValue.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(new BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("La cotización supera el valor máximo permitido");
        }

        Coin coin = coinRepository.findByCodeIgnoreCase(code);
        if (coin == null) throw new EntityNotFoundException("Moneda no encontrada: " + code);
        coin.setPresentValue(normalized);
        return coinMapper.toDto(coinRepository.save(coin));
    }
}
