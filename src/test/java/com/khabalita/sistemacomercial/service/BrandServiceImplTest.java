package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.Repositories.BrandRepository;
import com.khabalita.sistemacomercial.Service.Impl.BrandServiceImpl;
import com.khabalita.sistemacomercial.dto.request.BrandRequestDto;
import com.khabalita.sistemacomercial.dto.response.BrandResponseDto;
import com.khabalita.sistemacomercial.mapper.BrandMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock BrandRepository brandRepository;
    @Mock BrandMapper brandMapper;

    @InjectMocks BrandServiceImpl service;

    @Test
    void getAll_returnsMappedList() {
        when(brandRepository.findAll()).thenReturn(List.of(new Brand()));
        when(brandMapper.brandResponseDtoList(any())).thenReturn(List.of(
                BrandResponseDto.builder().id(1L).name("Sin marca").build()));

        assertEquals(1, service.getAllBrands().size());
    }

    @Test
    void getById_existing_returnsDto() {
        Brand b = Brand.builder().name("Sin marca").build();
        b.setId(1L);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(b));
        when(brandMapper.toDto(b)).thenReturn(BrandResponseDto.builder().id(1L).name("Sin marca").build());

        assertEquals("Sin marca", service.getBrandById(1L).name());
    }

    @Test
    void getById_notFound_throws() {
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getBrandById(99L));
    }

    @Test
    void save_persistsAndReturns() {
        BrandRequestDto req = BrandRequestDto.builder().name("La Serenisima").build();
        Brand entity = Brand.builder().name("La Serenisima").build();
        BrandResponseDto resp = BrandResponseDto.builder().id(1L).name("La Serenisima").build();

        when(brandMapper.toEntity(req)).thenReturn(entity);
        when(brandRepository.save(entity)).thenReturn(entity);
        when(brandMapper.toDto(entity)).thenReturn(resp);

        assertEquals("La Serenisima", service.saveBrand(req).name());
    }

    @Test
    void update_existing_updatesName() {
        Brand existing = Brand.builder().name("Sin marca").build();
        existing.setId(1L);
        BrandRequestDto req = BrandRequestDto.builder().name("Marca X").build();

        when(brandRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(brandRepository.save(existing)).thenReturn(existing);
        when(brandMapper.toDto(existing)).thenReturn(BrandResponseDto.builder().id(1L).name("Marca X").build());

        assertEquals("Marca X", service.updateBrand(1L, req).name());
        assertEquals("Marca X", existing.getName());
    }

    @Test
    void update_notFound_throws() {
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateBrand(99L, BrandRequestDto.builder().name("X").build()));
    }

    @Test
    void delete_existing_deletes() {
        when(brandRepository.existsById(1L)).thenReturn(true);

        service.deleteBrand(1L);

        verify(brandRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(brandRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.deleteBrand(99L));
        verify(brandRepository, never()).deleteById(any());
    }
}
