package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class Shipper {
    private String name;
    private String phone;
    private ShipperPhoneCodeId shipperPhoneCodeId;
    private String secondaryPhone;
    private ShipperPhoneCodeId shipperSecondaryPhoneCodeId;
    private String email;
}
