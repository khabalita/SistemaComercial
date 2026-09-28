package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.Repositories.BrandRepository;
import com.khabalita.sistemacomercial.Service.IBrandService;
import com.khabalita.sistemacomercial.dto.request.BrandRequestDto;
import com.khabalita.sistemacomercial.dto.response.BrandResponseDto;
import com.khabalita.sistemacomercial.mapper.BrandMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements IBrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDto> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brandMapper.brandResponseDtoList(brands);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponseDto> getBrandsPage(String name, Pageable pageable) {
        Page<Brand> page = name == null || name.isBlank()
                ? brandRepository.findAllBy(pageable)
                : brandRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        return page.map(brandMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponseDto getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        return   brandMapper.toDto(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponseDto getBrandByName(String name) {
        return brandMapper.toDto(brandRepository.findByNameIgnoreCase(name));
    }

    @Override
    @Transactional
    @Audited("BRAND_CREATE")
    public BrandResponseDto saveBrand(BrandRequestDto brandRequestDto) {
        Brand brand = brandMapper.toEntity(brandRequestDto);
        return brandMapper.toDto(brandRepository.save(brand));
    }

    @Override
    @Transactional
    @Audited("BRAND_UPDATE")
    public BrandResponseDto updateBrand(Long id, BrandRequestDto brandRequestDto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found: " + id));
        brand.setName(brandRequestDto.name());
        return brandMapper.toDto(brandRepository.save(brand));
    }

    @Override
    @Transactional
    @Audited("BRAND_DELETE")
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new EntityNotFoundException("Brand not found: " + id);
        }
        brandRepository.deleteById(id);
    }
}
