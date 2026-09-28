package com.khabalita.sistemacomercial.Entities;

import com.khabalita.sistemacomercial.Entities.Base.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_discount", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal lineDiscount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "product_name_snapshot", length = 200)
    private String productNameSnapshot;

    @Column(name = "product_code_snapshot", length = 50)
    private String productCodeSnapshot;

    @Column(name = "cost_snapshot", precision = 12, scale = 2)
    private BigDecimal costSnapshot;

    @Column(name = "margin_snapshot", precision = 6, scale = 2)
    private BigDecimal marginSnapshot;

    @Column(name = "iva_percentage_snapshot", precision = 5, scale = 2)
    private BigDecimal ivaPercentageSnapshot;
}
