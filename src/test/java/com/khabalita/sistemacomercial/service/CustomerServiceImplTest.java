package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.Repositories.CustomerRepository;
import com.khabalita.sistemacomercial.Service.Impl.CustomerServiceImpl;
import com.khabalita.sistemacomercial.dto.request.CustomerRequestDto;
import com.khabalita.sistemacomercial.dto.response.CustomerResponseDto;
import com.khabalita.sistemacomercial.mapper.CustomerMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock CustomerRepository customerRepository;
    @Mock CustomerMapper customerMapper;

    @InjectMocks CustomerServiceImpl service;

    @Test
    void getAll_returnsMappedList() {
        when(customerRepository.findAll()).thenReturn(List.of(new Customer()));
        when(customerMapper.customerResponseDtoList(any())).thenReturn(List.of(
                CustomerResponseDto.builder().id(1L).name("Juan").build()));

        assertEquals(1, service.getAllCustomers().size());
    }

    @Test
    void getAllActive_returnsActiveOnly() {
        when(customerRepository.findByActiveTrue()).thenReturn(List.of(new Customer()));
        when(customerMapper.customerResponseDtoList(any())).thenReturn(List.of(
                CustomerResponseDto.builder().id(1L).name("Juan").active(true).build()));

        List<CustomerResponseDto> result = service.getAllActiveCustomers();
        assertEquals(1, result.size());
        verify(customerRepository).findByActiveTrue();
    }

    @Test
    void getByNameContaining_delegates() {
        when(customerRepository.findByNameContainingIgnoreCase("juan")).thenReturn(List.of(new Customer()));
        when(customerMapper.customerResponseDtoList(any())).thenReturn(List.of(
                CustomerResponseDto.builder().id(1L).name("Juan").build()));

        assertEquals(1, service.getAllCustomersByNameContainingIgnoreCase("juan").size());
    }

    @Test
    void getById_existing_returnsDto() {
        Customer c = Customer.builder().name("Juan").build();
        c.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(customerMapper.toDto(c)).thenReturn(CustomerResponseDto.builder().id(1L).name("Juan").build());

        assertEquals("Juan", service.getCustomerById(1L).name());
    }

    @Test
    void getById_notFound_throws() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getCustomerById(99L));
    }

    @Test
    void getByTaxId_existing_returnsDto() {
        Customer c = Customer.builder().name("Juan").taxId("20-12345678-9").build();
        c.setId(1L);
        when(customerRepository.findByTaxId("20-12345678-9")).thenReturn(Optional.of(c));
        when(customerMapper.toDto(c)).thenReturn(
                CustomerResponseDto.builder().id(1L).name("Juan").taxId("20-12345678-9").build());

        assertEquals("20-12345678-9", service.getCustomerByTaxId("20-12345678-9").taxId());
    }

    @Test
    void getByTaxId_notFound_throws() {
        when(customerRepository.findByTaxId("x")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getCustomerByTaxId("x"));
    }

    @Test
    void save_persistsAndReturns() {
        CustomerRequestDto req = CustomerRequestDto.builder()
                .name("Juan").email("j@x.com").active(true).build();
        Customer entity = Customer.builder().name("Juan").build();
        CustomerResponseDto resp = CustomerResponseDto.builder().id(1L).name("Juan").build();

        when(customerMapper.toEntity(req)).thenReturn(entity);
        when(customerRepository.save(entity)).thenReturn(entity);
        when(customerMapper.toDto(entity)).thenReturn(resp);

        assertEquals("Juan", service.saveCustomer(req).name());
    }

    @Test
    void update_existing_updatesAndSaves() {
        Customer existing = Customer.builder().name("Juan").city("La Plata").build();
        existing.setId(1L);
        CustomerRequestDto req = CustomerRequestDto.builder()
                .name("Juan").city("CABA").active(true).build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);
        when(customerMapper.toDto(existing))
                .thenReturn(CustomerResponseDto.builder().id(1L).name("Juan").city("CABA").build());

        CustomerResponseDto result = service.updateCustomer(1L, req);

        assertEquals("CABA", result.city());
        assertEquals("CABA", existing.getCity());
    }

    @Test
    void update_activeNull_keepsExistingActive() {
        Customer existing = Customer.builder().name("Juan").active(false).build();
        existing.setId(1L);
        CustomerRequestDto req = CustomerRequestDto.builder().name("Juan").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);
        when(customerMapper.toDto(existing)).thenReturn(
                CustomerResponseDto.builder().id(1L).name("Juan").active(false).build());

        service.updateCustomer(1L, req);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertEquals(false, captor.getValue().getActive());
    }

    @Test
    void update_notFound_throws() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.updateCustomer(99L, CustomerRequestDto.builder().name("X").build()));
    }

    @Test
    void delete_existing_deletes() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        service.deleteCustomer(1L);
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(customerRepository.existsById(99L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.deleteCustomer(99L));
        verify(customerRepository, never()).deleteById(any());
    }
}
