package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class Consignee {
    private String name;
    private String phone;
    private ConsigneePhoneCodeId consigneePhoneCodeId;
    private String secondaryPhone;
    private ConsigneePhoneCodeId consigneeSecondaryPhoneCodeId;
    private String email;
}
