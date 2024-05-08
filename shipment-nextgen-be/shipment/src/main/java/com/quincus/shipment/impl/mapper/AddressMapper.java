package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public class AddressMapper {

    public static AddressEntity mapDomainToEntity(Address addressDomain) {
        if (addressDomain == null) {
            return null;
        }

        return mapDomainToEntity(addressDomain, new AddressEntity());
    }

    public static Address mapEntityToDomain(AddressEntity addressEntity) {
        if (addressEntity == null) {
            return null;
        }

        Address addressDomain = new Address();

        addressDomain.setExternalId(addressEntity.getExternalId());
        addressDomain.setLine1(addressEntity.getLine1());
        addressDomain.setLine2(addressEntity.getLine2());
        addressDomain.setLine3(addressEntity.getLine3());
        addressDomain.setPostalCode(addressEntity.getPostalCode());
        addressDomain.setLatitude(addressEntity.getLatitude());
        addressDomain.setLongitude(addressEntity.getLongitude());
        addressDomain.setFullAddress(addressEntity.getFullAddress());
        addressDomain.setManualCoordinates(addressEntity.getManualCoordinates() != null
                && addressEntity.getManualCoordinates());
        addressDomain.setCompany(addressEntity.getCompany());
        addressDomain.setDepartment(addressEntity.getDepartment());

        LocationHierarchyEntity locationHierarchyEntity = addressEntity.getLocationHierarchy();
        if (locationHierarchyEntity != null) {
            addressDomain.setId(locationHierarchyEntity.getId());
            LocationEntity countryLocation = locationHierarchyEntity.getCountry();
            if (nonNull(countryLocation)) {
                addressDomain.setCountryId(countryLocation.getExternalId());
                addressDomain.setCountry(countryLocation.getId());
                addressDomain.setCountryName(countryLocation.getName());
            }
            LocationEntity stateLocation = locationHierarchyEntity.getState();
            if (nonNull(stateLocation)) {
                addressDomain.setStateId(stateLocation.getExternalId());
                addressDomain.setState(stateLocation.getId());
                addressDomain.setStateName(stateLocation.getName());
            }
            LocationEntity cityLocation = locationHierarchyEntity.getCity();
            if (nonNull(cityLocation)) {
                addressDomain.setCityId(cityLocation.getExternalId());
                addressDomain.setCity(cityLocation.getId());
                addressDomain.setCityName(cityLocation.getName());
            }

        }

        return addressDomain;
    }

    public static AddressEntity mapDomainToEntity(Address addressDomain, AddressEntity addressEntity) {
        if (addressDomain == null) {
            return null;
        }
        addressEntity.setExternalId(addressDomain.getExternalId());
        addressEntity.setLine1(addressDomain.getLine1());
        addressEntity.setLine2(addressDomain.getLine2());
        addressEntity.setLine3(addressDomain.getLine3());
        addressEntity.setPostalCode(addressDomain.getPostalCode());
        addressEntity.setLatitude(addressDomain.getLatitude());
        addressEntity.setLongitude(addressDomain.getLongitude());
        addressEntity.setFullAddress(addressDomain.getFullAddress());
        addressEntity.setManualCoordinates(addressDomain.isManualCoordinates());
        addressEntity.setCompany(addressDomain.getCompany());
        addressEntity.setDepartment(addressDomain.getDepartment());
        return addressEntity;
    }
}
