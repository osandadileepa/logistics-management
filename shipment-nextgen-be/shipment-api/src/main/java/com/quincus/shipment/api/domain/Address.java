package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Address {
    private String id;
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String locationHierarchyId;
    private String country;
    private String state;
    private String city;
    private String countryId;
    private String stateId;
    private String cityId;
    @NotBlank
    private String countryName;
    @NotBlank
    private String stateName;
    @NotBlank
    private String cityName;
    @Size(max = 200, message = "Maximum of 200 characters allowed.")
    private String line1;
    @Size(max = 200, message = "Maximum of 200 characters allowed.")
    private String line2;
    @Size(max = 200, message = "Maximum of 200 characters allowed.")
    private String line3;
    private String postalCode;
    private String latitude;
    private String longitude;
    private boolean manualCoordinates;
    private String fullAddress;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String externalId;
    private String company;
    private String department;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
