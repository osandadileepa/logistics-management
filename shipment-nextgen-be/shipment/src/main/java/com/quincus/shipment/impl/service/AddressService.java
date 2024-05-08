package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.mapper.AddressMapper;
import com.quincus.shipment.impl.repository.AddressRepository;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AddressService {
    private final AddressRepository addressRepository;
    private final LocationHierarchyService locationHierarchyService;

    @Transactional
    public AddressEntity createAddressEntityForOrganization(final Address addressDomain) {
        if (isNull(addressDomain)) return null;
        AddressEntity addressEntity = AddressMapper.mapDomainToEntity(addressDomain);
        addressEntity.setLocationHierarchy(locationHierarchyService.setUpLocationHierarchy(addressDomain, null));
        return addressRepository.save(addressEntity);
    }

    @Transactional
    public AddressEntity createAddressEntityWithFacility(final Address addressDomain,
                                                         final String facilityName) {
        if (isNull(addressDomain) || (StringUtils.isBlank(addressDomain.getCountryId())
                && StringUtils.isBlank(addressDomain.getStateId()) && StringUtils.isBlank(addressDomain.getCity()))) {
            return null;
        }
        Facility facility = new Facility();
        facility.setId(addressDomain.getId());
        facility.setName(facilityName);
        facility.setLocation(addressDomain);
        LocationHierarchyEntity locationHierarchy = locationHierarchyService.setUpLocationHierarchy(addressDomain, facility);
        return saveAddress(addressDomain, locationHierarchy);
    }

    @Transactional
    public AddressEntity saveAddress(Address addressDomain, LocationHierarchyEntity locationHierarchy) {
        if (addressDomain == null
                || locationHierarchy == null
                || StringUtils.isBlank(locationHierarchy.getId())) {
            return null;
        }
        AddressEntity addressEntity = addressRepository.findByLocationHierarchyId(locationHierarchy.getId());
        if (addressEntity != null) {
            addressEntity.setFullAddress(addressDomain.getFullAddress());
            addressEntity.setLine1(addressDomain.getLine1());
            addressEntity.setLine2(addressDomain.getLine2());
            addressEntity.setLine3(addressDomain.getLine3());
            return addressRepository.saveAndFlush(addressEntity);
        }
        addressEntity = AddressMapper.mapDomainToEntity(addressDomain);
        addressEntity.setLocationHierarchy(locationHierarchy);
        return addressRepository.saveAndFlush(addressEntity);
    }

    public void setFacilityAddress(List<PackageJourneySegment> packageJourneySegments) {
        if (CollectionUtils.isEmpty(packageJourneySegments)) return;
        List<String> ids = getAllLocationHierarchyIds(packageJourneySegments);
        Map<String, AddressEntity> map = getAddressEntitiesByLocationHierarchyIds(ids);
        packageJourneySegments.forEach(segment -> {
                    setFacilityAddress(segment.getStartFacility(), map);
                    setFacilityAddress(segment.getEndFacility(), map);
                }
        );
    }

    public List<AddressEntity> getAddressByIds(List<String> ids) {
        List<Object[]> obj = addressRepository.findByIds(ids);
        if (CollectionUtils.isEmpty(obj)) return Collections.emptyList();
        List<AddressEntity> entities = new ArrayList<>();
        obj.forEach(o -> entities.add(convertObjectArrayToAddressEntity(o)));
        return entities;
    }

    private Map<String, AddressEntity> getAddressEntitiesByLocationHierarchyIds(List<String> locationHierarchyIds) {
        if (CollectionUtils.isEmpty(locationHierarchyIds)) return Collections.emptyMap();
        Map<String, AddressEntity> map = new HashMap<>();
        List<AddressEntity> addressEntities = addressRepository.findByLocationHierarchyIds(locationHierarchyIds);
        locationHierarchyIds.forEach(id -> map.put(id, getFacilityAddress(addressEntities, id)));
        return map;
    }

    private AddressEntity getFacilityAddress(List<AddressEntity> addressEntities, String locationHierarchyId) {
        if (CollectionUtils.isEmpty(addressEntities) || StringUtils.isBlank(locationHierarchyId)) return null;
        return addressEntities.stream().filter(a -> StringUtils.equals(a.getLocationHierarchyId(), locationHierarchyId)).findFirst().orElse(null);
    }

    private List<String> getAllLocationHierarchyIds(List<PackageJourneySegment> packageJourneySegments) {
        List<String> ids = new ArrayList<>();
        packageJourneySegments.forEach(segment -> {
            if (segment.getStartFacility() != null && segment.getStartFacility().getLocation() != null
                    && StringUtils.isNotBlank(segment.getStartFacility().getLocation().getLocationHierarchyId())) {
                ids.add(segment.getStartFacility().getLocation().getLocationHierarchyId());
            }
            if (segment.getEndFacility() != null && segment.getEndFacility().getLocation() != null
                    && StringUtils.isNotBlank(segment.getEndFacility().getLocation().getLocationHierarchyId())) {
                ids.add(segment.getEndFacility().getLocation().getLocationHierarchyId());
            }
        });
        return ids;
    }

    private void setFacilityAddress(Facility facility, Map<String, AddressEntity> map) {
        if (facility == null) return;
        Address address = facility.getLocation();
        if (address == null) return;
        AddressEntity addressEntity = map.get(address.getLocationHierarchyId());
        if (addressEntity == null) return;
        address.setLine1(addressEntity.getLine1());
        address.setLine2(addressEntity.getLine2());
        address.setLine3(addressEntity.getLine3());
    }

    private AddressEntity convertObjectArrayToAddressEntity(Object[] objArray) {
        if (ArrayUtils.isEmpty(objArray)) return null;
        AddressEntity address = new AddressEntity();
        address.setId((String) objArray[0]);
        address.setLine1((String) objArray[1]);
        address.setLine2((String) objArray[2]);
        address.setLine3((String) objArray[3]);
        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        locationHierarchy.setId((String) objArray[4]);
        locationHierarchy.setCountryCode((String) objArray[5]);
        locationHierarchy.setStateCode((String) objArray[6]);
        locationHierarchy.setCityCode((String) objArray[7]);
        locationHierarchy.setCountry(createLocation(locationHierarchy.getCountryCode()));
        locationHierarchy.setState(createLocation(locationHierarchy.getStateCode()));
        locationHierarchy.setCity(createLocation(locationHierarchy.getCityCode()));
        address.setLocationHierarchy(locationHierarchy);
        return address;
    }

    private LocationEntity createLocation(String name) {
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(name);
        return locationEntity;
    }
}