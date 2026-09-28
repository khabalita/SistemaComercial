package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.request.ProviderRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProviderResponseDto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProviderService {

    List<ProviderResponseDto> getAllProviders();
    Page<ProviderResponseDto> getProvidersPage(String name, Pageable pageable);
    ProviderResponseDto getProviderById(Long id);
    ProviderResponseDto saveProvider(ProviderRequestDto providerRequestDto);
    void deleteProvider(Long id);
    ProviderResponseDto updateProvider(Long id, ProviderRequestDto providerRequestDto);
    ProviderResponseDto getProviderByName(String name);
}
