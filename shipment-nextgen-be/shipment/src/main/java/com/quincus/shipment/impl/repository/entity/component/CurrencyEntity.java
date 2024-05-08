package com.quincus.shipment.impl.repository.entity.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CurrencyEntity {
    private String id;
    private String name;
    private String code;
    private String symbol;
}
