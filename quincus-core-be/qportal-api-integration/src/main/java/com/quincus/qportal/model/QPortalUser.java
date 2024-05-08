package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
    private String partnerName;
    private List<QPortalPermission> permissions;
    private List<QPortalLocationCoverage> locations;
    private List<QPortalRole> roles;
    @JsonProperty("users_location")
    private QPortalUserLocation userLocation;
    private List<QPortalUserPartner> userPartners;

    public String getFullName() {
        return this.getFirstName() + " " + this.getLastName();
    }

    public String getUsername() {
        return StringUtils.isBlank(this.username) ? this.email : this.username;
    }

    public List<QPortalUserPartner> getAllUserPartners() {
        List<QPortalUserPartner> allUserPartners = new ArrayList<>();
        if (this.partnerId != null) {
            allUserPartners.add(new QPortalUserPartner(this.partnerId, this.partnerName));
        }
        allUserPartners.addAll(this.userPartners);
        return allUserPartners;
    }
}
