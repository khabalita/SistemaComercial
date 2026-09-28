package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.dto.request.CustomerRequestDto;
import com.khabalita.sistemacomercial.dto.response.CustomerResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMapper {

    public CustomerResponseDto toDto(Customer entity) {
        if (entity == null) return null;
        return CustomerResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .taxId(entity.getTaxId())
                .address(entity.getAddress())
                .city(entity.getCity())
                .province(entity.getProvince())
                .postalCode(entity.getPostalCode())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .notes(entity.getNotes())
                .active(entity.getActive())
                .build();
    }

    public Customer toEntity(CustomerRequestDto dto) {
        if (dto == null) return null;
        return Customer.builder()
                .name(dto.name())
                .taxId(dto.taxId())
                .address(dto.address())
                .city(dto.city())
                .province(dto.province())
                .postalCode(dto.postalCode())
                .phone(dto.phone())
                .email(dto.email())
                .notes(dto.notes())
                .active(dto.active() == null ? Boolean.TRUE : dto.active())
                .build();
    }

    public List<CustomerResponseDto> customerResponseDtoList(List<Customer> customers) {
        if (customers == null) return List.of();
        return customers.stream().map(this::toDto).toList();
    }
}
