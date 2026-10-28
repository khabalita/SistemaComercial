package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.Entities.IvaType;
import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.dto.request.ProductRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProductResponseDto;
import com.khabalita.sistemacomercial.mapper.ProductMapper;
import com.khabalita.sistemacomercial.Repositories.BrandRepository;
import com.khabalita.sistemacomercial.Repositories.CategoryRepository;
import com.khabalita.sistemacomercial.Repositories.CoinRepository;
import com.khabalita.sistemacomercial.Repositories.IvaTypeRepository;
import com.khabalita.sistemacomercial.Repositories.ProviderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

class ProductMapperTest {

    private final CoinRepository coinRepo = mock(CoinRepository.class);
    private final IvaTypeRepository ivaRepo = mock(IvaTypeRepository.class);
    private final CategoryRepository catRepo = mock(CategoryRepository.class);
    private final BrandRepository brandRepo = mock(BrandRepository.class);
    private final ProviderRepository provRepo = mock(ProviderRepository.class);

    private final ProductMapper mapper = new ProductMapper(
            coinRepo, ivaRepo, catRepo, brandRepo, provRepo);

    @Test
    void calculateSalePrice_50PercentMarginAnd21PercentIva() {
        BigDecimal cost = new BigDecimal("2500.00");
        BigDecimal margin = new BigDecimal("50.00");

        BigDecimal result = mapper.calculateSalePrice(cost, margin, new BigDecimal("21.00"));

        assertEquals(0, new BigDecimal("4537.50").compareTo(result),
                "Expected 4537.50 but got " + result);
    }

    @Test
    void calculateSalePrice_zeroMargin_returnsCost() {
        BigDecimal result = mapper.calculateSalePrice(new BigDecimal("100"), BigDecimal.ZERO,
                new BigDecimal("21.00"));
        assertEquals(0, new BigDecimal("121.00").compareTo(result));
    }

    @Test
    void calculateSalePrice_25PercentMargin() {
        BigDecimal result = mapper.calculateSalePrice(new BigDecimal("80"), new BigDecimal("25"),
                BigDecimal.ZERO);
        assertEquals(0, new BigDecimal("100.00").compareTo(result));
    }

    @Test
    void calculateSalePrice_nullCost_returnsNull() {
        assertNull(mapper.calculateSalePrice(null, BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void calculateSalePrice_nullMargin_returnsNull() {
        assertNull(mapper.calculateSalePrice(BigDecimal.TEN, null, BigDecimal.ZERO));
    }

    @Test
    void toEntity_withoutSalePrice_calculatesFromCostMarginAndIva() {
        IvaType iva = IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21.00")).build();
        when(ivaRepo.findById(1L)).thenReturn(Optional.of(iva));
        when(coinRepo.findById(1L)).thenReturn(Optional.of(Coin.builder().code("ARS").build()));
        when(provRepo.findById(1L)).thenReturn(Optional.of(Provider.builder().name("Proveedor").build()));

        var dto = ProductRequestDto.builder()
                .name("Producto")
                .internalCode("P-001")
                .coinId(1L)
                .cost(new BigDecimal("100.00"))
                .margin(new BigDecimal("20.00"))
                .salePrice(null)
                .stock(0)
                .ivaTypeId(1L)
                .providerId(1L)
                .build();

        Product product = mapper.toEntity(dto);

        assertEquals(0, new BigDecimal("145.20").compareTo(product.getSalePrice()));
    }

    @Test
    void toEntity_keepsManuallyEnteredSalePrice() {
        IvaType iva = IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21.00")).build();
        when(ivaRepo.findById(1L)).thenReturn(Optional.of(iva));
        when(coinRepo.findById(1L)).thenReturn(Optional.of(Coin.builder().code("ARS").build()));
        when(provRepo.findById(1L)).thenReturn(Optional.of(Provider.builder().name("Proveedor").build()));

        var dto = ProductRequestDto.builder()
                .name("Producto")
                .internalCode("P-002")
                .coinId(1L)
                .cost(new BigDecimal("100.00"))
                .margin(new BigDecimal("20.00"))
                .salePrice(new BigDecimal("130.00"))
                .stock(0)
                .ivaTypeId(1L)
                .providerId(1L)
                .salePriceManualOverride(true)
                .build();

        Product product = mapper.toEntity(dto);

        assertEquals(0, new BigDecimal("130.00").compareTo(product.getSalePrice()));
    }

    @Test
    void toEntity_appliesProviderDiscountBeforeMarginAndIva() {
        IvaType iva = IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21.00")).build();
        Provider provider = Provider.builder().name("Proveedor").discountPercentage(new BigDecimal("10.00")).build();
        when(ivaRepo.findById(1L)).thenReturn(Optional.of(iva));
        when(coinRepo.findById(1L)).thenReturn(Optional.of(Coin.builder().code("ARS").build()));
        when(provRepo.findById(1L)).thenReturn(Optional.of(provider));

        var dto = ProductRequestDto.builder()
                .name("Producto con descuento")
                .internalCode("P-003")
                .coinId(1L)
                .cost(new BigDecimal("100.00"))
                .margin(new BigDecimal("20.00"))
                .salePrice(null)
                .stock(0)
                .ivaTypeId(1L)
                .providerId(1L)
                .build();

        Product product = mapper.toEntity(dto);

        assertEquals(0, new BigDecimal("90.00").compareTo(product.getDiscountedCost()));
        assertEquals(0, new BigDecimal("10.00").compareTo(product.getProviderDiscountPercentageApplied()));
        assertEquals(0, new BigDecimal("130.68").compareTo(product.getSalePrice()));
    }

    @Test
    void toDto_withAllRelations_populatesDerivedFields() {
        Coin coin = Coin.builder().code("ARS").name("Peso").build();
        coin.setId(1L);
        IvaType iva = IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21.00")).build();
        iva.setId(1L);
        Category cat = Category.builder().name("General").build();
        cat.setId(1L);
        Brand brand = Brand.builder().name("Sin marca").build();
        brand.setId(1L);
        Provider prov = Provider.builder().name("Prov").build();
        prov.setId(1L);
        Product p = Product.builder()
                .name("Test").internalCode("T-001")
                .coin(coin).ivaType(iva).category(cat).brand(brand).provider(prov)
                .cost(new BigDecimal("100")).margin(new BigDecimal("50"))
                .stock(10).minStock(2)
                .build();
        p.setId(1L);

        ProductResponseDto dto = mapper.toDto(p);

        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("Test", dto.name());
        assertEquals("ARS", dto.coinCode());
        assertEquals("IVA 21%", dto.ivaTypeDescription());
        assertEquals("General", dto.categoryName());
        assertEquals("Sin marca", dto.brandName());
        assertEquals("Prov", dto.providerName());
        assertEquals(0, new BigDecimal("181.50").compareTo(dto.salePrice()));
    }

    @Test
    void toDto_usdProductIncludesArsDisplayEquivalent() {
        Coin usd = Coin.builder().code("USD").presentValue(new BigDecimal("1250.00")).build();
        IvaType iva = IvaType.builder().description("Exento").percentage(BigDecimal.ZERO).build();
        Product product = Product.builder()
                .name("Producto USD")
                .internalCode("USD-001")
                .coin(usd)
                .ivaType(iva)
                .cost(new BigDecimal("10.00"))
                .discountedCost(new BigDecimal("10.00"))
                .providerDiscountPercentageApplied(BigDecimal.ZERO)
                .margin(BigDecimal.ZERO)
                .salePrice(new BigDecimal("12.50"))
                .stock(1)
                .build();

        ProductResponseDto dto = mapper.toDto(product);

        assertEquals(0, new BigDecimal("15625.00").compareTo(dto.salePriceArsEquivalent()));
    }

    @Test
    void toDto_nullEntity_returnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_withMissingCategoryAndBrand_stillWorks() {
        Coin coin = Coin.builder().code("ARS").build();
        coin.setId(1L);
        IvaType iva = IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21.00")).build();
        iva.setId(1L);
        Provider prov = Provider.builder().name("Prov").build();
        prov.setId(1L);
        Product p = Product.builder()
                .name("Sin cat/brand").internalCode("X-001")
                .coin(coin).ivaType(iva).provider(prov)
                .cost(new BigDecimal("100")).margin(new BigDecimal("0"))
                .stock(0)
                .build();
        p.setId(2L);

        ProductResponseDto dto = mapper.toDto(p);

        assertNull(dto.categoryId());
        assertNull(dto.categoryName());
        assertNull(dto.brandId());
        assertNull(dto.brandName());
    }
}
