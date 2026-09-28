package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.dto.request.ProductRequestDto;
import com.khabalita.sistemacomercial.Entities.*;
import com.khabalita.sistemacomercial.Repositories.*;
import com.khabalita.sistemacomercial.dto.response.ProductResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CoinRepository coinRepository;
    private final IvaTypeRepository ivaTypeRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProviderRepository providerRepository;

    public ProductResponseDto toDto(Product entity) {
        if (entity == null) return null;
        return ProductResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .internalCode(entity.getInternalCode())
                .barCode(entity.getBarCode())
                .providerCode(entity.getProviderCode())
                .coinId(entity.getCoin() != null ? entity.getCoin().getId() : null)
                .coinCode(entity.getCoin() != null ? entity.getCoin().getCode() : null)
                .cost(entity.getCost())
                .margin(entity.getMargin())
                .salePrice(entity.getSalePrice() != null ? entity.getSalePrice() : calculateSalePrice(entity.getCost(), entity.getMargin(),
                        entity.getIvaType() != null ? entity.getIvaType().getPercentage() : null))
                .stock(entity.getStock())
                .minStock(entity.getMinStock())
                .ivaTypeId(entity.getIvaType() != null ? entity.getIvaType().getId() : null)
                .ivaTypeDescription(entity.getIvaType() != null ? entity.getIvaType().getDescription() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .brandId(entity.getBrand() != null ? entity.getBrand().getId() : null)
                .brandName(entity.getBrand() != null ? entity.getBrand().getName() : null)
                .providerId(entity.getProvider() != null ? entity.getProvider().getId() : null)
                .providerName(entity.getProvider() != null ? entity.getProvider().getName() : null)
                .hasImage(entity.getImages() != null && !entity.getImages().isEmpty())
                .build();
    }

    public Product toEntity(ProductRequestDto productRequestDto) {
        if (productRequestDto == null) return null;
        Product entity = new Product();
        entity.setName(productRequestDto.name());
        entity.setInternalCode(productRequestDto.internalCode());
        entity.setBarCode(productRequestDto.barCode());
        entity.setProviderCode(productRequestDto.providerCode());
        entity.setCost(productRequestDto.cost());
        entity.setMargin(productRequestDto.margin());
        IvaType ivaType = resolveOrThrow(ivaTypeRepository::findById, productRequestDto.ivaTypeId(), "IvaType");
        entity.setSalePrice(productRequestDto.salePrice() != null
                ? productRequestDto.salePrice()
                : calculateSalePrice(productRequestDto.cost(), productRequestDto.margin(), ivaType.getPercentage()));
        entity.setStock(productRequestDto.stock());
        entity.setMinStock(productRequestDto.minStock());
        entity.setCoin(resolveOrThrow(coinRepository::findById, productRequestDto.coinId(), "Coin"));
        entity.setIvaType(ivaType);
        entity.setCategory(resolveOrNull(categoryRepository::findById, productRequestDto.categoryId()));
        entity.setBrand(resolveOrNull(brandRepository::findById, productRequestDto.brandId()));
        entity.setProvider(resolveOrThrow(providerRepository::findById, productRequestDto.providerId(), "Provider"));
        return entity;
    }

    public List<ProductResponseDto> productResponseDtoList(List<Product> products) {
        if (products == null) return List.of();
        return products.stream()
                .map(this::toDto)
                .toList();
    }

    public BigDecimal calculateSalePrice(BigDecimal cost, BigDecimal margin, BigDecimal ivaPercentage) {
        if (cost == null || margin == null || ivaPercentage == null) return null;
        BigDecimal marginMultiplier = BigDecimal.ONE.add(
                margin.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal ivaMultiplier = BigDecimal.ONE.add(
                ivaPercentage.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return cost.multiply(marginMultiplier)
                .multiply(ivaMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private <T> T resolveOrThrow(java.util.function.Function<Long, java.util.Optional<T>> finder,
                                 Long id, String entityName) {
        if (id == null) {
            throw new EntityNotFoundException(entityName + " id is required");
        }
        return finder.apply(id).orElseThrow(() ->
                new EntityNotFoundException(entityName + " not found: " + id));
    }

    private <T> T resolveOrNull(java.util.function.Function<Long, java.util.Optional<T>> finder, Long id) {
        if (id == null) return null;
        return finder.apply(id).orElseThrow(() ->
                new EntityNotFoundException("Referenced entity not found: " + id));
    }
}
