package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.IvaType;
import com.khabalita.sistemacomercial.Repositories.IvaTypeRepository;
import com.khabalita.sistemacomercial.Service.Impl.IvaTypeServiceImpl;
import com.khabalita.sistemacomercial.dto.response.IvaTypeResponseDto;
import com.khabalita.sistemacomercial.mapper.IvaTypeMapper;
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
class IvaTypeServiceImplTest {

    @Mock IvaTypeRepository ivaTypeRepository;
    @Mock IvaTypeMapper ivaTypeMapper;

    @InjectMocks IvaTypeServiceImpl service;

    @Test
    void getAll_returnsMappedList() {
        when(ivaTypeRepository.findAll()).thenReturn(List.of(new IvaType()));
        when(ivaTypeMapper.ivaTypeResponseDtoList(any())).thenReturn(List.of(
                IvaTypeResponseDto.builder().id(1L).description("IVA 21%").build()));

        assertEquals(1, service.getAllIvaTypes().size());
    }

    @Test
    void getById_existing_returnsDto() {
        IvaType i = IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21.00")).build();
        i.setId(1L);
        when(ivaTypeRepository.findById(1L)).thenReturn(Optional.of(i));
        when(ivaTypeMapper.toDto(i)).thenReturn(IvaTypeResponseDto.builder().id(1L).description("IVA 21%").build());

        assertEquals("IVA 21%", service.getIvaTypeById(1L).description());
    }

    @Test
    void getById_notFound_throws() {
        when(ivaTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getIvaTypeById(99L));
    }

    @Test
    void getByDescription_returnsDto() {
        IvaType i = IvaType.builder().description("IVA 21%").build();
        i.setId(1L);
        when(ivaTypeRepository.findByDescriptionIgnoreCase("iva 21%")).thenReturn(i);
        when(ivaTypeMapper.toDto(i)).thenReturn(IvaTypeResponseDto.builder().id(1L).description("IVA 21%").build());

        assertEquals("IVA 21%", service.getIvaTypeByDescription("iva 21%").description());
    }
}
