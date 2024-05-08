package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.util.List;

@Data
public class QPortalPartner {
    private String id;
    private String name;
    private String partnerCode;
    private String partnerCategory;
    private String partnerType;
    private String status;
    private String contactName;
    @JsonSetter("phone_no")
    private String contactNumber;
    @JsonSetter("phonecode")
    private String contactCode;
    private String email;
    private String postalCode;
    @JsonSetter("deleted_at")
    private String deletedTime;
    private String organizationId;
    private String locationId;
    private String locationType;
    @JsonSetter("business_address1")
    private String addressLine;
    @JsonSetter("location_ancestors")
    private QPortalAddress location;
    @JsonSetter("managed_locations")
    private List<QPortalFacility> facilities;
}
