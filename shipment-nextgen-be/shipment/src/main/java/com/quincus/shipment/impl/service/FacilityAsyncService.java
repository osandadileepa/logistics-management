package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import liquibase.repackaged.org.apache.commons.text.WordUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class FacilityAsyncService {
    private static final String QPORTAL_ANCESTOR_DELIMITER = ", ";
    private static final String QPORTAL_FACILITY_NAME_ERR_MSG = "Error retrieving QPortal location details with Facility Name %s.";
    private static final String QPORTAL_FACILITY_ID_ERR_MSG = "Error retrieving QPortal location details with Id %s.";
    private static final String INVALID_FACILITY = "Facility Name: `%s` is not a valid Facility.";
    private final QPortalApi qPortalApi;

    public Facility generateFacilityFromQPortal(final String facilityName, final String organizationId) {

        QPortalLocation facilityQPortalLocation = extractCorrectQPortalFacility(getQPortalLocations(facilityName, organizationId));
        if (facilityQPortalLocation == null) {
            throw new QuincusValidationException(String.format(INVALID_FACILITY, facilityName));
        }
        Facility domainFacility = new Facility();
        domainFacility.setName(facilityQPortalLocation.getName());
        domainFacility.setCode(facilityQPortalLocation.getCode());
        domainFacility.setLocationCode(facilityQPortalLocation.getLocationCode());
        domainFacility.setExternalId(facilityQPortalLocation.getId());

        Address facilityAddress = setUpFacilityAddressFromLocationAncestors(facilityQPortalLocation, organizationId);
        facilityAddress.setCountryId(facilityQPortalLocation.getCountryId());
        facilityAddress.setCityId(facilityQPortalLocation.getCityId());
        facilityAddress.setStateId(facilityQPortalLocation.getStateProvinceId());
        facilityAddress.setLine1(facilityQPortalLocation.getAddress1());
        facilityAddress.setLine2(facilityQPortalLocation.getAddress2());
        facilityAddress.setLine3(facilityQPortalLocation.getAddress3());
        domainFacility.setLocation(facilityAddress);
        return domainFacility;
    }

    private QPortalLocation extractCorrectQPortalFacility(List<QPortalLocation> qPortalLocations) {
        if (CollectionUtils.isEmpty(qPortalLocations)) {
            return null;
        }
        // Facility name should be unique so filter list only to check if QPortal location data is facility
        return qPortalLocations.stream().filter(qPortalLocation ->
                qPortalLocation.getLocationType().equalsIgnoreCase(LocationType.FACILITY.name())).findFirst().orElse(null);
    }

    private List<QPortalLocation> getQPortalLocations(final String name, final String organizationId) {
        if (StringUtils.isBlank(name)) return Collections.emptyList();
        try {
            return qPortalApi.getLocationsByName(organizationId, name);
        } catch (Exception exception) {
            throw new QuincusException(String.format(QPORTAL_FACILITY_NAME_ERR_MSG, name));
        }
    }

    private Address setUpFacilityAddressFromLocationAncestors(final QPortalLocation facilityQPortalLocation, final String organizationId) {
        Address facilityAddress = new Address();
        String[] ancestors = extractAncestors(facilityQPortalLocation);
        if (ancestors.length != 3) {
            setUpFacilityLocationFromQPortal(facilityAddress, facilityQPortalLocation, organizationId);
            return facilityAddress;
        }
        String countryValueFromAncestors = ancestors[0];
        String stateValueFromAncestors = ancestors[1];
        String cityValueFromAncestors = ancestors[2];
        facilityAddress.setCountryName(countryValueFromAncestors);
        facilityAddress.setStateName(stateValueFromAncestors);
        facilityAddress.setCityName(cityValueFromAncestors);
        return facilityAddress;
    }

    private String[] extractAncestors(final QPortalLocation facilityQPortalLocation) {
        if (StringUtils.isBlank(facilityQPortalLocation.getAncestors())) {
            return new String[]{};
        }
        return WordUtils.capitalize(facilityQPortalLocation.getAncestors()).split(QPORTAL_ANCESTOR_DELIMITER);
    }

    private void setUpFacilityLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation, final String organizatioId) {
        setUpFacilityCityLocationFromQPortal(location, facilityQPortalLocation, organizatioId);
        setUpFacilityStateLocationFromQPortal(location, facilityQPortalLocation, organizatioId);
        setUpFacilityCountryLocationFromQPortal(location, facilityQPortalLocation, organizatioId);
    }

    private void setUpFacilityCountryLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation, final String organizatioId) {
        QPortalLocation countryQPortalLocation = getQPortalLocationById(facilityQPortalLocation.getCountryId(), organizatioId);
        location.setCountryId(countryQPortalLocation.getId());
        location.setCountryName(countryQPortalLocation.getName());
    }

    private void setUpFacilityStateLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation, final String organizatioId) {
        QPortalLocation stateQPortalLocation = getQPortalLocationById(facilityQPortalLocation.getStateProvinceId(), organizatioId);
        location.setStateId(stateQPortalLocation.getId());
        location.setStateName(stateQPortalLocation.getName());
    }

    private void setUpFacilityCityLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation, final String organizatioId) {
        QPortalLocation cityQPortalLocation = getQPortalLocationById(facilityQPortalLocation.getCityId(), organizatioId);
        location.setCityId(cityQPortalLocation.getId());
        location.setCityName(cityQPortalLocation.getName());
    }

    private QPortalLocation getQPortalLocationById(final String locationId, final String organizationId) {
        QPortalLocation qPortalLocation;
        try {
            qPortalLocation = qPortalApi.getLocation(locationId, organizationId);
        } catch (Exception exception) {
            throw new QuincusException(String.format(QPORTAL_FACILITY_ID_ERR_MSG, locationId));
        }
        if (isNull(qPortalLocation)) {
            throw new QuincusException(String.format(QPORTAL_FACILITY_ID_ERR_MSG, locationId));
        }
        return qPortalLocation;
    }
}