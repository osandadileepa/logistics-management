package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {
    @JsonAlias({"lat","latitude"})
    private BigDecimal lat;
    @JsonAlias({"lon","longitude"})
    private BigDecimal lon;
}
