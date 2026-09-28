package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.IvaType;
import com.khabalita.sistemacomercial.Repositories.IvaTypeRepository;
import com.khabalita.sistemacomercial.Service.IIvaTypeService;
import com.khabalita.sistemacomercial.dto.response.IvaTypeResponseDto;
import com.khabalita.sistemacomercial.mapper.IvaTypeMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IvaTypeServiceImpl implements IIvaTypeService {

    private final IvaTypeRepository ivaTypeRepository;
    private final IvaTypeMapper ivaTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<IvaTypeResponseDto> getAllIvaTypes() {
        return ivaTypeMapper.ivaTypeResponseDtoList(ivaTypeRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public IvaTypeResponseDto getIvaTypeById(Long id) {
        IvaType ivaType = ivaTypeRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("IvaType not found with id: " + id));
        return ivaTypeMapper.toDto(ivaType);
    }

    @Override
    @Transactional(readOnly = true)
    public IvaTypeResponseDto getIvaTypeByDescription(String description) {
        return ivaTypeMapper.toDto(ivaTypeRepository.findByDescriptionIgnoreCase(description));
    }
}