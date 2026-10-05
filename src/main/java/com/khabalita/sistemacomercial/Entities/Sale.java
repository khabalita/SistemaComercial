package com.khabalita.sistemacomercial.Entities;

import com.khabalita.sistemacomercial.Entities.Base.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale extends AuditableEntity {

    @Column(name = "number", nullable = false, unique = true, length = 20)
    private String number;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    @Builder.Default
    private SalePaymentMethod paymentMethod = SalePaymentMethod.CASH;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(name = "general_discount_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal generalDiscountPercent = BigDecimal.ZERO;

    @Column(name = "general_discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal generalDiscountAmount = BigDecimal.ZERO;

    @Column(name = "iva_amount", precision = 12, scale = 2)
    private BigDecimal ivaAmount;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Item> items = new ArrayList<>();
}
