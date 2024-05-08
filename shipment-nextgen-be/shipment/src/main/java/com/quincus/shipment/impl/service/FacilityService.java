package com.quincus.shipment.impl.service;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.exception.SegmentException;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import liquibase.repackaged.org.apache.commons.text.WordUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FacilityService {
    private static final String QPORTAL_ANCESTOR_DELIMITER = ", ";
    private static final String QPORTAL_ERR_MSG = "Error retrieving QPortal location details with id %s.";
    private final LocationHierarchyService locationHierarchyService;
    private final QPortalService qPortalService;
    private final AddressService addressService;
    private final UserDetailsProvider userDetailsProvider;

    @Transactional
    public void assignFacilityToPackageJourneySegments(final ShipmentJourneyEntity shipmentJourneyEntity,
                                                       final ShipmentJourney shipmentJourneyDomain) {
        if ((isNull(shipmentJourneyEntity)) || (isNull(shipmentJourneyDomain))
                || (CollectionUtils.isEmpty(shipmentJourneyDomain.getPackageJourneySegments()))
                || (CollectionUtils.isEmpty(shipmentJourneyEntity.getPackageJourneySegments()))) {
            return;
        }
        List<PackageJourneySegment> domainJourneySegments = shipmentJourneyDomain.getPackageJourneySegments();
        List<PackageJourneySegmentEntity> entityJourneySegments = shipmentJourneyEntity.getPackageJourneySegments();

        IntStream.range(0, entityJourneySegments.size()).
                forEach(i -> {
                    setupLocationHierarchy(domainJourneySegments.get(i), entityJourneySegments.get(i));
                    setupFlightOriginAndDestination(domainJourneySegments.get(i), entityJourneySegments.get(i));
                });
    }

    @Transactional
    public void setupLocationHierarchy(final PackageJourneySegment packageJourneySegment,
                                       final PackageJourneySegmentEntity packageJourneySegmentEntity) {
        Facility startFacility = nonNull(packageJourneySegment) ? packageJourneySegment.getStartFacility() : null;
        Facility endFacility = nonNull(packageJourneySegment) ? packageJourneySegment.getEndFacility() : null;
        packageJourneySegmentEntity.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        Address startAddress = setUpAddress(startFacility);
        Address endAddress = setUpAddress(endFacility);
        locationHierarchyService.setUpLocationHierarchies(startAddress, startFacility, endAddress, endFacility, packageJourneySegmentEntity);
        addressService.saveAddress(startAddress, packageJourneySegmentEntity.getStartLocationHierarchy());
        addressService.saveAddress(endAddress, packageJourneySegmentEntity.getEndLocationHierarchy());
    }

    public void enrichFacilityWithLocationFromQPortal(final Facility facility) {
        if (facility == null) return;
        facility.setLocation(setUpAddress(facility));
    }

    private Address setUpAddress(final Facility domainFacility) {
        if (isNull(domainFacility)) return null;
        Address location = domainFacility.getLocation();
        String externalId = isNull(domainFacility.getExternalId()) ? domainFacility.getCode() : domainFacility.getExternalId();

        if (isNull(location) && StringUtils.isNotBlank(externalId)) {
            setUpFacilityFromQPortal(domainFacility, externalId);
            location = domainFacility.getLocation();
        }
        return location;
    }

    /**
     * Domain Facility address is set in OrderToShipmentMapper.createFacility().
     * The first segment would always have the value of the shipment origin as the pick-up location
     * and the last segment would always have the shipment destination as the pick-up location
     * pick up and drop off facilities in between the origin and destination would not have an address and would always be set
     * to null in  OrderToShipmentMapper.createFacility(),
     * We try to set up the facility  by calling QPortal here.
     * Setting the location based on the ancestors value
     * and the name and code from name and code fields
     *
     * @param domainFacility the domain facility
     * @param externalId     the QPortal location id
     */
    private void setUpFacilityFromQPortal(final Facility domainFacility,
                                          final String externalId) {
        QPortalLocation facilityQPortalLocation;
        facilityQPortalLocation = getQPortalLocation(externalId);
        if (facilityQPortalLocation == null) return;

        domainFacility.setName(facilityQPortalLocation.getName());
        domainFacility.setCode(facilityQPortalLocation.getCode());
        domainFacility.setLocationCode(facilityQPortalLocation.getLocationCode());
        domainFacility.setExternalId(facilityQPortalLocation.getId());

        Address facilityAddress = setUpFacilityAddressFromLocationAncestors(facilityQPortalLocation);
        facilityAddress.setCountryId(facilityQPortalLocation.getCountryId());
        facilityAddress.setCityId(facilityQPortalLocation.getCityId());
        facilityAddress.setStateId(facilityQPortalLocation.getStateProvinceId());
        facilityAddress.setLine1(facilityQPortalLocation.getAddress1());
        facilityAddress.setLine2(facilityQPortalLocation.getAddress2());
        facilityAddress.setLine3(facilityQPortalLocation.getAddress3());
        domainFacility.setLocation(facilityAddress);
    }

    private QPortalLocation getQPortalLocation(final String externalId) {
        if (StringUtils.isBlank(externalId)) return null;
        QPortalLocation facilityQPortalLocation;
        try {
            facilityQPortalLocation = qPortalService.getLocation(externalId);
        } catch (Exception exception) {
            throw new SegmentException(String.format(QPORTAL_ERR_MSG, externalId));
        }

        if (isNull(facilityQPortalLocation)) {
            throw new SegmentException(String.format(QPORTAL_ERR_MSG, externalId));
        }
        return facilityQPortalLocation;
    }

    /**
     * One of the fields that QPortal send us is the location ancestors
     * It has this format "COUNTRY, STATE,CITY"
     * here is the sample from QPortal
     * "ancestors": "PHILIPPINES, METRO MANILA, QUEZON CITY"
     * In order to generate the address we first need set the ancestors value to lower case
     * then capitalize every first Letter of each word
     * we then split the values using the comma delimiter
     * <p>
     * If we don't have any value from the ancestors attribute
     * We call QPortal to get the country , city and state
     * <p>
     * If everything else fail, throw SegmentException
     *
     * @param facilityQPortalLocation location details from QPortal
     *
     * @return facility domain address
     */
    private Address setUpFacilityAddressFromLocationAncestors(final QPortalLocation facilityQPortalLocation) {
        Address facilityAddress = new Address();
        if (StringUtils.isBlank(facilityQPortalLocation.getAncestors())) {
            setUpFacilityLocationFromQPortal(facilityAddress, facilityQPortalLocation);
            return facilityAddress;
        }
        String[] ancestors = WordUtils.capitalize(facilityQPortalLocation.getAncestors().toLowerCase()).split(QPORTAL_ANCESTOR_DELIMITER);
        if (ancestors.length != 3) {
            setUpFacilityLocationFromQPortal(facilityAddress, facilityQPortalLocation);
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

    private void setUpFacilityLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation) {
        setUpFacilityCityLocationFromQPortal(location, facilityQPortalLocation);
        setUpFacilityStateLocationFromQPortal(location, facilityQPortalLocation);
        setUpFacilityCountryLocationFromQPortal(location, facilityQPortalLocation);
    }

    private void setUpFacilityCountryLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation) {
        QPortalLocation countryQPortalLocation = getQPortalLocation(facilityQPortalLocation);
        location.setCountryId(countryQPortalLocation.getId());
        location.setCountryName(countryQPortalLocation.getName());
    }

    private void setUpFacilityStateLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation) {
        QPortalLocation stateQPortalLocation = getQPortalLocation(facilityQPortalLocation);
        location.setStateId(stateQPortalLocation.getId());
        location.setStateName(stateQPortalLocation.getName());
    }

    private void setUpFacilityCityLocationFromQPortal(final Address location, final QPortalLocation facilityQPortalLocation) {
        QPortalLocation cityQPortalLocation = getQPortalLocation(facilityQPortalLocation);
        location.setCityId(cityQPortalLocation.getId());
        location.setCityName(cityQPortalLocation.getName());
    }

    private QPortalLocation getQPortalLocation(final QPortalLocation facilityQPortalLocation) {
        QPortalLocation qPortalLocation;
        try {
            qPortalLocation = qPortalService.getLocation(facilityQPortalLocation.getId());
        } catch (Exception exception) {
            throw new SegmentException(String.format(QPORTAL_ERR_MSG, facilityQPortalLocation.getId()));
        }
        if (isNull(qPortalLocation)) {
            throw new SegmentException(String.format(QPORTAL_ERR_MSG, facilityQPortalLocation.getId()));
        }
        return qPortalLocation;
    }

    public void setupFlightOriginAndDestination(final PackageJourneySegment packageJourneySegment,
                                                final PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (packageJourneySegment == null || packageJourneySegment.getTransportType() != TransportType.AIR) return;
        packageJourneySegmentEntity.setFlightOrigin(getLocationCode(packageJourneySegment.getStartFacility()));
        packageJourneySegmentEntity.setFlightDestination(getLocationCode(packageJourneySegment.getEndFacility()));
    }

    private String getLocationCode(Facility facility) {
        if (facility == null) return null;
        if (StringUtils.isNotBlank(facility.getLocationCode())) return facility.getLocationCode();
        String externalId = isNull(facility.getExternalId()) ? facility.getCode() : facility.getExternalId();
        QPortalLocation facilityQPortalLocation = getQPortalLocation(externalId);
        return facilityQPortalLocation != null ? facilityQPortalLocation.getLocationCode() : null;
    }
}