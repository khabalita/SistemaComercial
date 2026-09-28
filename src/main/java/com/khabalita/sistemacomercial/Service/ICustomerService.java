package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.request.CustomerRequestDto;
import com.khabalita.sistemacomercial.dto.response.CustomerResponseDto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerService {

    List<CustomerResponseDto> getAllCustomers();
    List<CustomerResponseDto> getAllActiveCustomers();
    List<CustomerResponseDto> getAllCustomersByNameContainingIgnoreCase(String name);
    Page<CustomerResponseDto> getCustomersPage(String name, Pageable pageable);
    CustomerResponseDto getCustomerById(Long id);
    CustomerResponseDto getCustomerByTaxId(String taxId);
    CustomerResponseDto saveCustomer(CustomerRequestDto customerRequestDto);
    CustomerResponseDto updateCustomer(Long id, CustomerRequestDto customerRequestDto);
    void deleteCustomer(Long id);
}
