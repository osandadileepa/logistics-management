package com.quincus.qportal.model;

import lombok.Data;

@Data
public class QPortalCurrency {
    private String id;
    private String name;
    private String exchangeRate;
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
