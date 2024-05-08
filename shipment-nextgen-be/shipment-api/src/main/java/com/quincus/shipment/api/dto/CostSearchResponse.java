package com.quincus.shipment.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CostSearchResponse {
    private String id;
    private String costType;
    private BigDecimal costAmount;
    private String currencySymbol;
    private String currencyCode;
    private String incurredBy;
    private LocalDateTime incurredByDate;
    private String incurredByTimezone;
    private String vendor;
}
