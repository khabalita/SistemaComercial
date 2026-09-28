package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.Repositories.ProviderRepository;
import com.khabalita.sistemacomercial.Service.Impl.ProviderServiceImpl;
import com.khabalita.sistemacomercial.dto.request.ProviderRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProviderResponseDto;
import com.khabalita.sistemacomercial.mapper.ProviderMapper;
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
class ProviderServiceImplTest {

    @Mock ProviderRepository providerRepository;
    @Mock ProviderMapper providerMapper;

    @InjectMocks ProviderServiceImpl service;

    @Test
    void getAll_returnsMappedList() {
        when(providerRepository.findAll()).thenReturn(List.of(new Provider()));
        when(providerMapper.providerResponseDtoList(any())).thenReturn(List.of(
                ProviderResponseDto.builder().id(1L).name("Prov").build()));

        assertEquals(1, service.getAllProviders().size());
    }

    @Test
    void getById_existing_returnsDto() {
        Provider p = Provider.builder().name("Prov").build();
        p.setId(1L);
        when(providerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(providerMapper.toDto(p)).thenReturn(ProviderResponseDto.builder().id(1L).name("Prov").build());

        assertEquals("Prov", service.getProviderById(1L).name());
    }

    @Test
    void getById_notFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getProviderById(99L));
    }

    @Test
    void save_persistsAndReturns() {
        ProviderRequestDto req = ProviderRequestDto.builder().name("Prov").build();
        Provider entity = Provider.builder().name("Prov").build();
        ProviderResponseDto resp = ProviderResponseDto.builder().id(1L).name("Prov").build();

        when(providerMapper.toEntity(req)).thenReturn(entity);
        when(providerRepository.save(entity)).thenReturn(entity);
        when(providerMapper.toDto(entity)).thenReturn(resp);

        assertEquals("Prov", service.saveProvider(req).name());
    }

    @Test
    void update_existing_updatesAndSaves() {
        Provider existing = Provider.builder().name("Prov").build();
        existing.setId(1L);
        ProviderRequestDto req = ProviderRequestDto.builder()
                .name("Prov updated").taxId("30-12345678-9").build();

        when(providerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(providerRepository.save(existing)).thenReturn(existing);
        when(providerMapper.toDto(existing))
                .thenReturn(ProviderResponseDto.builder().id(1L).name("Prov updated").build());

        ProviderResponseDto result = service.updateProvider(1L, req);

        assertEquals("Prov updated", result.name());
        assertEquals("Prov updated", existing.getName());
        assertEquals("30-12345678-9", existing.getTaxId());
    }

    @Test
    void update_notFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateProvider(99L, ProviderRequestDto.builder().name("X").build()));
    }

    @Test
    void delete_existing_deletes() {
        when(providerRepository.existsById(1L)).thenReturn(true);

        service.deleteProvider(1L);

        verify(providerRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(providerRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.deleteProvider(99L));
        verify(providerRepository, never()).deleteById(any());
    }

    @Test
    void getByName_returnsDto() {
        Provider p = Provider.builder().name("Prov").build();
        p.setId(1L);
        when(providerRepository.findByNameIgnoreCase("prov")).thenReturn(p);
        when(providerMapper.toDto(p)).thenReturn(ProviderResponseDto.builder().id(1L).name("Prov").build());

        assertEquals("Prov", service.getProviderByName("prov").name());
    }
}
