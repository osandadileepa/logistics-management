package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class NetworkLaneMapper {

    private final NetworkLaneSegmentMapper networkLaneSegmentMapper;
    private final LocationHierarchyEntityAddressMapper lhToAddressMapper;
    private final LocationHierarchyEntityFacilityMapper lhToFacilityMapper;

    public NetworkLaneEntity mapDomainToEntity(NetworkLane networkLane) {
        if (networkLane == null) {
            return null;
        }
        NetworkLaneEntity networkLaneEntity = new NetworkLaneEntity();
        networkLaneEntity.setId(networkLane.getId());
        networkLaneEntity.setOrganizationId(networkLane.getOrganizationId());
        networkLaneEntity.setServiceType(ServiceTypeMapper.mapDomainToEntity(networkLane.getServiceType(), networkLane.getOrganizationId()));
        return networkLaneEntity;
    }

    public NetworkLane mapEntityToDomain(NetworkLaneEntity entity) {
        if (entity == null) {
            return null;
        }
        NetworkLane domain = new NetworkLane();
        domain.setId(entity.getId());
        domain.setServiceType(ServiceTypeMapper.mapEntityToDomain(entity.getServiceType()));
        if (Optional.ofNullable(entity.getOrigin()).map(LocationHierarchyEntity::getFacility).isPresent()) {
            domain.setOriginFacility(lhToFacilityMapper.mapLocationHierarchyToFacility(entity.getOrigin()));
        } else {
            domain.setOrigin(lhToAddressMapper.mapLocationHierarchyToAddress(entity.getOrigin()));
        }
        if (Optional.ofNullable(entity.getDestination()).map(LocationHierarchyEntity::getFacility).isPresent()) {
            domain.setDestinationFacility(lhToFacilityMapper.mapLocationHierarchyToFacility(entity.getDestination()));
        } else {
            domain.setDestination(lhToAddressMapper.mapLocationHierarchyToAddress(entity.getDestination()));
        }
        domain.setNetworkLaneSegments(networkLaneSegmentMapper.mapEntitiesToDomain(entity.getNetworkLaneSegmentList()));
        return domain;
    }

    public List<NetworkLane> mapEntitiesToDomain(List<NetworkLaneEntity> entities) {
        return entities.stream().map(this::mapEntityToDomain).toList();
    }

    public NetworkLane mapCsvToDomain(NetworkLaneCsv networkLaneCsv) {
        if (networkLaneCsv == null) {
            return null;
        }
        NetworkLane domain = new NetworkLane();
        domain.setServiceType(generateServiceType(networkLaneCsv.getServiceType(), networkLaneCsv.getOrganizationId()));
        domain.setOrigin(createOrigin(networkLaneCsv));
        domain.setDestination(createDestination(networkLaneCsv));
        domain.setOriginFacility(createOriginFacility(networkLaneCsv));
        domain.setDestinationFacility(createDestinationFacility(networkLaneCsv));
        domain.setOrganizationId(networkLaneCsv.getOrganizationId());
        return domain;
    }

    private ServiceType generateServiceType(String serviceTypeCode, String organizationId) {
        if (StringUtils.isBlank(serviceTypeCode)) {
            return null;
        }
        ServiceType serviceType = new ServiceType();
        serviceType.setCode(serviceTypeCode);
        serviceType.setName(serviceTypeCode);
        serviceType.setOrganizationId(organizationId);
        return serviceType;
    }

    //If facility is provided, address should be null and if facility is not provided, address should be provided
    private Address createOrigin(NetworkLaneCsv networkLaneCsv) {
        if (StringUtils.isNotBlank(networkLaneCsv.getOriginLocationTreeLevel4())) {
            return null;
        }
        return createAddress(networkLaneCsv.getOriginLocationTreeLevel1()
                , networkLaneCsv.getOriginLocationTreeLevel2()
                , networkLaneCsv.getOriginLocationTreeLevel3());
    }

    //If facility is provided, address should be null and if facility is not provided, address should be provided
    private Address createDestination(NetworkLaneCsv networkLaneCsv) {
        if (StringUtils.isNotBlank(networkLaneCsv.getDestinationLocationTreeLevel4())) {
            return null;
        }
        return createAddress(networkLaneCsv.getDestinationLocationTreeLevel1()
                , networkLaneCsv.getDestinationLocationTreeLevel2()
                , networkLaneCsv.getDestinationLocationTreeLevel3());
    }

    private Address createAddress(String country, String state, String city) {
        Address address = new Address();
        address.setCountryName(country);
        address.setCityName(city);
        address.setStateName(state);
        return address;
    }

    private Facility createOriginFacility(NetworkLaneCsv networkLaneCsv) {
        if (StringUtils.isBlank(networkLaneCsv.getOriginLocationTreeLevel4())) {
            return null;
        }
        return createFacility(networkLaneCsv.getOriginLocationTreeLevel4()
                , networkLaneCsv.getOriginLocationTreeLevel1()
                , networkLaneCsv.getOriginLocationTreeLevel2()
                , networkLaneCsv.getOriginLocationTreeLevel3());
    }

    private Facility createDestinationFacility(NetworkLaneCsv networkLaneCsv) {
        if (StringUtils.isBlank(networkLaneCsv.getDestinationLocationTreeLevel4())) {
            return null;
        }
        return createFacility(networkLaneCsv.getDestinationLocationTreeLevel4()
                , networkLaneCsv.getDestinationLocationTreeLevel1()
                , networkLaneCsv.getDestinationLocationTreeLevel2()
                , networkLaneCsv.getDestinationLocationTreeLevel3());
    }

    private Facility createFacility(String facilityName, String countryName
            , String stateName, String cityName) {
        Facility facility = new Facility();
        facility.setName(facilityName);
        facility.setLocation(createAddress(countryName, stateName, cityName));
        return facility;
    }
}
