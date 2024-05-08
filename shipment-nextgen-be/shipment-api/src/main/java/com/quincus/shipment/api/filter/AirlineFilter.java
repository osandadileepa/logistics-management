package com.quincus.shipment.api.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AirlineFilter {
    @Size(max = 128)
    private String airlineName;
    private List<@Size(max = 64) String> flightNumbers;
}