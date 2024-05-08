package com.quincus.shipment.impl.mapper;

import com.quincus.order.api.domain.Location;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.NONE)
@Slf4j
public class OMLocationMapper {
    public static Facility mapLocationToFacility(String facilityId, Location location, String locationName) {
        if (StringUtils.isBlank(facilityId) && location == null) return null;
        Facility facility = new Facility();
        String id = facilityId != null ? facilityId : location.getId();
        String code = (location == null) ? id : locationName;
        facility.setName(code);
        facility.setCode(code);
        facility.setExternalId(id);
        //set address to null since we are only given the facility ext id per segment
        //this would be set in FacilityService.java
        facility.setLocation(mapToAddress(location));
        return facility;
    }

    public static Address mapToAddress(Location location) {
        if (location == null) {
            return null;
        }
        Address address = new Address();
        address.setExternalId(location.getId());
        address.setCountryName(location.getCountry());
        address.setStateName(location.getState());
        address.setCityName(location.getCity());
        address.setLine1(location.getAddressLine1());
        address.setLine2(location.getAddressLine2());
        address.setLine3(location.getAddressLine3());
        address.setCountryId(location.getCountryId());
        address.setStateId(location.getStateId());
        address.setCityId(location.getCityId());
        address.setPostalCode(location.getPostalCode());
        address.setLatitude(location.getLatitude());
        address.setLongitude(location.getLongitude());
        address.setManualCoordinates(location.isManualCoordinates());
        address.setFullAddress(location.getAddress());
        address.setCompany(location.getCompany());
        address.setDepartment(location.getDepartment());
        return address;
    }
}
