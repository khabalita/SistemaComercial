package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.request.ProductRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProductResponseDto;
import com.khabalita.sistemacomercial.dto.request.PriceUpdateRequest;
import com.khabalita.sistemacomercial.dto.response.PriceChangePreview;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {

    List<ProductResponseDto> getAllProducts();
    List<ProductResponseDto> getAllProductsByCategoryNameIgnoreCase(String categoryName);
    List<ProductResponseDto> getAllProductsByBrandNameIgnoreCase(String brandName);
    List<ProductResponseDto> getAllProductsByProviderNameIgnoreCase(String providerName);
    List<ProductResponseDto> getAllProductsByNameIgnoreCase(String productName);
    Page<ProductResponseDto> getProductsPage(String name, String category, String brand,
                                              String provider, Pageable pageable);
    ProductResponseDto getProductById(Long id);
    ProductResponseDto getProductByBarCode(String barCode);
    void deleteProductById(Long id);
    ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto);
    ProductResponseDto updateStock(Long id, Integer stock);
    ProductResponseDto saveProduct(ProductRequestDto productRequestDto);
    List<PriceChangePreview> previewPriceUpdate(PriceUpdateRequest request);
    int applyPriceUpdate(PriceUpdateRequest request);

}
