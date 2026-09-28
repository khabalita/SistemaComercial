package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.request.SaleRequestDto;
import com.khabalita.sistemacomercial.dto.response.SaleResponseDto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISaleService {

    List<SaleResponseDto> getAllSales();
    SaleResponseDto getSaleById(Long id);
    SaleResponseDto createSale(SaleRequestDto dto);
    List<SaleResponseDto> getSalesByCustomerId(Long customerId);
    List<SaleResponseDto> getSaleByCustomerName(String customerName);
    List<SaleResponseDto> getSaleByProductName(String productName);
    List<SaleResponseDto> getSaleByProductId(Long productId);
    Page<SaleResponseDto> getSalesPage(String customerName, String productName, Pageable pageable);
}
