package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.Entities.Item;
import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.Entities.Sale;
import com.khabalita.sistemacomercial.Entities.SaleSequence;
import com.khabalita.sistemacomercial.Entities.SalePaymentMethod;
import com.khabalita.sistemacomercial.Repositories.CustomerRepository;
import com.khabalita.sistemacomercial.Repositories.ProductRepository;
import com.khabalita.sistemacomercial.Repositories.SaleRepository;
import com.khabalita.sistemacomercial.Repositories.SaleSequenceRepository;
import com.khabalita.sistemacomercial.Service.ISaleService;
import com.khabalita.sistemacomercial.Service.ICustomerAccountService;
import com.khabalita.sistemacomercial.dto.request.ItemRequestDto;
import com.khabalita.sistemacomercial.dto.request.SaleRequestDto;
import com.khabalita.sistemacomercial.dto.response.SaleResponseDto;
import com.khabalita.sistemacomercial.mapper.ProductMapper;
import com.khabalita.sistemacomercial.mapper.SaleMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements ISaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ProductMapper productMapper;
    private final SaleMapper saleMapper;
    private final SaleSequenceRepository saleSequenceRepository;
    private final ICustomerAccountService customerAccountService;

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDto> getAllSales() {
        return saleMapper.saleResponseDtoList(saleRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponseDto getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found: " + id));
        return saleMapper.toDto(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDto> getSalesByCustomerId(Long customerId) {
        return saleMapper.saleResponseDtoList(saleRepository.findByCustomerId(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDto> getSaleByProductId(Long productId) {
        return saleMapper.saleResponseDtoList(saleRepository.findDistinctByItems_Product_Id(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDto> getSaleByCustomerName(String customerName) {
        return saleMapper.saleResponseDtoList(
                saleRepository.findByCustomer_NameContainingIgnoreCase(customerName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDto> getSaleByProductName(String productName) {
        return saleMapper.saleResponseDtoList(
                saleRepository.findDistinctByItems_Product_NameContainingIgnoreCase(productName));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SaleResponseDto> getSalesPage(String customerName, String productName, Pageable pageable) {
        String customerFilter = normalizeFilter(customerName);
        String productFilter = normalizeFilter(productName);
        Page<Long> idPage = saleRepository.findIdsByFilters(customerFilter, productFilter, pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Sale> sales = saleRepository.findByIdIn(idPage.getContent());
        Map<Long, Sale> salesById = new LinkedHashMap<>();
        sales.forEach(sale -> salesById.put(sale.getId(), sale));
        List<SaleResponseDto> content = idPage.getContent().stream()
                .map(salesById::get)
                .filter(sale -> sale != null)
                .map(saleMapper::toDto)
                .toList();
        return new org.springframework.data.domain.PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    @Override
    @Transactional
    @Audited("SALE_CREATE")
    public synchronized SaleResponseDto createSale(SaleRequestDto dto) {
        validateItems(dto);

        Sale sale = Sale.builder()
                .date(LocalDateTime.now())
                .paymentMethod(dto.paymentMethod() == null ? SalePaymentMethod.CASH : dto.paymentMethod())
                .notes(dto.notes())
                .subtotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .generalDiscountPercent(normalizeGeneralDiscount(dto.generalDiscountPercent()))
                .generalDiscountAmount(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        if (dto.customerId() != null) {
            Customer customer = customerRepository.findById(dto.customerId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Customer not found: " + dto.customerId()));
            sale.setCustomer(customer);
        }

        if (sale.getPaymentMethod() == SalePaymentMethod.CURRENT_ACCOUNT && sale.getCustomer() == null) {
            throw new IllegalArgumentException("La venta a cuenta corriente requiere un cliente");
        }

        sale.setNumber(generateNextNumber());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;

        for (ItemRequestDto itemReq : dto.items()) {
            Product product = productRepository.findByIdForUpdate(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product not found: " + itemReq.productId()));

            BigDecimal lineDiscount = itemReq.lineDiscount() == null
                    ? BigDecimal.ZERO
                    : itemReq.lineDiscount();

            BigDecimal unitPrice = product.getSalePrice() != null
                    ? product.getSalePrice()
                    : productMapper.calculateSalePrice(product.getCost(), product.getMargin(),
                    product.getIvaType().getPercentage());

            BigDecimal lineTotal = calculateLineTotal(
                    unitPrice, itemReq.quantity(), lineDiscount);
            BigDecimal grossLineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));

            Item item = Item.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemReq.quantity())
                     .unitPrice(unitPrice)
                     .lineDiscount(lineDiscount)
                     .lineTotal(lineTotal)
                     .productNameSnapshot(product.getName())
                     .productCodeSnapshot(product.getInternalCode())
                     .costSnapshot(product.getCost())
                     .marginSnapshot(product.getMargin())
                     .ivaPercentageSnapshot(product.getIvaType().getPercentage())
                     .build();
            sale.getItems().add(item);

            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            subtotal = subtotal.add(lineTotal);
            discountTotal = discountTotal.add(grossLineTotal.subtract(lineTotal));
        }

        sale.setSubtotal(subtotal.add(discountTotal).setScale(2, RoundingMode.HALF_UP));
        sale.setDiscount(discountTotal.setScale(2, RoundingMode.HALF_UP));
        BigDecimal generalDiscountAmount = subtotal
                .multiply(sale.getGeneralDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        sale.setGeneralDiscountAmount(generalDiscountAmount);
        sale.setTotal(subtotal.subtract(generalDiscountAmount).setScale(2, RoundingMode.HALF_UP));
        sale.setIvaAmount(calculateIvaAmount(sale));

        Sale saved = saleRepository.save(sale);
        if (saved.getPaymentMethod() == SalePaymentMethod.CURRENT_ACCOUNT) {
            customerAccountService.registerSaleDebit(saved);
        }
        return saleMapper.toDto(saved);
    }

    private void validateItems(SaleRequestDto dto) {
        if (dto.items() == null || dto.items().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un item");
        }
        BigDecimal generalDiscount = normalizeGeneralDiscount(dto.generalDiscountPercent());
        if (generalDiscount.signum() < 0 || generalDiscount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El descuento general debe estar entre 0% y 100%");
        }
        for (ItemRequestDto item : dto.items()) {
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            if (item.lineDiscount() != null && item.lineDiscount().signum() < 0) {
                throw new IllegalArgumentException("El descuento no puede ser negativo");
            }
            if (item.lineDiscount() != null && item.lineDiscount().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("El descuento no puede superar el 100%");
            }
        }
    }

    private BigDecimal normalizeGeneralDiscount(BigDecimal discount) {
        return discount == null ? BigDecimal.ZERO : discount;
    }

    private BigDecimal calculateIvaAmount(Sale sale) {
        BigDecimal factor = BigDecimal.ONE.subtract(
                sale.getGeneralDiscountPercent().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal iva = BigDecimal.ZERO;
        for (Item item : sale.getItems()) {
            BigDecimal percentage = item.getIvaPercentageSnapshot();
            if (percentage == null || percentage.signum() == 0) continue;
            BigDecimal finalLineTotal = item.getLineTotal().multiply(factor);
            iva = iva.add(finalLineTotal.multiply(percentage)
                    .divide(BigDecimal.valueOf(100).add(percentage), 2, RoundingMode.HALF_UP));
        }
        return iva.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BigDecimal calculateLineTotal(BigDecimal unitPrice, Integer quantity, BigDecimal lineDiscountPercent) {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
                lineDiscountPercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return gross.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateNextNumber() {
        SaleSequence sequence = saleSequenceRepository.findByIdForUpdate(1L)
                .orElseThrow(() -> new IllegalStateException("No existe la secuencia de ventas"));
        long next = sequence.getNextNumber();
        sequence.setNextNumber(next + 1);
        saleSequenceRepository.save(sequence);
        return String.format("V-%05d", next);
    }
}
