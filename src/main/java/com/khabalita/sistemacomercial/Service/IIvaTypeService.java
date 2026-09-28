package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.response.IvaTypeResponseDto;

import java.util.List;

public interface IIvaTypeService {

    List<IvaTypeResponseDto> getAllIvaTypes();
    IvaTypeResponseDto getIvaTypeById(Long id);
    IvaTypeResponseDto getIvaTypeByDescription(String description);
}