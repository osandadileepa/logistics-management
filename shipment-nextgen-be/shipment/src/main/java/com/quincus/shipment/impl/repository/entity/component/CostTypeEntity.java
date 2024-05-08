package com.quincus.shipment.impl.repository.entity.component;

import com.quincus.shipment.api.constant.CostCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CostTypeEntity {
    private String id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private CostCategory category;
    private String proof;
    private String status;
}
