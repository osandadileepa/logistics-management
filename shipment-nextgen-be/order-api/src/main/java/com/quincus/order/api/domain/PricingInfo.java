package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class PricingInfo {
    private String id;
    private Currency currency;
    private String currencyCode;
    private double baseTariff;
    private double serviceTypeCharge;
    private double surcharge;
    private double insuranceCharge;
    private double discount;
    private double tax;
    private double total;
    private double cod;
    private double subTotal;
    private double grandTotal;
}
