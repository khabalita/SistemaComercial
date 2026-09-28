package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.Entities.IvaType;
import com.khabalita.sistemacomercial.Entities.Item;
import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.Entities.Sale;
import com.khabalita.sistemacomercial.Entities.SaleSequence;
import com.khabalita.sistemacomercial.Repositories.CustomerRepository;
import com.khabalita.sistemacomercial.Repositories.ProductRepository;
import com.khabalita.sistemacomercial.Repositories.SaleRepository;
import com.khabalita.sistemacomercial.Repositories.SaleSequenceRepository;
import com.khabalita.sistemacomercial.Service.Impl.SaleServiceImpl;
import com.khabalita.sistemacomercial.dto.request.ItemRequestDto;
import com.khabalita.sistemacomercial.dto.request.SaleRequestDto;
import com.khabalita.sistemacomercial.dto.response.ItemResponseDto;
import com.khabalita.sistemacomercial.dto.response.SaleResponseDto;
import com.khabalita.sistemacomercial.mapper.ProductMapper;
import com.khabalita.sistemacomercial.mapper.SaleMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock SaleRepository saleRepository;
    @Mock ProductRepository productRepository;
    @Mock CustomerRepository customerRepository;
    @Mock ProductMapper productMapper;
    @Mock SaleMapper saleMapper;
    @Mock SaleSequenceRepository saleSequenceRepository;

    @InjectMocks SaleServiceImpl service;

    private Coin coin;
    private IvaType iva;
    private Provider provider;
    private Product product;

    @BeforeEach
    void setUp() {
        coin = Coin.builder().code("ARS").name("Peso").build();
        coin.setId(1L);
        iva = IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21")).build();
        iva.setId(1L);
        provider = Provider.builder().name("Prov").build();
        provider.setId(1L);

        product = Product.builder()
                .name("Yerba").internalCode("YB-001")
                .coin(coin).ivaType(iva).provider(provider)
                .cost(new BigDecimal("1000"))
                .margin(new BigDecimal("50"))
                .stock(100)
                .build();
        product.setId(1L);

        lenient().when(productMapper.calculateSalePrice(product.getCost(), product.getMargin(), iva.getPercentage()))
                .thenReturn(new BigDecimal("1815.00"));
        lenient().when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(saleSequenceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(
                SaleSequence.builder().nextNumber(1L).build()));
        lenient().when(saleSequenceRepository.save(any(SaleSequence.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            if (s.getId() == null) s.setId(1L);
            return s;
        });
    }

    private SaleRequestDto req(Long customerId, ItemRequestDto... items) {
        return SaleRequestDto.builder()
                .customerId(customerId)
                .notes("nota test")
                .items(List.of(items))
                .build();
    }

    private ItemRequestDto item(Long productId, Integer qty, BigDecimal discount) {
        return ItemRequestDto.builder().productId(productId).quantity(qty).lineDiscount(discount).build();
    }

    @Test
    void createSale_decrementsStockAndCalculatesTotals() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(saleMapper.toDto(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            return SaleResponseDto.builder()
                    .id(s.getId()).number(s.getNumber())
                    .subtotal(s.getSubtotal()).total(s.getTotal())
                    .items(new ArrayList<>())
                    .build();
        });

        SaleRequestDto dto = req(null,
                item(1L, 2, BigDecimal.ZERO),
                item(1L, 3, new BigDecimal("10")));

        SaleResponseDto result = service.createSale(dto);

        assertNotNull(result);
        ArgumentCaptor<Product> prodCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(prodCaptor.capture());
        Product updated = prodCaptor.getValue();
        assertEquals(95, updated.getStock(), "stock should be 100 - 2 - 3");

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(saleCaptor.capture());
        Sale saved = saleCaptor.getValue();
        assertEquals(0, new BigDecimal("9075.00").compareTo(saved.getSubtotal()),
                "expected subtotal 5*1815 = 9075.00");
        assertEquals(0, new BigDecimal("544.50").compareTo(saved.getDiscount()),
                "expected discount 3*1815*0.1 = 544.50");
        assertEquals(0, new BigDecimal("8530.50").compareTo(saved.getTotal()),
                "expected total after discount = 8530.50");
    }

    @Test
    void createSale_appliesGeneralDiscountAfterLineDiscounts() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(clone(product)));
        when(saleMapper.toDto(any(Sale.class))).thenAnswer(inv -> SaleResponseDto.builder()
                .id(1L).items(new ArrayList<>()).build());

        SaleRequestDto dto = SaleRequestDto.builder()
                .items(List.of(item(1L, 2, new BigDecimal("10"))))
                .generalDiscountPercent(new BigDecimal("10"))
                .build();

        service.createSale(dto);

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(captor.capture());
        Sale saved = captor.getValue();
        assertEquals(new BigDecimal("3630.00"), saved.getSubtotal());
        assertEquals(new BigDecimal("363.00"), saved.getDiscount());
        assertEquals(new BigDecimal("326.70"), saved.getGeneralDiscountAmount());
        assertEquals(new BigDecimal("2940.30"), saved.getTotal());
        assertEquals(new BigDecimal("510.30"), saved.getIvaAmount());
    }

    @Test
    void createSale_generalDiscountOverOneHundred_throwsIllegalArgument() {
        SaleRequestDto dto = SaleRequestDto.builder()
                .items(List.of(item(1L, 1, BigDecimal.ZERO)))
                .generalDiscountPercent(new BigDecimal("100.01"))
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.createSale(dto));
        verifyNoInteractions(productRepository, saleRepository);
    }

    @Test
    void createSale_usesProductSalePriceSnapshot() {
        Product p = clone(product);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(saleMapper.toDto(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            return SaleResponseDto.builder().id(s.getId()).items(new ArrayList<>()).build();
        });

        SaleRequestDto dto = req(null, item(1L, 1, BigDecimal.ZERO));
        service.createSale(dto);

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(captor.capture());
        Item persisted = captor.getValue().getItems().get(0);
        assertEquals(0, new BigDecimal("1815.00").compareTo(persisted.getUnitPrice()));
    }

    @Test
    void createSale_allowsNegativeStock() {
        Product p = clone(product);
        p.setStock(2);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(saleMapper.toDto(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            return SaleResponseDto.builder().id(s.getId()).items(new ArrayList<>()).build();
        });

        SaleRequestDto dto = req(null, item(1L, 5, BigDecimal.ZERO));
        service.createSale(dto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals(-3, captor.getValue().getStock());
    }

    @Test
    void createSale_productNotFound_throws() {
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        SaleRequestDto dto = req(null, item(99L, 1, BigDecimal.ZERO));
        assertThrows(EntityNotFoundException.class, () -> service.createSale(dto));
        verify(saleRepository, never()).save(any());
    }

    @Test
    void createSale_customerNotFound_throws() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        SaleRequestDto dto = req(99L, item(1L, 1, BigDecimal.ZERO));
        assertThrows(EntityNotFoundException.class, () -> service.createSale(dto));
        verify(saleRepository, never()).save(any());
    }

    @Test
    void createSale_noItems_throwsIllegalArgument() {
        SaleRequestDto dto = SaleRequestDto.builder().items(List.of()).build();
        assertThrows(IllegalArgumentException.class, () -> service.createSale(dto));
    }

    @Test
    void createSale_quantityZero_throwsIllegalArgument() {
        SaleRequestDto dto = req(null, item(1L, 0, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> service.createSale(dto));
    }

    @Test
    void createSale_negativeDiscount_throwsIllegalArgument() {
        SaleRequestDto dto = req(null, item(1L, 1, new BigDecimal("-5")));
        assertThrows(IllegalArgumentException.class, () -> service.createSale(dto));
    }

    @Test
    void createSale_discountOverOneHundred_throwsIllegalArgument() {
        SaleRequestDto dto = req(null, item(1L, 1, new BigDecimal("100.01")));

        assertThrows(IllegalArgumentException.class, () -> service.createSale(dto));
        verifyNoInteractions(productRepository, saleRepository);
    }

    @Test
    void createSale_nullDiscount_defaultsToZero() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(clone(product)));
        when(saleMapper.toDto(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            return SaleResponseDto.builder().id(s.getId()).items(new ArrayList<>()).build();
        });

        SaleRequestDto dto = req(null, item(1L, 1, null));
        service.createSale(dto);

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(captor.capture());
        Item persisted = captor.getValue().getItems().get(0);
        assertEquals(0, BigDecimal.ZERO.compareTo(persisted.getLineDiscount()));
    }

    @Test
    void createSale_generatesSequentialNumber() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(clone(product)));
        when(saleMapper.toDto(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            return SaleResponseDto.builder().id(s.getId()).items(new ArrayList<>()).build();
        });

        service.createSale(req(null, item(1L, 1, BigDecimal.ZERO)));

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(captor.capture());
        assertEquals("V-00001", captor.getValue().getNumber());
    }

    @Test
    void createSale_continuesFromLastNumber() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(clone(product)));
        SaleSequence sequence = SaleSequence.builder().nextNumber(8L).build();
        sequence.setId(1L);
        when(saleSequenceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sequence));
        when(saleMapper.toDto(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            return SaleResponseDto.builder().id(s.getId()).items(new ArrayList<>()).build();
        });

        service.createSale(req(null, item(1L, 1, BigDecimal.ZERO)));

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(captor.capture());
        assertEquals("V-00008", captor.getValue().getNumber());
    }

    @Test
    void getAll_returnsMappedList() {
        when(saleRepository.findAll()).thenReturn(List.of(new Sale()));
        when(saleMapper.saleResponseDtoList(any())).thenReturn(List.of(
                SaleResponseDto.builder().id(1L).number("V-00001").items(new ArrayList<>()).build()));
        assertEquals(1, service.getAllSales().size());
    }

    @Test
    void getById_existing_returnsDto() {
        Sale s = Sale.builder().number("V-00001").items(new ArrayList<>()).build();
        s.setId(1L);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(s));
        when(saleMapper.toDto(s)).thenReturn(
                SaleResponseDto.builder().id(1L).number("V-00001").items(new ArrayList<>()).build());

        assertEquals("V-00001", service.getSaleById(1L).number());
    }

    @Test
    void getById_notFound_throws() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getSaleById(99L));
    }

    @Test
    void getByCustomer_delegatesToRepo() {
        when(saleRepository.findByCustomerId(5L)).thenReturn(List.of(new Sale()));
        when(saleMapper.saleResponseDtoList(any())).thenReturn(List.of());
        assertEquals(0, service.getSalesByCustomerId(5L).size());
        verify(saleRepository).findByCustomerId(5L);
    }

    @Test
    void getSalesPage_loadsDetailsInTheIdPageOrder() {
        Sale first = Sale.builder().number("V-00001").items(new ArrayList<>()).build();
        first.setId(1L);
        Sale second = Sale.builder().number("V-00002").items(new ArrayList<>()).build();
        second.setId(2L);
        Page<Long> ids = new PageImpl<>(List.of(2L, 1L), PageRequest.of(0, 50), 2);
        when(saleRepository.findIdsByFilters("juan", "yerba", PageRequest.of(0, 50)))
                .thenReturn(ids);
        when(saleRepository.findByIdIn(List.of(2L, 1L))).thenReturn(List.of(first, second));
        when(saleMapper.toDto(first)).thenReturn(SaleResponseDto.builder().id(1L).build());
        when(saleMapper.toDto(second)).thenReturn(SaleResponseDto.builder().id(2L).build());

        Page<SaleResponseDto> result = service.getSalesPage(" juan ", "yerba", PageRequest.of(0, 50));

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(2L, 1L), result.getContent().stream().map(SaleResponseDto::id).toList());
    }

    private Product clone(Product p) {
        Product copy = Product.builder()
                .name(p.getName())
                .internalCode(p.getInternalCode())
                .coin(p.getCoin())
                .ivaType(p.getIvaType())
                .category(p.getCategory() != null ? Category.builder().name(p.getCategory().getName()).build() : null)
                .brand(p.getBrand() != null ? Brand.builder().name(p.getBrand().getName()).build() : null)
                .provider(p.getProvider())
                .cost(p.getCost())
                .margin(p.getMargin())
                .stock(p.getStock())
                .build();
        copy.setId(p.getId());
        return copy;
    }
}
