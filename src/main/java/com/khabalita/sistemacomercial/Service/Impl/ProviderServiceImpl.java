package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.Repositories.ProviderRepository;
import com.khabalita.sistemacomercial.Service.IProviderService;
import com.khabalita.sistemacomercial.dto.request.ProviderRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProviderResponseDto;
import com.khabalita.sistemacomercial.mapper.ProviderMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements IProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponseDto> getAllProviders() {
        List<Provider> providers = providerRepository.findAll();
        return providerMapper.providerResponseDtoList(providers);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderResponseDto> getProvidersPage(String name, Pageable pageable) {
        Page<Provider> page = name == null || name.isBlank()
                ? providerRepository.findAllBy(pageable)
                : providerRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        return page.map(providerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponseDto getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Provider not found" + id));
        return providerMapper.toDto(provider);
    }

    @Override
    @Transactional
    @Audited("PROVIDER_CREATE")
    public ProviderResponseDto saveProvider(ProviderRequestDto providerRequestDto) {
        Provider provider = providerMapper.toEntity(providerRequestDto);
        return providerMapper.toDto(providerRepository.save(provider));
    }

    @Override
    @Transactional
    @Audited("PROVIDER_DELETE")
    public void deleteProvider(Long id) {
        if (!providerRepository.existsById(id)) {
            throw new EntityNotFoundException("Provider not found: " + id);
        }
        providerRepository.deleteById(id);
    }

    @Override
    @Transactional
    @Audited("PROVIDER_UPDATE")
    public ProviderResponseDto updateProvider(Long id, ProviderRequestDto providerRequestDto) {
        Provider existing = providerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found: " + id));
        existing.setName(providerRequestDto.name());
        existing.setTaxId(providerRequestDto.taxId());
        existing.setAddress(providerRequestDto.address());
        existing.setPhone(providerRequestDto.phone());
        existing.setEmail(providerRequestDto.email());
        existing.setWebsite(providerRequestDto.website());
        return providerMapper.toDto(providerRepository.save(existing));
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponseDto getProviderByName(String name) {
        return providerMapper.toDto(providerRepository.findByNameIgnoreCase(name));
    }
}
