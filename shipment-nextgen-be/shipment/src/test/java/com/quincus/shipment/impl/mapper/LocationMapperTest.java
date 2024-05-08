package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Location;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LocationMapperTest {

    @Test
    void mapDomainToEntity_locationDomainTypeCountry_shouldReturnLocationEntity() {
        Location domain = new Location();
        domain.setId("LOCATION1");
        domain.setCity("City");
        domain.setState("State");
        domain.setCountry("Country");
        domain.setOrganizationId("ORG1");
        domain.setTimezone("Asia/Manila UTC+08:00");

        final LocationEntity entity = LocationMapper.mapDomainToEntity(domain);

        assertThat(entity.getId()).withFailMessage("Autogenerated field `id` was mapped.").isNull();
        assertThat(entity)
                .extracting(LocationEntity::getExternalId, LocationEntity::getType, LocationEntity::getCode, LocationEntity::getTimezone)
                .containsExactly(domain.getId(), LocationType.COUNTRY, domain.getCountry(), domain.getTimezone());
        assertThat(entity.getOrganizationId()).withFailMessage("Organization was also mapped.").isNull();
    }

    @Test
    void mapDomainToEntity_locationDomainTypeState_shouldReturnLocationEntity() {
        Location domain = new Location();
        domain.setId("LOCATION1");
        domain.setCity("City");
        domain.setState("State");
        domain.setOrganizationId("ORG1");
        domain.setTimezone("Asia/Manila UTC+08:00");

        final LocationEntity entity = LocationMapper.mapDomainToEntity(domain);

        assertThat(entity.getId()).withFailMessage("Autogenerated field `id` was mapped.").isNull();
        assertThat(entity)
                .extracting(LocationEntity::getExternalId, LocationEntity::getType, LocationEntity::getCode, LocationEntity::getTimezone)
                .containsExactly(domain.getId(), LocationType.STATE, domain.getState(), domain.getTimezone());
        assertThat(entity.getOrganizationId()).withFailMessage("Organization was also mapped.").isNull();
    }

    @Test
    void mapDomainToEntity_locationDomainTypeCity_shouldReturnLocationEntity() {
        Location domain = new Location();
        domain.setId("LOCATION1");
        domain.setCity("City");
        domain.setOrganizationId("ORG1");
        domain.setTimezone("Asia/Manila UTC+08:00");

        final LocationEntity entity = LocationMapper.mapDomainToEntity(domain);
        assertThat(entity.getId()).withFailMessage("Autogenerated field `id` was mapped.").isNull();
        assertThat(entity)
                .extracting(LocationEntity::getExternalId, LocationEntity::getType, LocationEntity::getCode, LocationEntity::getTimezone)
                .containsExactly(domain.getId(), LocationType.CITY, domain.getCity(), domain.getTimezone());
        assertThat(entity.getOrganizationId()).withFailMessage("Organization was also mapped.").isNull();
    }

    @Test
    void mapDomainToEntity_locationDomainTypeNull_shouldReturnLocationEntity() {
        Location domain = new Location();
        domain.setId("LOCATION1");
        domain.setOrganizationId("ORG1");
        domain.setTimezone("Asia/Manila UTC+08:00");

        final LocationEntity entity = LocationMapper.mapDomainToEntity(domain);

        assertThat(entity.getId()).withFailMessage("Autogenerated field `id` was mapped.").isNull();
        assertThat(entity.getExternalId()).isEqualTo(domain.getId());
        assertThat(entity.getType()).isNull();
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getTimezone()).isEqualTo(domain.getTimezone());
        assertThat(entity.getOrganizationId()).withFailMessage("Organization was also mapped.").isNull();
    }

    @Test
    void mapDomainToEntity_locationDomainNull_shouldReturnNull() {
        assertThat(LocationMapper.mapDomainToEntity(null)).isNull();
    }

    @Test
    void mapEntityToDomain_locationEntityTypeCountry_shouldReturnLocationDomain() {
        var entity = new LocationEntity();
        entity.setExternalId("location-1");
        entity.setType(LocationType.COUNTRY);
        entity.setCode("country");
        entity.setDescription("Country Description");
        entity.setTimezone("Asia/Manila UTC+08:00");
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId("org1");
        entity.setOrganizationId(organization.getId());

        final Location domain = LocationMapper.mapEntityToDomain(entity);

        assertThat(entity.getExternalId()).isEqualTo(domain.getId());
        assertThat(entity.getCode()).isEqualTo(domain.getCountry());
        assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
        assertThat(entity.getTimezone()).isEqualTo(domain.getTimezone());
        assertThat(domain.getState()).isNull();
        assertThat(domain.getCity()).isNull();
    }

    @Test
    void mapEntityToDomain_locationEntityTypeState_shouldReturnLocationDomain() {
        var entity = new LocationEntity();
        entity.setExternalId("location-1");
        entity.setType(LocationType.STATE);
        entity.setCode("state");
        entity.setDescription("State Description");
        entity.setTimezone("Asia/Manila UTC+08:00");
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId("org1");
        entity.setOrganizationId(organization.getId());

        final Location domain = LocationMapper.mapEntityToDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getExternalId());
        assertThat(domain.getState()).isEqualTo(entity.getCode());
        assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
        assertThat(entity.getTimezone()).isEqualTo(domain.getTimezone());
        assertThat(domain.getCountry()).isNull();
        assertThat(domain.getCity()).isNull();
    }

    @Test
    void mapEntityToDomain_locationEntityTypeCity_shouldReturnLocationDomain() {
        var entity = new LocationEntity();
        entity.setExternalId("location-1");
        entity.setType(LocationType.CITY);
        entity.setCode("city");
        entity.setDescription("City Description");
        entity.setTimezone("Asia/Manila UTC+08:00");
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId("org1");
        entity.setOrganizationId(organization.getId());

        final Location domain = LocationMapper.mapEntityToDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getExternalId());
        assertThat(domain.getCity()).isEqualTo(entity.getCode());
        assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
        assertThat(entity.getTimezone()).isEqualTo(domain.getTimezone());
        assertThat(domain.getCountry()).isNull();
        assertThat(domain.getState()).isNull();
    }

    @Test
    void mapEntityToDomain_locationEntityTypeNull_shouldReturnLocationDomain() {
        LocationEntity entity = new LocationEntity();
        entity.setExternalId("location-1");
        entity.setCode("xxx");
        entity.setDescription("Description");
        entity.setTimezone("Asia/Manila UTC+08:00");
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId("org1");
        entity.setOrganizationId(organization.getId());

        final Location domain = LocationMapper.mapEntityToDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getExternalId());
        assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
        assertThat(entity.getTimezone()).isEqualTo(domain.getTimezone());
        assertThat(domain.getCountry()).isNull();
        assertThat(domain.getState()).isNull();
        assertThat(domain.getCity()).isNull();
    }

    @Test
    void mapEntityToDomain_locationEntityOrganizationNull_shouldReturnLocationDomain() {
        var entity = new LocationEntity();
        entity.setExternalId("location-1");
        entity.setType(LocationType.CITY);
        entity.setCode("city");
        entity.setDescription("City Description");
        entity.setTimezone("Asia/Manila UTC+08:00");

        final Location domain = LocationMapper.mapEntityToDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getExternalId());
        assertThat(domain.getOrganizationId()).isEqualTo(entity.getOrganizationId());
        assertThat(domain.getCity()).isEqualTo(entity.getCode());
        assertThat(domain.getTimezone()).isEqualTo(entity.getTimezone());
        assertThat(domain.getCountry()).isNull();
        assertThat(domain.getState()).isNull();
    }

    @Test
    void mapEntityToDomain_locationEntityNull_shouldReturnNull() {
        assertThat(LocationMapper.mapEntityToDomain(null)).isNull();
    }
}
