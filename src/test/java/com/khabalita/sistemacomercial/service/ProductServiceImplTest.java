package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.Entities.IvaType;
import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.Repositories.BrandRepository;
import com.khabalita.sistemacomercial.Repositories.CategoryRepository;
import com.khabalita.sistemacomercial.Repositories.CoinRepository;
import com.khabalita.sistemacomercial.Repositories.IvaTypeRepository;
import com.khabalita.sistemacomercial.Repositories.ProductRepository;
import com.khabalita.sistemacomercial.Repositories.ProviderRepository;
import com.khabalita.sistemacomercial.Service.Impl.ProductServiceImpl;
import com.khabalita.sistemacomercial.dto.request.ProductRequestDto;
import com.khabalita.sistemacomercial.dto.request.PriceUpdateRequest;
import com.khabalita.sistemacomercial.dto.response.ProductResponseDto;
import com.khabalita.sistemacomercial.mapper.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock ProductMapper productMapper;
    @Mock CoinRepository coinRepository;
    @Mock IvaTypeRepository ivaTypeRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock BrandRepository brandRepository;
    @Mock ProviderRepository providerRepository;

    @InjectMocks ProductServiceImpl service;

    private Product product;
    private ProductRequestDto requestDto;
    private ProductResponseDto responseDto;

    @BeforeEach
    void setUp() {
        Coin coin = Coin.builder().code("ARS").name("Peso Argentino")
                .symbol("$").presentValue(BigDecimal.ONE).build();
        coin.setId(1L);
        IvaType iva = IvaType.builder().description("IVA 21%")
                .percentage(new BigDecimal("21.00")).build();
        iva.setId(1L);
        Category cat = Category.builder().name("General").build();
        cat.setId(1L);
        Brand brand = Brand.builder().name("Sin marca").build();
        brand.setId(1L);
        Provider prov = Provider.builder().name("Proveedor A").build();
        prov.setId(1L);

        product = Product.builder()
                .name("Yerba Mate 1kg")
                .internalCode("YB-001")
                .coin(coin)
                .ivaType(iva)
                .category(cat)
                .brand(brand)
                .provider(prov)
                .cost(new BigDecimal("2500.00"))
                .margin(new BigDecimal("50.00"))
                .stock(30)
                .minStock(5)
                .build();
        product.setId(10L);

        requestDto = ProductRequestDto.builder()
                .name("Yerba Mate 1kg")
                .internalCode("YB-001")
                .coinId(1L)
                .ivaTypeId(1L)
                .categoryId(1L)
                .brandId(1L)
                .providerId(1L)
                .cost(new BigDecimal("2500.00"))
                .margin(new BigDecimal("50.00"))
                .stock(30)
                .minStock(5)
                .build();

        responseDto = ProductResponseDto.builder()
                .id(10L)
                .name("Yerba Mate 1kg")
                .internalCode("YB-001")
                .coinId(1L).coinCode("ARS")
                .cost(new BigDecimal("2500.00"))
                .margin(new BigDecimal("50.00"))
                .salePrice(new BigDecimal("3750.00"))
                .stock(30).minStock(5)
                .ivaTypeId(1L).ivaTypeDescription("IVA 21%")
                .categoryId(1L).categoryName("General")
                .brandId(1L).brandName("Sin marca")
                .providerId(1L).providerName("Proveedor A")
                .build();

        lenient().when(coinRepository.findById(1L)).thenReturn(Optional.of(coin));
        lenient().when(ivaTypeRepository.findById(1L)).thenReturn(Optional.of(iva));
        lenient().when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        lenient().when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        lenient().when(providerRepository.findById(1L)).thenReturn(Optional.of(prov));
    }

    @Test
    void saveProduct_happyPath_callsMapperAndRepo() {
        when(productMapper.toEntity(requestDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = service.saveProduct(requestDto);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("Yerba Mate 1kg", result.name());
        verify(productMapper).toEntity(requestDto);
        verify(productRepository).save(product);
        verify(productMapper).toDto(product);
    }

    @Test
    void previewPriceUpdate_byBrand_calculatesPercentage() {
        product.setSalePrice(new BigDecimal("100.00"));
        when(productRepository.findByBrand_Id(1L)).thenReturn(List.of(product));

        var result = service.previewPriceUpdate(new PriceUpdateRequest(1L, "PERCENTAGE", new BigDecimal("10")));

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("110.00"), result.get(0).newPrice());
    }

    @Test
    void applyPriceUpdate_general_updatesAllProducts() {
        product.setSalePrice(new BigDecimal("100.00"));
        when(productRepository.findAll()).thenReturn(List.of(product));

        int affected = service.applyPriceUpdate(new PriceUpdateRequest(null, "FIXED", new BigDecimal("25")));

        assertEquals(1, affected);
        assertEquals(new BigDecimal("125.00"), product.getSalePrice());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void saveProduct_propagatesMapperException() {
        when(productMapper.toEntity(requestDto))
                .thenThrow(new EntityNotFoundException("Coin not found: 1"));

        assertThrows(EntityNotFoundException.class, () -> service.saveProduct(requestDto));
        verify(productRepository, never()).save(any());
    }

    @Test
    void saveProduct_whenCategoryMissing_isAllowedSinceNullable() {
        ProductRequestDto dto = ProductRequestDto.builder()
                .name("Sin categoria").internalCode("SC-001")
                .coinId(1L).ivaTypeId(1L).providerId(1L)
                .cost(BigDecimal.TEN).margin(BigDecimal.ZERO).stock(0)
                .build();
        Product withoutCat = Product.builder().name("Sin categoria").build();
        withoutCat.setId(11L);

        when(productMapper.toEntity(dto)).thenReturn(withoutCat);
        when(productRepository.save(withoutCat)).thenReturn(withoutCat);
        when(productMapper.toDto(withoutCat)).thenReturn(
                ProductResponseDto.builder().id(11L).name("Sin categoria").build());

        ProductResponseDto result = service.saveProduct(dto);
        assertEquals(11L, result.id());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void getAllProducts_returnsMappedList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.productResponseDtoList(List.of(product))).thenReturn(List.of(responseDto));

        List<ProductResponseDto> result = service.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
    }

    @Test
    void getProductById_existing_returnsDto() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = service.getProductById(10L);

        assertEquals(10L, result.id());
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getProductById(99L));
    }

    @Test
    void getProductByBarCode_existing_returnsDto() {
        when(productRepository.findByBarCode("7790000001")).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = service.getProductByBarCode("7790000001");

        assertEquals(10L, result.id());
    }

    @Test
    void getProductByBarCode_notFound_throws() {
        when(productRepository.findByBarCode("999")).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.getProductByBarCode("999"));
    }

    @Test
    void deleteProductById_existing_deletes() {
        when(productRepository.existsById(10L)).thenReturn(true);

        service.deleteProductById(10L);

        verify(productRepository).deleteById(10L);
    }

    @Test
    void deleteProductById_notFound_throws() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.deleteProductById(99L));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void updateProduct_existing_updatesFieldsAndSaves() {
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));
        when(productMapper.toEntity(requestDto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = service.updateProduct(10L, requestDto);

        assertEquals(10L, result.id());
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals("Yerba Mate 1kg", saved.getName());
        assertEquals(30, saved.getStock());
    }

    @Test
    void updateProduct_notFound_throws() {
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateProduct(99L, requestDto));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStock_happyPath_updatesAndReturns() {
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        ProductResponseDto result = service.updateStock(10L, 99);

        assertNotNull(result);
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals(99, captor.getValue().getStock());
    }

    @Test
    void updateStock_negative_allowed() {
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        service.updateStock(10L, -1);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals(-1, captor.getValue().getStock());
    }

    @Test
    void updateStock_null_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.updateStock(10L, null));
    }

    @Test
    void updateStock_notFound_throws() {
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateStock(99L, 50));
    }

    @Test
    void saveProduct_negativeStock_allowed() {
        ProductRequestDto dto = ProductRequestDto.builder()
                .name("Negative").internalCode("N-001")
                .coinId(1L).ivaTypeId(1L).providerId(1L)
                .cost(BigDecimal.TEN).margin(BigDecimal.ZERO).stock(-5)
                .build();
        Product neg = Product.builder().name("Negative").stock(-5).build();
        neg.setId(11L);

        when(productMapper.toEntity(dto)).thenReturn(neg);
        when(productRepository.save(neg)).thenReturn(neg);
        when(productMapper.toDto(neg)).thenReturn(ProductResponseDto.builder().id(11L).name("Negative").stock(-5).build());

        ProductResponseDto result = service.saveProduct(dto);
        assertEquals(-5, result.stock());
    }

    @Test
    void saveProduct_negativeMinStock_throwsIllegalArgument() {
        ProductRequestDto bad = ProductRequestDto.builder()
                .name("Bad").internalCode("B-002")
                .coinId(1L).ivaTypeId(1L).providerId(1L)
                .cost(BigDecimal.TEN).margin(BigDecimal.ZERO).stock(2).minStock(-1)
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(bad));
    }

    @Test
    void findByCategoryName_delegatesToRepo() {
        when(productRepository.findByCategory_NameContainingIgnoreCase("lac")).thenReturn(List.of(product));
        when(productMapper.productResponseDtoList(List.of(product))).thenReturn(List.of(responseDto));

        assertEquals(1, service.getAllProductsByCategoryNameIgnoreCase("lac").size());
    }

    @Test
    void findByBarCode_delegatesToRepo() {
        when(productRepository.findByBarCode("7790000001")).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        assertEquals(10L, service.getProductByBarCode("7790000001").id());
    }
}
