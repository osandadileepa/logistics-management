package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class QPortalUser {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    @JsonProperty("organisation_id")
    private String organizationId;
    @JsonProperty("organisation_name")
    private String organizationName;
    private String partnerId;
    private String partner;
    private List<QPortalPermission> permissions;
    private List<QPortalLocationCoverage> locations;
    private List<QPortalRole> roles;
    @JsonProperty("users_location")
    private QPortalUserLocation userLocation;
    @JsonProperty("facility")
    private QPortalFacility userFacility;
    private List<QPortalUserPartner> userPartners;
    private String mobileNo;
    @JsonProperty("mobile_phonecode")
    private MobilePhoneCode mobilePhoneCode;

    public String getFullName() {
        return this.getFirstName() + " " + this.getLastName();
    }

    public String getUsername() {
        return StringUtils.isBlank(this.username) ? this.email : this.username;
    }

    public String getFullPhoneNumber() {
        return Optional.ofNullable(mobilePhoneCode).map(MobilePhoneCode::getPhonecode).orElse(StringUtils.EMPTY) +
                Optional.ofNullable(mobileNo).orElse(StringUtils.EMPTY);
    }

    public List<QPortalUserPartner> getAllUserPartners() {
        List<QPortalUserPartner> allUserPartners = new ArrayList<>();
        if (this.partnerId != null) {
            allUserPartners.add(new QPortalUserPartner(this.partnerId, this.partner));
        }
        allUserPartners.addAll(this.userPartners);
        return allUserPartners;
    }

    @Data
    private static class MobilePhoneCode {
        private String id;
        private String name;
        private String locationCode;
        private String phonecode;
        private int minLength;
    }
}
