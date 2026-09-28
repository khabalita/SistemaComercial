package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.request.BrandRequestDto;
import com.khabalita.sistemacomercial.dto.response.BrandResponseDto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBrandService {

    List<BrandResponseDto> getAllBrands();
    Page<BrandResponseDto> getBrandsPage(String name, Pageable pageable);
    BrandResponseDto getBrandById(Long id);
    BrandResponseDto getBrandByName(String name);
    BrandResponseDto saveBrand(BrandRequestDto brandRequestDto);
    BrandResponseDto updateBrand(Long id, BrandRequestDto brandRequestDto);
    void deleteBrand(Long id);
}
