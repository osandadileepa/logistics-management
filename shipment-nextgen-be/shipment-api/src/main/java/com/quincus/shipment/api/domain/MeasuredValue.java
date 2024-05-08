package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasuredValue {
    private Number value;
    private UnitOfMeasure uom;
}
