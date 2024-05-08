package com.quincus.networkmanagement.impl.repository.entity.embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class FacilityEmbeddable {
    private String id;
    private String name;
    private String code;
    private BigDecimal lat;
    private BigDecimal lon;
}
