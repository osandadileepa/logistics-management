package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class Currency {
    private String id;
    private String name;
    private boolean deleted;
    private String createdAt;
    private String updatedAt;
    private String code;
    private String organisationId;
    private boolean isDefaultCurrency;
    private double exchangeRate;
}
