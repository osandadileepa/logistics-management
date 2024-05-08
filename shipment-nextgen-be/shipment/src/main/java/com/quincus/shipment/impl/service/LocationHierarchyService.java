package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qportal.model.QPortalChange;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.dto.CustomTreePageImpl;
import com.quincus.shipment.api.exception.LocationMessageException;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import com.quincus.shipment.impl.repository.LocationHierarchyRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity_;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.LocationHierarchyDuplicateValidator;
import com.quincus.shipment.impl.valueobject.LocationHierarchyTree;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.quincus.shipment.api.constant.LocationType.CITY;
import static com.quincus.shipment.api.constant.LocationType.COUNTRY;
import static com.quincus.shipment.api.constant.LocationType.FACILITY;
import static com.quincus.shipment.api.constant.LocationType.STATE;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.CITY_CODE;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.CODE;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.COUNTRY_CODE;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.EXTERNAL_ID;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.FACILITY_CODE;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.FACILITY_LOCATION_CODE;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.FACILITY_NAME;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.ID;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.NAME;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.PREFIX_CITY;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.PREFIX_COUNTRY;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.PREFIX_FACILITY;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.PREFIX_STATE;
import static com.quincus.shipment.impl.repository.constant.LocationHierarchyTupleAlias.STATE_CODE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationHierarchyService {

    private static final String INFO_CONVERTING_LOCATION_MESSAGE_TO_LOCATION_HIERARCHY = "Creating LocationHierarchy from Location message. UUID: {}, Payload: {}";
    private static final String ERR_FAILED_TO_CONVERT_MESSAGE = "Failed to convert location message. Error: %s";
    private static final String DELIMITER = ".";
    private static final String ERR_SAVE_LOCATION_HIERARCHY_FAILED = "Error saving location hierarchy with country `{}`, city `{}`, state `{}`, and externalId `{}`. Due to the error `{}`. Retrying to retrieve again.";
    private final LocationHierarchyRepository locationHierarchyRepository;
    private final LocationService locationService;
    private final UserDetailsProvider userDetailsProvider;
    private final LocationHierarchyDuplicateValidator locationHierarchyDuplicateValidator;
    private final ObjectMapper objectMapper;

    @Transactional
    public void setUpLocationHierarchies(final Address startAddress,
                                         final Facility startFacilityDomain,
                                         final Address endAddress,
                                         final Facility endFacilityDomain,
                                         final PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (isNull(packageJourneySegmentEntity)) {
            return;
        }
        setUpStartAndEndFacilities(startAddress, startFacilityDomain, endAddress, endFacilityDomain, packageJourneySegmentEntity);
    }

    private List<String> extractExternalIds(final Address startAddress,
                                            final Address endAddress,
                                            final Facility startFacilityDomain,
                                            final Facility endFacilityDomain) {
        String startFacilityId = nonNull(startFacilityDomain) ? extractId(startFacilityDomain.getExternalId()) : "";
        String endFacilityId = nonNull(endFacilityDomain) ? extractId(endFacilityDomain.getExternalId()) : "";
        List<String> startAddressIds = nonNull(startAddress) ? List.of(extractId(startAddress.getCountryId()), extractId(startAddress.getStateId()), extractId(startAddress.getCityId())) : Collections.emptyList();
        List<String> endAddressIds = nonNull(endAddress) ? List.of(extractId(endAddress.getCountryId()), extractId(endAddress.getStateId()), extractId(endAddress.getCityId())) : Collections.emptyList();
        return ListUtils.union(ListUtils.union(startAddressIds, endAddressIds), List.of(startFacilityId, endFacilityId));
    }

    private String extractId(String id) {
        return StringUtils.isEmpty(id) ? "" : id;
    }

    private LocationEntity createLocationCountry(final List<LocationEntity> locations,
                                                 final Address address) {
        return locationService.createLocation(locations, address.getCountryId(),
                COUNTRY, StringUtils.trim(address.getCountryName()), StringUtils.trim(address.getCountryName()));
    }

    private LocationEntity createLocationState(final List<LocationEntity> locations,
                                               final Address address) {
        return locationService.createLocation(locations, address.getStateId(),
                STATE, StringUtils.trim(address.getStateName()), StringUtils.trim(address.getStateName()));
    }

    private LocationEntity createLocationCity(final List<LocationEntity> locations,
                                              final Address address) {
        return locationService.createLocation(locations, address.getCityId(),
                CITY, StringUtils.trim(address.getCityName()), StringUtils.trim(address.getCityName()));
    }

    private LocationEntity createLocationFacility(final List<LocationEntity> locations,
                                                  final Facility facility) {
        return locationService.createLocation(locations, facility.getExternalId(), FACILITY, StringUtils.trim(facility.getName()), StringUtils.trim(facility.getLocationCode()));
    }

    private LocationHierarchyEntity createLocationHierarchy(final List<LocationEntity> locations,
                                                            final Address address,
                                                            final Facility facility) {
        if (address == null) return null;

        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        locationHierarchy.setCountry(createLocationCountry(locations, address));
        locationHierarchy.setState(createLocationState(locations, address));
        locationHierarchy.setCity(createLocationCity(locations, address));
        locationHierarchy.setCountryCode(address.getCountryName());
        locationHierarchy.setStateCode(address.getStateName());
        locationHierarchy.setCityCode(address.getCityName());
        locationHierarchy.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        locationHierarchy.setActive(true);
        locationHierarchy.setExternalId(address.getCityId());
        if (nonNull(facility)) {
            locationHierarchy.setFacility(createLocationFacility(locations, facility));
            locationHierarchy.setFacilityName(facility.getName());
            locationHierarchy.setFacilityCode(facility.getCode());
            locationHierarchy.setFacilityLocationCode(facility.getLocationCode());
            locationHierarchy.setExternalId(facility.getExternalId());
        }
        return locationHierarchy;
    }

    @Transactional
    public LocationHierarchyEntity setUpLocationHierarchy(final Address startAddress, final Facility facility) {
        List<String> externalIds = extractExternalIds(startAddress, null, facility, null);
        List<LocationEntity> locations = locationService.findByOrganizationIdAndExternalIds(externalIds);
        return createLocationHierarchy(locations, startAddress, facility);
    }

    @Transactional
    public LocationHierarchyEntity setUpLocationHierarchyFromFacilityAndSave(final Facility facility) {
        try {
            LocationHierarchyEntity locationHierarchy = setUpLocationHierarchy(facility.getLocation(), facility);
            return locationHierarchyRepository.saveAndFlush(locationHierarchy);
        } catch (DataIntegrityViolationException e) {
            log.warn(ERR_SAVE_LOCATION_HIERARCHY_FAILED, facility.getLocation().getCountryId(), facility.getLocation().getCityId(), facility.getLocation().getStateId(), facility.getExternalId(), e.getMessage());
            return findByLocationExternalIdWithFacility(facility.getLocation().getCountryId(), facility.getLocation().getCityId(), facility.getLocation().getStateId(), facility.getExternalId());
        }
    }

    private void setUpStartAndEndFacilities(final Address startAddress,
                                            final Facility startFacilityDomain,
                                            final Address endAddress,
                                            final Facility endFacilityDomain,
                                            final PackageJourneySegmentEntity packageJourneySegmentEntity) {
        List<String> externalIds = extractExternalIds(startAddress, endAddress, startFacilityDomain, endFacilityDomain);
        final List<LocationEntity> locations = new ArrayList<>(locationService.findByOrganizationIdAndExternalIds(externalIds));
        LocationHierarchyEntity pStartLocationHierarchy = findOrCreateLocationHierarchy(startAddress,
                startFacilityDomain, createLocationHierarchy(locations, startAddress, startFacilityDomain));
        packageJourneySegmentEntity.setStartLocationHierarchy(pStartLocationHierarchy);
        if (nonNull(pStartLocationHierarchy)) {
            locationHierarchyDuplicateValidator.validateLocationHierarchy(pStartLocationHierarchy);
            addLocationToLocationList(locations, pStartLocationHierarchy.getCountry(), pStartLocationHierarchy.getState(), pStartLocationHierarchy.getCity(), pStartLocationHierarchy.getFacility());
        }
        LocationHierarchyEntity pEndLocationHierarchy = findOrCreateLocationHierarchy(endAddress, endFacilityDomain, createLocationHierarchy(locations, endAddress, endFacilityDomain));
        locationHierarchyDuplicateValidator.validateLocationHierarchy(pEndLocationHierarchy);
        packageJourneySegmentEntity.setEndLocationHierarchy(pEndLocationHierarchy);
    }

    private void addLocationToLocationList(List<LocationEntity> locations, LocationEntity... locationEntities) {
        if (isNull(locations)) {
            return;
        }
        Collections.addAll(locations, locationEntities);
    }

    private LocationHierarchyEntity findOrCreateLocationHierarchy(final Address address,
                                                                  final Facility facility,
                                                                  final LocationHierarchyEntity paramlocationHierarchy) {
        if (paramlocationHierarchy == null) return null;
        String facilityId = getLocationEntityId(paramlocationHierarchy.getFacility());
        LocationHierarchyEntity locationHierarchy = findLocationHierarchyEntity(paramlocationHierarchy.getCountry(), paramlocationHierarchy.getCity(), paramlocationHierarchy.getState(), facilityId);
        if (locationHierarchy != null) return locationHierarchy;

        try {
            return locationHierarchyRepository.save(createLocationHierarchyEntity(address, facility, paramlocationHierarchy.getCountry(), paramlocationHierarchy.getCity(), paramlocationHierarchy.getState(), paramlocationHierarchy.getFacility()));
        } catch (ConstraintViolationException e) {
            log.warn(ERR_SAVE_LOCATION_HIERARCHY_FAILED, paramlocationHierarchy.getCountry(), paramlocationHierarchy.getCity(), paramlocationHierarchy.getState(), facility.getExternalId(), e.getMessage());
            return findLocationHierarchyEntity(paramlocationHierarchy.getCountry(), paramlocationHierarchy.getCity(), paramlocationHierarchy.getState(), facilityId);
        }
    }

    public LocationHierarchyEntity findLocationHierarchyEntity(
            final LocationEntity country,
            final LocationEntity city,
            final LocationEntity state,
            final String facilityId) {
        final String organizationId = userDetailsProvider.getCurrentOrganizationId();
        final String countryId = getLocationEntityId(country);
        final String stateId = getLocationEntityId(state);
        final String cityId = getLocationEntityId(city);
        log.debug("find LocationHierarchy w/ organizationId={}, countryId={}, stateId={}, cityId={}, facilityId={}",
                organizationId, countryId, stateId, cityId, facilityId);
        if (nonNull(facilityId)) {
            return locationHierarchyRepository.findByCountryAndStateAndCityAndFacilityAndOrganizationId(countryId, stateId, cityId, facilityId, organizationId)
                    .orElse(null);
        }
        return locationHierarchyRepository.findLHWithNullFacility(countryId, stateId, cityId, organizationId)
                .orElse(null);
    }

    private String getLocationEntityId(final LocationEntity locationEntity) {
        return nonNull(locationEntity) ? locationEntity.getId() : null;
    }

    public Page<LocationHierarchyTree> findPageableLocationHierarchiesByFilter(LocationHierarchyFilter filter) {
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        Pageable pageable = buildPageable(filter);
        Page<Tuple> tuplePage = switch (LocationType.fromValue(filter.getLevel())) {
            case COUNTRY ->
                    locationHierarchyRepository.findCountries(filter.getCountryId(), filter.getKey(), organizationId, pageable);
            case STATE ->
                    locationHierarchyRepository.findStates(filter.getCountryId(), filter.getStateId(), filter.getKey(), organizationId, pageable);
            case CITY ->
                    locationHierarchyRepository.findCities(filter.getCountryId(), filter.getStateId(), filter.getCityId(), filter.getKey(), organizationId, pageable);
            default ->
                    locationHierarchyRepository.findFacilities(filter.getCountryId(), filter.getStateId(), filter.getCityId(), filter.getFacilityId(), filter.getKey(), organizationId, pageable);
        };
        return new CustomTreePageImpl<>(LocationHierarchyTree.parseTreeList(tuplePage.getContent(), filter.getLevel()), pageable, tuplePage.getTotalElements());
    }

    public Page<LocationEntity> findAllStatesByCountry(final LocationHierarchyFilter filter) {
        return locationHierarchyRepository.findStates(filter.getCountryId(), userDetailsProvider.getCurrentOrganizationId(), buildPageable(filter));
    }

    public Page<LocationEntity> findAllCitiesByState(final LocationHierarchyFilter filter) {
        return locationHierarchyRepository.findCities(filter.getCountryId(), filter.getStateId(), userDetailsProvider.getCurrentOrganizationId(), buildPageable(filter));
    }

    public Page<LocationEntity> findAllFacilitiesByCity(final LocationHierarchyFilter filter) {
        return locationHierarchyRepository.findFacilities(filter.getCountryId(), filter.getStateId(), filter.getCityId(), userDetailsProvider.getCurrentOrganizationId(), buildPageable(filter));
    }

    private LocationHierarchyEntity createLocationHierarchyEntity(final Address address,
                                                                  final Facility domainFacility,
                                                                  final LocationEntity country,
                                                                  final LocationEntity city,
                                                                  final LocationEntity state,
                                                                  final LocationEntity facility) {
        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        if (nonNull(facility)) {
            locationHierarchy.setFacility(facility);
            locationHierarchy.setFacilityName(facility.getName());
            locationHierarchy.setFacilityCode(domainFacility.getCode());
            locationHierarchy.setFacilityLocationCode(facility.getCode());
        }
        locationHierarchy.setCountry(country);
        locationHierarchy.setState(state);
        locationHierarchy.setCity(city);
        locationHierarchy.setCountryCode(address.getCountryName());
        locationHierarchy.setStateCode(address.getStateName());
        locationHierarchy.setCityCode(address.getCityName());
        locationHierarchy.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        locationHierarchy.setActive(true);
        locationHierarchy.setExternalId(domainFacility.getExternalId());
        return locationHierarchy;
    }

    private Pageable buildPageable(final LocationHierarchyFilter filter) {
        if (isNull(filter.getLocationType())) {
            return PageRequest.of((filter.getPage() - 1), filter.getPerPage());
        }
        return PageRequest.of((filter.getPage() - 1), filter.getPerPage(), Sort.by(Sort.Direction.ASC, getSortBy(filter)));
    }

    private String getSortBy(final LocationHierarchyFilter filter) {
        String sortBy = getOrDefaultFieldName(filter.getSortBy());
        return switch (filter.getLocationType()) {
            case COUNTRY -> StringUtils.join(LocationHierarchyEntity_.COUNTRY, DELIMITER, sortBy);
            case STATE -> StringUtils.join(LocationHierarchyEntity_.STATE, DELIMITER, sortBy);
            case CITY -> StringUtils.join(LocationHierarchyEntity_.CITY, DELIMITER, sortBy);
            case FACILITY -> StringUtils.join(LocationHierarchyEntity_.FACILITY, DELIMITER, sortBy);
        };
    }

    private String getOrDefaultFieldName(final String fieldName) {
        if (!StringUtils.equalsAny(fieldName, LocationEntity_.NAME, LocationEntity_.CODE)) {
            return LocationEntity_.NAME;
        }
        return fieldName;
    }

    public List<LocationHierarchyEntity> findAllByIds(Set<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return mapTupleToLocationHierarchy(locationHierarchyRepository.findAllByIdsIn(ids));
    }

    private List<LocationHierarchyEntity> mapTupleToLocationHierarchy(List<Tuple> tuples) {
        return tuples.stream().map(tuple -> {
            LocationHierarchyEntity entity = new LocationHierarchyEntity();
            entity.setId(tuple.get(BaseEntity_.ID, String.class));
            entity.setCountryCode(tuple.get(COUNTRY_CODE, String.class));
            entity.setStateCode(tuple.get(STATE_CODE, String.class));
            entity.setCityCode(tuple.get(CITY_CODE, String.class));
            entity.setFacilityCode(tuple.get(FACILITY_CODE, String.class));
            entity.setFacilityLocationCode(tuple.get(FACILITY_LOCATION_CODE, String.class));
            entity.setFacilityName(tuple.get(FACILITY_NAME, String.class));

            entity.setCountry(mapTupleToLocationEntity(tuple, PREFIX_COUNTRY));
            entity.setState(mapTupleToLocationEntity(tuple, PREFIX_STATE));
            entity.setCity(mapTupleToLocationEntity(tuple, PREFIX_CITY));
            entity.setFacility(mapTupleToLocationEntity(tuple, PREFIX_FACILITY));
            return entity;
        }).toList();
    }

    private LocationEntity mapTupleToLocationEntity(Tuple tuple, String locationPrefixAlias) {
        String id = tuple.get(locationPrefixAlias.concat(ID), String.class);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(tuple.get(locationPrefixAlias.concat(NAME), String.class));
        locationEntity.setId(id);
        locationEntity.setExternalId(tuple.get(locationPrefixAlias.concat(EXTERNAL_ID), String.class));
        locationEntity.setCode(tuple.get(locationPrefixAlias.concat(CODE), String.class));
        return locationEntity;
    }

    public LocationHierarchyEntity findByLocationExternalIdWithNoFacility(String extCountryId, String extCityId, String extStateId) {
        return locationHierarchyRepository.findByLocationExternalId(extCountryId, extCityId, extStateId, null, userDetailsProvider.getCurrentOrganizationId());
    }

    public LocationHierarchyEntity findByLocationExternalIdWithFacility(String extCountryId, String extCityId, String extStateId, String extFacilityId) {
        return locationHierarchyRepository.findByLocationExternalId(extCountryId, extCityId, extStateId, extFacilityId, userDetailsProvider.getCurrentOrganizationId());
    }

    public List<LocationHierarchyEntity> findLocationHierarchyByFacilityExternalIds(List<String> facilityExternalIds) {
        return locationHierarchyRepository.findByFacilityExternalIds(facilityExternalIds, userDetailsProvider.getCurrentOrganizationId());
    }

    @Transactional
    public void receiveLocationMessage(
            final String payload,
            final String transactionId) {
        if (StringUtils.isEmpty(payload)) return;
        log.info(INFO_CONVERTING_LOCATION_MESSAGE_TO_LOCATION_HIERARCHY, transactionId, payload);

        try {
            final QPortalChange qPortalChange = objectMapper.readValue(payload, QPortalChange.class);
            if (qPortalChange == null || qPortalChange.getChanges() == null) return;

            final QPortalLocation qPortalLocation = qPortalChange.getChanges();
            final LocationType locationType = locationService.getLocationType(qPortalLocation);
            if (locationType == null) return;

            Optional<LocationEntity> locationOpt = locationService.createOrUpdateLocation(qPortalLocation);
            if (FACILITY == locationType && locationOpt.isPresent()) {
                createOrUpdateLocationHierarchyForFacility(qPortalLocation, locationOpt.get());
            }
        } catch (JsonProcessingException e) {
            throw new LocationMessageException(String.format(ERR_FAILED_TO_CONVERT_MESSAGE, payload), transactionId);
        }
    }

    private void createOrUpdateLocationHierarchyForFacility(
            final QPortalLocation facilityQPortalLocation,
            final LocationEntity facilityLocation) {

        final List<LocationEntity> ancestorLocations = locationService.findByOrganizationIdAndExternalIds(List.of(
                facilityQPortalLocation.getCountryId(),
                facilityQPortalLocation.getStateProvinceId(),
                facilityQPortalLocation.getCityId()));

        if (ancestorLocations == null) {
            log.warn("Cannot create LocationHierarchy w/o ancestor locations.");
            return;
        }

        final LocationEntity countryLocation = findLocationByType(ancestorLocations, LocationType.COUNTRY);
        final LocationEntity stateLocation = findLocationByType(ancestorLocations, LocationType.STATE);
        final LocationEntity cityLocation = findLocationByType(ancestorLocations, LocationType.CITY);
        if (countryLocation == null || stateLocation == null || cityLocation == null) {
            log.warn("Cannot create Facility's LocationHierarchy w/o country={}, state={} & city={}.",
                    facilityQPortalLocation.getCountryId(),
                    facilityQPortalLocation.getStateProvinceId(),
                    facilityQPortalLocation.getCityId());
            return;
        }

        LocationHierarchyEntity facilityLocationHierarchy = findLocationHierarchyEntity(countryLocation, cityLocation, stateLocation, facilityLocation.getId());
        if (facilityLocationHierarchy == null) {
            facilityLocationHierarchy = new LocationHierarchyEntity();
        }

        facilityLocationHierarchy.setFacility(facilityLocation);
        facilityLocationHierarchy.setFacilityName(facilityQPortalLocation.getName());
        facilityLocationHierarchy.setFacilityCode(facilityQPortalLocation.getCode());
        facilityLocationHierarchy.setFacilityLocationCode(facilityQPortalLocation.getLocationCode());
        facilityLocationHierarchy.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        facilityLocationHierarchy.setCountry(countryLocation);
        facilityLocationHierarchy.setCountryCode(countryLocation.getCode());
        facilityLocationHierarchy.setState(stateLocation);
        facilityLocationHierarchy.setStateCode(stateLocation.getCode());
        facilityLocationHierarchy.setCity(cityLocation);
        facilityLocationHierarchy.setCityCode(cityLocation.getCode());

        locationHierarchyRepository.save(facilityLocationHierarchy);
    }

    private LocationEntity findLocationByType(
            final List<LocationEntity> locations,
            final LocationType type) {
        return locations.stream()
                .filter(location -> location.getType() == type)
                .findFirst()
                .orElse(null);
    }
}
