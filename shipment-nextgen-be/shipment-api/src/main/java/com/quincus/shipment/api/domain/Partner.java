package com.quincus.shipment.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Partner {
    private String id;
    @NotBlank
    private String organizationId;
    private String name;
    private String code;
    private String partnerType;
    private String locationType;
    private String contactName;
    private String contactNumber;
    private String contactCode;
    private String email;
    private Address address;
    private List<Facility> facilities;
    private List<Milestone> vendorEvents;
}