package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.Entities.IvaType;
import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.Repositories.BrandRepository;
import com.khabalita.sistemacomercial.Repositories.CategoryRepository;
import com.khabalita.sistemacomercial.Repositories.CoinRepository;
import com.khabalita.sistemacomercial.Repositories.IvaTypeRepository;
import com.khabalita.sistemacomercial.Repositories.ProductRepository;
import com.khabalita.sistemacomercial.Repositories.ProviderRepository;
import com.khabalita.sistemacomercial.dto.response.ProductImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImportServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CoinRepository coinRepository;
    @Mock IvaTypeRepository ivaTypeRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock BrandRepository brandRepository;
    @Mock ProviderRepository providerRepository;

    private ProductImportService service;
    private Coin coin;
    private IvaType iva;
    private Provider provider;

    @BeforeEach
    void setUp() {
        service = new ProductImportService(productRepository, coinRepository, ivaTypeRepository,
                categoryRepository, brandRepository, providerRepository);
        coin = Coin.builder().code("ARS").name("Peso").build();
        iva = IvaType.builder().description("Exento").percentage(BigDecimal.ZERO).build();
        provider = Provider.builder().name("Proveedor").taxId("30-123").build();
        lenient().when(productRepository.findByInternalCodeIgnoreCase("A-001")).thenReturn(Optional.empty());
        lenient().when(coinRepository.findByCodeIgnoreCase("ARS")).thenReturn(coin);
        lenient().when(ivaTypeRepository.findByDescriptionIgnoreCase("Exento")).thenReturn(iva);
        lenient().when(providerRepository.findByNameIgnoreCase("Proveedor")).thenReturn(provider);
    }

    @Test
    void validate_validCsv_doesNotPersist() {
        ProductImportResult result = service.validate(file(validRow()));

        assertTrue(result.valid(), result.errors().toString());
        assertEquals(1, result.acceptedRows());
        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void import_invalidRelation_doesNotPersist() {
        when(providerRepository.findByNameIgnoreCase("Proveedor inexistente")).thenReturn(null);

        ProductImportResult result = service.importProducts(file(validRow().replace("Proveedor", "Proveedor inexistente")));

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("proveedor no existe")));
        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void import_optionalColumnsEmpty_usesDefaults() {
        ProductImportResult result = service.importProducts(file(
                        String.join(",", ProductImportService.HEADERS) + "\n"
                        + "Producto,A-001,,,1000,,10,,,,\n"));

        assertTrue(result.valid(), result.errors().toString());
        ArgumentCaptor<java.util.List<com.khabalita.sistemacomercial.Entities.Product>> captor =
                ArgumentCaptor.forClass(java.util.List.class);
        verify(productRepository).saveAll(captor.capture());
        com.khabalita.sistemacomercial.Entities.Product product = captor.getValue().get(0);
        assertEquals(BigDecimal.ZERO, product.getMargin());
        assertEquals(iva, product.getIvaType());
        assertNull(product.getProvider());
    }

    @Test
    void import_validCsv_persistsOneProduct() {
        ProductImportResult result = service.importProducts(file(validRow()));

        assertTrue(result.valid(), result.errors().toString());
        ArgumentCaptor<java.util.List<com.khabalita.sistemacomercial.Entities.Product>> captor =
                ArgumentCaptor.forClass(java.util.List.class);
        verify(productRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals("A-001", captor.getValue().get(0).getInternalCode());
    }

    private MockMultipartFile file(String content) {
        return new MockMultipartFile("file", "products.csv", "text/csv", content.getBytes());
    }

    private String validRow() {
        return String.join(",", ProductImportService.HEADERS) + "\n"
                + "Producto,A-001,,,1000,1000,10,2,,,Proveedor\n";
    }
}
