package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.Repositories.CustomerRepository;
import com.khabalita.sistemacomercial.Service.ICustomerService;
import com.khabalita.sistemacomercial.dto.request.CustomerRequestDto;
import com.khabalita.sistemacomercial.dto.response.CustomerResponseDto;
import com.khabalita.sistemacomercial.mapper.CustomerMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getAllCustomers() {
        return customerMapper.customerResponseDtoList(customerRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getAllActiveCustomers() {
        return customerMapper.customerResponseDtoList(customerRepository.findByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getAllCustomersByNameContainingIgnoreCase(String name) {
        return customerMapper.customerResponseDtoList(
                customerRepository.findByNameContainingIgnoreCase(name));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> getCustomersPage(String name, Pageable pageable) {
        Page<Customer> page = name == null || name.isBlank()
                ? customerRepository.findAllBy(pageable)
                : customerRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        return page.map(customerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerByTaxId(String taxId) {
        Customer customer = customerRepository.findByTaxId(taxId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with taxId: " + taxId));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    @Audited("CUSTOMER_CREATE")
    public CustomerResponseDto saveCustomer(CustomerRequestDto dto) {
        Customer customer = customerMapper.toEntity(dto);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    @Transactional
    @Audited("CUSTOMER_UPDATE")
    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto dto) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
        existing.setName(dto.name());
        existing.setTaxId(dto.taxId());
        existing.setAddress(dto.address());
        existing.setCity(dto.city());
        existing.setProvince(dto.province());
        existing.setPostalCode(dto.postalCode());
        existing.setPhone(dto.phone());
        existing.setEmail(dto.email());
        existing.setNotes(dto.notes());
        if (dto.active() != null) {
            existing.setActive(dto.active());
        }
        return customerMapper.toDto(customerRepository.save(existing));
    }

    @Override
    @Transactional
    @Audited("CUSTOMER_DELETE")
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }
}
