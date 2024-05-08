package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AddressMapperTest {

    @Test
    void mapDomainToEntity_addressDomain_shouldReturnAddressEntity() {
        Address domain = new Address();
        domain.setId("address1");
        domain.setExternalId("ext-1");
        domain.setCityId("city-1");
        domain.setCity("city");
        domain.setStateId("state-1");
        domain.setState("state");
        domain.setCountryId("country-1");
        domain.setCountry("country");
        domain.setPostalCode("12345");
        domain.setLine1("LINE1");
        domain.setLine2("LINE2");
        domain.setLine3("LINE3");
        domain.setFullAddress("address string");
        domain.setLatitude("latitude");
        domain.setLongitude("longitude");
        domain.setManualCoordinates(true);
        domain.setCompany("company");
        domain.setDepartment("department");

        final AddressEntity entity = AddressMapper.mapDomainToEntity(domain);

        assertThat(entity.getId()).withFailMessage("Address ID was also mapped.").isNull();
        assertThat(entity.getLocationHierarchy()).withFailMessage("Location Hierarchy was prematurely set.").isNull();
        assertThat(entity.getExternalId()).withFailMessage("External ID mismatch.").isEqualTo(domain.getExternalId());
        assertThat(entity.getLine1()).withFailMessage("Address Line 1 mismatch.").isEqualTo(domain.getLine1());
        assertThat(entity.getLine2()).withFailMessage("Address Line 2 mismatch.").isEqualTo(domain.getLine2());
        assertThat(entity.getLine3()).withFailMessage("Address Line 3 mismatch.").isEqualTo(domain.getLine3());
        assertThat(entity.getPostalCode()).withFailMessage("Postal Code mismatch.").isEqualTo(domain.getPostalCode());
        assertThat(entity.getLatitude()).withFailMessage("Latitude mismatch.").isEqualTo(domain.getLatitude());
        assertThat(entity.getLongitude()).withFailMessage("Longitude mismatch.").isEqualTo(domain.getLongitude());
        assertThat(entity.getManualCoordinates()).withFailMessage("Is Manual Coordinates flag mismatch.")
                .isEqualTo(domain.isManualCoordinates());
        assertThat(entity.getFullAddress()).withFailMessage("Full Address mismatch.").isEqualTo(domain.getFullAddress());
        assertThat(entity.getCompany()).withFailMessage("Company mismatch.").isEqualTo(domain.getCompany());
        assertThat(entity.getDepartment()).withFailMessage("Department mismatch.").isEqualTo(domain.getDepartment());
    }

    @Test
    void mapDomainToEntity_addressDomainNull_shouldReturnNull() {
        assertThat(AddressMapper.mapDomainToEntity(null)).isNull();
    }

    @Test
    void mapEntityToDomain_addressEntity_shouldReturnAddressDomain() {
        AddressEntity entity = new AddressEntity();
        entity.setExternalId("EXT-1");
        entity.setLine1("-line1-");
        entity.setLine2("-line2-");
        entity.setLine3("-line3-");
        entity.setPostalCode("54321");
        entity.setLatitude("LATITUDE");
        entity.setLongitude("LONGITUDE");
        entity.setManualCoordinates(true);
        entity.setFullAddress("-full address-");
        entity.setCompany("company");
        entity.setDepartment("department");
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();

        String expectedId = "LOC-H-1";
        locationHierarchyEntity.setId(expectedId);

        LocationEntity countryLocation = new LocationEntity();
        String expectedCountryId = "country1";
        String expectedCountry = "COUNTRY";
        countryLocation.setExternalId(expectedCountryId);
        countryLocation.setCode(expectedCountryId);
        countryLocation.setName(expectedCountry);
        countryLocation.setId(expectedCountry);
        locationHierarchyEntity.setCountry(countryLocation);

        LocationEntity stateLocation = new LocationEntity();
        String expectedStateId = "state1";
        String expectedState = "STATE";
        stateLocation.setExternalId(expectedStateId);
        stateLocation.setCode(expectedStateId);
        stateLocation.setName(expectedState);
        stateLocation.setId(expectedState);
        locationHierarchyEntity.setState(stateLocation);

        LocationEntity cityLocation = new LocationEntity();
        String expectedCityId = "city1";
        String expectedCity = "CITY";
        cityLocation.setExternalId(expectedCityId);
        cityLocation.setCode(expectedCityId);
        cityLocation.setName(expectedCity);
        cityLocation.setId(expectedCity);
        locationHierarchyEntity.setCity(cityLocation);

        entity.setLocationHierarchy(locationHierarchyEntity);

        final Address domain = AddressMapper.mapEntityToDomain(entity);

        assertThat(domain.getId()).withFailMessage("Address ID mismatch.").isEqualTo(expectedId);
        assertThat(domain.getExternalId()).withFailMessage("External ID mismatch.").isEqualTo(entity.getExternalId());
        assertThat(domain.getLine1()).withFailMessage("Address Line 1 mismatch.").isEqualTo(entity.getLine1());
        assertThat(domain.getLine2()).withFailMessage("Address Line 2 mismatch.").isEqualTo(entity.getLine2());
        assertThat(domain.getLine3()).withFailMessage("Address Line 3 mismatch.").isEqualTo(entity.getLine3());
        assertThat(domain.getPostalCode()).withFailMessage("Postal Code mismatch.").isEqualTo(entity.getPostalCode());
        assertThat(domain.getCountryId()).withFailMessage("Country ID mismatch.").isEqualTo(expectedCountryId);
        assertThat(domain.getCountry()).withFailMessage("Country mismatch.").isEqualTo(expectedCountry);
        assertThat(domain.getStateId()).withFailMessage("State ID mismatch.").isEqualTo(expectedStateId);
        assertThat(domain.getState()).withFailMessage("State mismatch.").isEqualTo(expectedState);
        assertThat(domain.getCityId()).withFailMessage("City ID mismatch.").isEqualTo(expectedCityId);
        assertThat(domain.getCity()).withFailMessage("City mismatch.").isEqualTo(expectedCity);
        assertThat(domain.getPostalCode()).withFailMessage("Address Postal Code mismatch.").isEqualTo(entity.getPostalCode());
        assertThat(domain.getFullAddress()).withFailMessage("Full Address mismatch.").isEqualTo(entity.getFullAddress());
        assertThat(domain.getLatitude()).withFailMessage("Latitude mismatch.").isEqualTo(entity.getLatitude());
        assertThat(domain.getLongitude()).withFailMessage("Longitude mismatch.").isEqualTo(entity.getLongitude());
        assertThat(domain.isManualCoordinates()).withFailMessage("Manual Coordinates flag mismatch.")
                .isEqualTo(entity.getManualCoordinates());
        assertThat(domain.getCompany()).withFailMessage("Company mismatch.").isEqualTo(entity.getCompany());
        assertThat(domain.getDepartment()).withFailMessage("Department mismatch.").isEqualTo(entity.getDepartment());
    }

    @Test
    void mapEntityToDomain_addressEntityNoLocationHierarchy_shouldReturnAddressDomain() {
        AddressEntity entity = new AddressEntity();
        entity.setExternalId("EXT-A");
        entity.setLine1("-line1-");
        entity.setLine2("-line2-");
        entity.setLine3("-line3-");
        entity.setPostalCode("54321");
        entity.setLatitude("LATITUDE");
        entity.setLongitude("LONGITUDE");
        entity.setManualCoordinates(true);
        entity.setFullAddress("-full address-");
        entity.setLocationHierarchy(null);
        entity.setCompany("company");
        entity.setDepartment("department");

        final Address domain = AddressMapper.mapEntityToDomain(entity);

        assertThat(domain.getId()).isNull();
        assertThat(domain.getExternalId()).withFailMessage("External ID mismatch.").isEqualTo(entity.getExternalId());
        assertThat(domain.getLine1()).withFailMessage("Address Line 1 mismatch.").isEqualTo(entity.getLine1());
        assertThat(domain.getLine2()).withFailMessage("Address Line 2 mismatch.").isEqualTo(entity.getLine2());
        assertThat(domain.getLine3()).withFailMessage("Address Line 3 mismatch.").isEqualTo(entity.getLine3());
        assertThat(domain.getPostalCode()).withFailMessage("Postal Code mismatch.").isEqualTo(entity.getPostalCode());
        assertThat(domain.getCity()).withFailMessage("Address City was also mapped.").isNull();
        assertThat(domain.getCityId()).withFailMessage("Address City ID was also mapped.").isNull();
        assertThat(domain.getState()).withFailMessage("Address State was also mapped.").isNull();
        assertThat(domain.getStateId()).withFailMessage("Address State ID was also mapped.").isNull();
        assertThat(domain.getCountry()).withFailMessage("Address Country was also mapped.").isNull();
        assertThat(domain.getCountryId()).withFailMessage("Address Country ID was also mapped.").isNull();
        assertThat(domain.getPostalCode()).withFailMessage("Address Postal Code mismatch.").isEqualTo(entity.getPostalCode());
        assertThat(domain.getFullAddress()).withFailMessage("Full Address mismatch.").isEqualTo(entity.getFullAddress());
        assertThat(domain.getLatitude()).withFailMessage("Latitude mismatch.").isEqualTo(entity.getLatitude());
        assertThat(domain.getLongitude()).withFailMessage("Longitude mismatch.").isEqualTo(entity.getLongitude());
        assertThat(domain.isManualCoordinates()).withFailMessage("Manual Coordinates flag mismatch.")
                .isEqualTo(entity.getManualCoordinates());
        assertThat(domain.getCompany()).withFailMessage("Company mismatch.").isEqualTo(entity.getCompany());
        assertThat(domain.getDepartment()).withFailMessage("Department mismatch.").isEqualTo(entity.getDepartment());
    }

    @Test
    void mapEntityToDomain_addressEntityNull_shouldReturnNull() {
        assertThat(AddressMapper.mapEntityToDomain(null)).isNull();
    }
}
