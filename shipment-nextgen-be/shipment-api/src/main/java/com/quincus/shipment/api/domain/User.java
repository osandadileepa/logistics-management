package com.quincus.shipment.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String name;
    private String username;
    private String email;
    private String organizationId;
    private String partnerId;
    private String partner;
    private String mobileNo;
    private String fullPhoneNumber;
}
