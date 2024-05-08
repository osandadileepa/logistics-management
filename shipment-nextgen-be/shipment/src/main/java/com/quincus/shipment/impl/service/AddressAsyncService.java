package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.impl.mapper.AddressMapper;
import com.quincus.shipment.impl.repository.AddressRepository;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import liquibase.repackaged.org.apache.commons.text.WordUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class AddressAsyncService {

    private static final String QPORTAL_ANCESTOR_DELIMITER = ", ";
    private static final String INVALID_CITY = "City:`%s` is not valid.";
    private static final String QPORTAL_ERR_MSG = "Error retrieving QPortal location details with city %s.";

    private final QPortalApi qPortalApi;

    private final LocationHierarchyAsyncService locationHierarchyAsyncService;
    private final AddressRepository addressRepository;


    public Address generateCityAddressFromQPortal(final String cityName, String ancestorStateName, String ancestorCountryName, final String organizationId) {

        QPortalLocation qPortalCityLocation = extractQPortalLocationCity(getQPortalLocation(cityName, organizationId), ancestorStateName, ancestorCountryName);
        if (qPortalCityLocation == null || !qPortalCityLocation.getLocationType().equalsIgnoreCase(LocationType.CITY.name())) {
            throw new QuincusValidationException(String.format(INVALID_CITY, cityName));
        }

        Address cityAddress = new Address();
        cityAddress.setCountryId(qPortalCityLocation.getCountryId());
        cityAddress.setCityName(qPortalCityLocation.getName());
        cityAddress.setCityId(qPortalCityLocation.getId());
        cityAddress.setStateId(qPortalCityLocation.getStateProvinceId());
        cityAddress.setLine1(qPortalCityLocation.getAddress1());
        cityAddress.setLine2(qPortalCityLocation.getAddress2());
        cityAddress.setLine3(qPortalCityLocation.getAddress3());
        cityAddress.setExternalId(qPortalCityLocation.getId());

        String[] ancestors = WordUtils.capitalize(qPortalCityLocation.getAncestors().toLowerCase()).split(QPORTAL_ANCESTOR_DELIMITER);
        String countryValueFromAncestors = ancestors[0];
        String stateValueFromAncestors = ancestors[1];
        cityAddress.setCountryName(countryValueFromAncestors);
        cityAddress.setStateName(stateValueFromAncestors);

        return cityAddress;
    }

    private QPortalLocation extractQPortalLocationCity(List<QPortalLocation> qPortalLocations, String ancestorStateName, String ancestorCountryName) {
        // City name can be duplicate hence correctly finding QPortal locations with same state and country from input/csv
        if (CollectionUtils.isEmpty(qPortalLocations)) {
            return null;
        }
        if (qPortalLocations.size() == 1) {
            return qPortalLocations.get(0);
        }
        // if no ancestor is similar just get the first and let the validation from the service validate
        return qPortalLocations.stream().filter(qPortalLocation ->
                qPortalLocation.getLocationType().equalsIgnoreCase(LocationType.CITY.name())
                        && isQPortalLocationAncestorEqualsToStateAndCountry(qPortalLocation, ancestorStateName, ancestorCountryName)
        ).findFirst().orElse(qPortalLocations.get(0));
    }

    private boolean isQPortalLocationAncestorEqualsToStateAndCountry(QPortalLocation qPortalLocation, String ancestorStateName, String ancestorCountryName) {
        if (StringUtils.isBlank(qPortalLocation.getAncestors())) {
            return false;
        }

        String[] ancestors = WordUtils.capitalize(qPortalLocation.getAncestors().toLowerCase()).split(QPORTAL_ANCESTOR_DELIMITER);
        if (ancestors.length < 2) {
            return false;
        }
        return ancestors[0].equalsIgnoreCase(ancestorCountryName) &&
                ancestors[1].equalsIgnoreCase(ancestorStateName);
    }


    private List<QPortalLocation> getQPortalLocation(final String cityName, final String organizationId) {
        if (StringUtils.isBlank(cityName)) return Collections.emptyList();
        List<QPortalLocation> facilityQPortalLocation;
        try {
            facilityQPortalLocation = qPortalApi.getLocationsByName(organizationId, cityName);
        } catch (Exception exception) {
            throw new QuincusException(String.format(QPORTAL_ERR_MSG, cityName));
        }

        return facilityQPortalLocation;
    }

    @Transactional
    public AddressEntity createAddressEntityWithFacility(final Address addressDomain,
                                                         final String facilityName,
                                                         final String organizationId) {
        if (isNull(addressDomain) || (StringUtils.isBlank(addressDomain.getCountryId())
                && StringUtils.isBlank(addressDomain.getStateId()) && StringUtils.isBlank(addressDomain.getCity()))) {
            return null;
        }
        Facility facility = new Facility();
        facility.setId(addressDomain.getId());
        facility.setName(facilityName);
        facility.setLocation(addressDomain);
        LocationHierarchyEntity locationHierarchy = locationHierarchyAsyncService.setUpLocationHierarchies(facility, organizationId);
        return saveAddress(addressDomain, locationHierarchy);
    }

    @Transactional
    public AddressEntity saveAddress(Address addressDomain, LocationHierarchyEntity locationHierarchy) {
        if (addressDomain == null || locationHierarchy == null || StringUtils.isBlank(locationHierarchy.getId()))
            return null;
        AddressEntity addressEntity = addressRepository.findByLocationHierarchyId(locationHierarchy.getId());
        if (addressEntity != null) return addressEntity;
        addressEntity = AddressMapper.mapDomainToEntity(addressDomain);
        addressEntity.setLocationHierarchy(locationHierarchy);
        return addressRepository.saveAndFlush(addressEntity);
    }
}