package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.Entities.Item;
import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.Entities.Sale;
import com.khabalita.sistemacomercial.dto.response.ItemResponseDto;
import com.khabalita.sistemacomercial.dto.response.SaleResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.math.BigDecimal;

@Component
public class SaleMapper {

    public SaleResponseDto toDto(Sale entity) {
        if (entity == null) return null;
        Customer customer = entity.getCustomer();
        List<ItemResponseDto> items = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream().map(this::toItemDto).toList();
        return SaleResponseDto.builder()
                .id(entity.getId())
                .number(entity.getNumber())
                .date(entity.getDate())
                .customerId(customer != null ? customer.getId() : null)
                .customerName(customer != null ? customer.getName() : null)
                .customerAddress(customer != null ? customer.getAddress() : null)
                .customerCity(customer != null ? customer.getCity() : null)
                .subtotal(entity.getSubtotal())
                .discount(entity.getDiscount())
                .generalDiscountPercent(entity.getGeneralDiscountPercent() != null
                        ? entity.getGeneralDiscountPercent() : BigDecimal.ZERO)
                .generalDiscountAmount(entity.getGeneralDiscountAmount() != null
                        ? entity.getGeneralDiscountAmount() : BigDecimal.ZERO)
                .ivaAmount(entity.getIvaAmount())
                .total(entity.getTotal())
                .notes(entity.getNotes())
                .items(items)
                .build();
    }

    public ItemResponseDto toItemDto(Item item) {
        if (item == null) return null;
        Product product = item.getProduct();
        return ItemResponseDto.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(item.getProductNameSnapshot() != null ? item.getProductNameSnapshot()
                        : product != null ? product.getName() : null)
                .productInternalCode(item.getProductCodeSnapshot() != null ? item.getProductCodeSnapshot()
                        : product != null ? product.getInternalCode() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineDiscount(item.getLineDiscount())
                .lineTotal(item.getLineTotal())
                .build();
    }

    public List<SaleResponseDto> saleResponseDtoList(List<Sale> sales) {
        if (sales == null) return List.of();
        return sales.stream().map(this::toDto).toList();
    }
}
