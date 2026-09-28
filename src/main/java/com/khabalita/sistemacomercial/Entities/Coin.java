package com.khabalita.sistemacomercial.Entities;

import com.khabalita.sistemacomercial.Entities.Base.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "coin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coin extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 3)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "symbol", length = 5)
    private String symbol;

    @Column(name = "present_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal presentValue;
}