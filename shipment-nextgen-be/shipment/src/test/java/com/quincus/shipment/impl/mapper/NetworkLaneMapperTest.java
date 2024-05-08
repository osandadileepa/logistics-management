package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetworkLaneMapperTest {

    @Mock
    private NetworkLaneSegmentMapper networkLaneSegmentMapper;
    @Mock
    private LocationHierarchyEntityAddressMapper lhToAddressMapper;
    @Mock
    private LocationHierarchyEntityFacilityMapper lhToFacilityMapper;
    @InjectMocks
    NetworkLaneMapper networkLaneMapper;

    @Test
    void testMapDomainToEntity_whenNull_returnNull() {
        assertThat(networkLaneMapper.mapDomainToEntity(null)).isNull();
    }

    @Test
    void testMapDomainToEntity_shouldMapNetworkLaneToEntity() {
        NetworkLane networkLane = new NetworkLane();
        networkLane.setOrganizationId("testOrgId");
        networkLane.setId("testId");
        networkLane.setServiceType(createServiceType());
        NetworkLaneEntity entity = networkLaneMapper.mapDomainToEntity(networkLane);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("testId");
        assertThat(entity.getOrganizationId()).isEqualTo("testOrgId");
        assertThat(entity.getServiceType().getName()).isEqualTo("testServiceName");
        assertThat(entity.getServiceType().getCode()).isEqualTo("testServiceCode");
        assertThat(entity.getServiceType().getOrganizationId()).isEqualTo("testOrgId");
        assertThat(entity.getServiceType().getId()).isEqualTo("testServiceId");
    }

    @Test
    void testMapCsvToDomain_whenNull_returnNull() {
        assertThat(networkLaneMapper.mapCsvToDomain(null)).isNull();
    }

    @Test
    void testMapCsvToDomain_shouldMapNetworkLaneCsvToDomain() {
        NetworkLaneCsv networkLaneCsv = new NetworkLaneCsv();
        networkLaneCsv.setOriginLocationTreeLevel1("Philippines");
        networkLaneCsv.setOriginLocationTreeLevel2("Metro Manila");
        networkLaneCsv.setOriginLocationTreeLevel3("Makati");
        networkLaneCsv.setOriginLocationTreeLevel4("JRS");

        networkLaneCsv.setDestinationLocationTreeLevel1("Philippines");
        networkLaneCsv.setDestinationLocationTreeLevel1("Metro Manila");
        networkLaneCsv.setDestinationLocationTreeLevel1("Quezon City");
        networkLaneCsv.setServiceType("testServiceType");
        NetworkLane domain = networkLaneMapper.mapCsvToDomain(networkLaneCsv);
        assertThat(domain).isNotNull();
        assertThat(domain.getServiceType()).isNotNull();
        // only destination and not the destination facility is populated as there is no facility value
        assertThat(domain.getDestination()).isNotNull();
        assertThat(domain.getDestinationFacility()).isNull();
        // origin facility is populated as there is a facility value and not origin
        assertThat(domain.getOriginFacility()).isNotNull();
        assertThat(domain.getOrigin()).isNull();
        assertThat(domain.getServiceType().getName()).isEqualTo("testServiceType");
        assertThat(domain.getServiceType().getCode()).isEqualTo("testServiceType");
    }

    @Test
    void testMapCsvToDomain_whenDestination5HasValue_ThenDestinationFacilityIsCreated() {
        NetworkLaneCsv networkLaneCsv = new NetworkLaneCsv();
        networkLaneCsv.setOriginLocationTreeLevel1("Philippines");
        networkLaneCsv.setOriginLocationTreeLevel2("Metro Manila");
        networkLaneCsv.setOriginLocationTreeLevel3("Makati");

        networkLaneCsv.setDestinationLocationTreeLevel1("Philippines");
        networkLaneCsv.setDestinationLocationTreeLevel2("Metro Manila");
        networkLaneCsv.setDestinationLocationTreeLevel3("Quezon City");
        networkLaneCsv.setDestinationLocationTreeLevel4("JRS");

        networkLaneCsv.setServiceType("testServiceType");
        NetworkLane domain = networkLaneMapper.mapCsvToDomain(networkLaneCsv);
        assertThat(domain).isNotNull();
        assertThat(domain.getServiceType()).isNotNull();

        // only origin and not the origin facility is populated as there is no facility value
        assertThat(domain.getOrigin()).isNotNull();
        assertThat(domain.getOriginFacility()).isNull();
        // destination facility  is populated as there is a facility value and not destination
        assertThat(domain.getDestinationFacility()).isNotNull();
        assertThat(domain.getDestination()).isNull();

        assertThat(domain.getServiceType().getName()).isEqualTo("testServiceType");
        assertThat(domain.getServiceType().getCode()).isEqualTo("testServiceType");
    }

    @Test
    void testMapEntityToDomain_whenNull_ShouldReturnNull() {
        assertThat(networkLaneMapper.mapEntityToDomain(null)).isNull();
    }

    @Test
    void testMapEntityToDomain_shouldMapNetworkLaneEntityToDomain() {
        NetworkLaneEntity entity = new NetworkLaneEntity();
        entity.addNetworkLaneSegment(new NetworkLaneSegmentEntity());
        entity.setId("testId");
        entity.setServiceType(createServiceTypeEntity());
        LocationHierarchyEntity origin = new LocationHierarchyEntity();
        entity.setOrigin(origin);
        LocationHierarchyEntity destination = new LocationHierarchyEntity();
        destination.setFacility(new LocationEntity());
        entity.setDestination(destination);
        NetworkLane domain = networkLaneMapper.mapEntityToDomain(entity);
        assertThat(domain).isNotNull();
        assertThat(domain.getServiceType()).isNotNull();
        assertThat(domain.getServiceType().getName()).isEqualTo("testServiceName");
        assertThat(domain.getServiceType().getCode()).isEqualTo("testServiceCode");
        verify(lhToFacilityMapper, times(1)).mapLocationHierarchyToFacility(destination);
        verify(lhToAddressMapper, times(1)).mapLocationHierarchyToAddress(origin);
        verify(networkLaneSegmentMapper, times(1)).mapEntitiesToDomain(anyList());
    }


    private ServiceType createServiceType() {
        ServiceType serviceType = new ServiceType();
        serviceType.setName("testServiceName");
        serviceType.setCode("testServiceCode");
        serviceType.setId("testServiceId");
        serviceType.setOrganizationId("testOrgId");
        return serviceType;
    }

    private ServiceTypeEntity createServiceTypeEntity() {
        ServiceTypeEntity serviceType = new ServiceTypeEntity();
        serviceType.setName("testServiceName");
        serviceType.setCode("testServiceCode");
        serviceType.setId("testServiceId");
        serviceType.setOrganizationId("testOrgId");
        return serviceType;
    }
}
