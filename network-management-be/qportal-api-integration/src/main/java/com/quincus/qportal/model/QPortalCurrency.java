package com.quincus.qportal.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QPortalCurrency {
    private String id;
    private String name;
    private BigDecimal exchangeRate;
    private String code;
    private String isBase;
    private String status;
    private String statusI18n;
    private String symbol;
    private String flag;
    private String masterCurrencyId;
    private String format;
    private String userLogo;
}
