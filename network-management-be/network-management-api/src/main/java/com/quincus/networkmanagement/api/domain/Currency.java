package com.quincus.networkmanagement.api.domain;

import com.quincus.networkmanagement.api.validator.constraint.ValidCurrency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ValidCurrency
public class Currency {
    private String id;
    private String name;
    private String code;
    private BigDecimal exchangeRate;
}
