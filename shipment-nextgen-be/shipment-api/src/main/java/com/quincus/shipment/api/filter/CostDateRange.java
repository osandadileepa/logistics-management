package com.quincus.shipment.api.filter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CostDateRange {
    private LocalDateTime incurredDateFrom;
    private LocalDateTime incurredDateTo;
}
