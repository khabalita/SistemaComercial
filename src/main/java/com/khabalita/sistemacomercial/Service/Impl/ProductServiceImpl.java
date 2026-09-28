package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.Repositories.ProductRepository;
import com.khabalita.sistemacomercial.Service.IProductService;
import com.khabalita.sistemacomercial.dto.request.ProductRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProductResponseDto;
import com.khabalita.sistemacomercial.dto.request.PriceUpdateRequest;
import com.khabalita.sistemacomercial.dto.response.PriceChangePreview;
import com.khabalita.sistemacomercial.mapper.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    @Audited("PRODUCT_CREATE")
    public ProductResponseDto saveProduct(ProductRequestDto productRequestDto) {
        validateProduct(productRequestDto);
        Product product = productMapper.toEntity(productRequestDto);
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return productMapper.productResponseDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProductsByCategoryNameIgnoreCase(String categoryName) {
        return productMapper.productResponseDtoList(
                productRepository.findByCategory_NameContainingIgnoreCase(categoryName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProductsByBrandNameIgnoreCase(String brandName) {
        return productMapper.productResponseDtoList(
                productRepository.findByBrand_NameContainingIgnoreCase(brandName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProductsByProviderNameIgnoreCase(String providerName) {
        return productMapper.productResponseDtoList(
                productRepository.findByProvider_NameContainingIgnoreCase(providerName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProductsByNameIgnoreCase(String productName) {
        return productMapper.productResponseDtoList(
                productRepository.findByNameContainingIgnoreCase(productName));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsPage(String name, String category, String brand,
                                                    String provider, Pageable pageable) {
        if (name != null && !name.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(name.trim(), pageable)
                    .map(productMapper::toDto);
        }
        if (category != null && !category.isBlank()) {
            return productRepository.findByCategory_NameContainingIgnoreCase(category.trim(), pageable)
                    .map(productMapper::toDto);
        }
        if (brand != null && !brand.isBlank()) {
            return productRepository.findByBrand_NameContainingIgnoreCase(brand.trim(), pageable)
                    .map(productMapper::toDto);
        }
        if (provider != null && !provider.isBlank()) {
            return productRepository.findByProvider_NameContainingIgnoreCase(provider.trim(), pageable)
                    .map(productMapper::toDto);
        }
        return productRepository.findAllBy(pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductByBarCode(String barCode) {
        Product product = productRepository.findByBarCode(barCode);
        if (product == null) {
            throw new EntityNotFoundException("Product not found with barCode: " + barCode);
        }
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    @Audited("PRODUCT_DELETE")
    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    @Audited("PRODUCT_UPDATE")
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
        validateProduct(productRequestDto);
        Product existing = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        Product updated = productMapper.toEntity(productRequestDto);
        existing.setName(updated.getName());
        existing.setInternalCode(updated.getInternalCode());
        existing.setBarCode(updated.getBarCode());
        existing.setProviderCode(updated.getProviderCode());
        existing.setCoin(updated.getCoin());
        existing.setCost(updated.getCost());
        existing.setMargin(updated.getMargin());
        existing.setSalePrice(updated.getSalePrice());
        existing.setStock(updated.getStock());
        existing.setMinStock(updated.getMinStock());
        existing.setIvaType(updated.getIvaType());
        existing.setCategory(updated.getCategory());
        existing.setBrand(updated.getBrand());
        existing.setProvider(updated.getProvider());
        return productMapper.toDto(productRepository.save(existing));
    }

    @Override
    @Transactional
    @Audited("STOCK_UPDATE")
    public ProductResponseDto updateStock(Long id, Integer stock) {
        if (stock == null) {
            throw new IllegalArgumentException("El stock no puede ser nulo");
        }
        Product existing = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        existing.setStock(stock);
        return productMapper.toDto(productRepository.save(existing));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceChangePreview> previewPriceUpdate(PriceUpdateRequest request) {
        return productsForPriceUpdate(request).stream().map(product -> changePreview(product, request)).toList();
    }

    @Override
    @Transactional
    @Audited("PRODUCT_PRICE_BULK_UPDATE")
    public int applyPriceUpdate(PriceUpdateRequest request) {
        List<Product> products = productsForPriceUpdate(request);
        for (Product product : products) product.setSalePrice(changePreview(product, request).newPrice());
        productRepository.saveAll(products);
        return products.size();
    }

    private List<Product> productsForPriceUpdate(PriceUpdateRequest request) {
        validatePriceUpdate(request);
        return request.brandId() == null ? productRepository.findAll() : productRepository.findByBrand_Id(request.brandId());
    }

    private PriceChangePreview changePreview(Product product, PriceUpdateRequest request) {
        BigDecimal previous = product.getSalePrice();
        if (previous == null) previous = productMapper.toDto(product).salePrice();
        BigDecimal newPrice = "FIXED".equalsIgnoreCase(request.adjustmentType())
                ? previous.add(request.value())
                : previous.multiply(BigDecimal.ONE.add(request.value().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)));
        newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);
        if (newPrice.signum() <= 0) throw new IllegalArgumentException("El precio resultante debe ser mayor a 0");
        return new PriceChangePreview(product.getId(), product.getInternalCode(), product.getName(), previous, newPrice);
    }

    private void validatePriceUpdate(PriceUpdateRequest request) {
        if (request == null || request.value() == null) throw new IllegalArgumentException("El ajuste es obligatorio");
        if (!"FIXED".equalsIgnoreCase(request.adjustmentType()) && !"PERCENTAGE".equalsIgnoreCase(request.adjustmentType()))
            throw new IllegalArgumentException("Tipo de ajuste no valido");
        if ("PERCENTAGE".equalsIgnoreCase(request.adjustmentType()) && request.value().compareTo(BigDecimal.valueOf(-100)) <= 0)
            throw new IllegalArgumentException("El porcentaje no puede reducir el precio a cero o menos");
    }

    private void validateProduct(ProductRequestDto dto) {
        if (dto.cost() != null && dto.cost().signum() <= 0) {
            throw new IllegalArgumentException("El costo debe ser mayor a 0");
        }
        if (dto.margin() != null && dto.margin().signum() < 0) {
            throw new IllegalArgumentException("El margen no puede ser negativo");
        }
        if (dto.salePrice() != null && dto.salePrice().signum() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a 0");
        }
        if (dto.minStock() != null && dto.minStock() < 0) {
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        }
    }
}
