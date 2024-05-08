package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qportal.model.QPortalChange;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.exception.LocationMessageException;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import com.quincus.shipment.impl.repository.LocationHierarchyRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.LocationHierarchyDuplicateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationHierarchyServiceTest {
    OrganizationEntity organization = new OrganizationEntity();
    LocationEntity countryLocation = new LocationEntity();
    LocationEntity stateLocation = new LocationEntity();
    LocationEntity cityLocation = new LocationEntity();
    LocationEntity facilityLocation = new LocationEntity();
    Facility facilityDomain = new Facility();
    Address address = new Address();
    String country = "US";
    String state = "MA";
    String city = "Boston";
    String facility = "facility";
    String organizationId = "ORG1";
    String countryId = "COUNTRY-US-01";
    String stateId = "STATE-MA-01";
    String cityId = "CITY-BOSTON-01";
    String facilityId = "FACILITY-ID";
    @InjectMocks
    private LocationHierarchyService locationHierarchyService;
    @Mock
    private LocationHierarchyDuplicateValidator locationHierarchyDuplicateValidator;
    @Mock
    private LocationService locationService;
    @Mock
    private LocationHierarchyRepository locationHierarchyRepository;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private ObjectMapper objectMapper;
    @Captor
    private ArgumentCaptor<LocationHierarchyEntity> locationHierarchyEntityArgumentCaptor;

    @BeforeEach
    void setUpData() {
        organization.setId(organizationId);
        facilityLocation.setId(facilityId);
        facilityLocation.setExternalId(facilityId);
        facilityLocation.setCode(facilityId);
        facilityDomain.setId(facilityId);
        facilityDomain.setExternalId(facilityId);
        facilityDomain.setName(facility);
        countryLocation.setId(countryId);
        stateLocation.setId(stateId);
        cityLocation.setId(cityId);
        address.setCountry(country);
        address.setCountryId(countryId);
        address.setState(state);
        address.setStateId(stateId);
        address.setCityId(cityId);
        address.setCity(city);
    }

    @Test
    void getAllStatesByCountry_ShouldThrowNull_WhenNoResult() {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId("country");
        filter.setPage(1);
        filter.setPerPage(10);
        Page mock = new PageImpl(Collections.emptyList());
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(locationHierarchyRepository.findStates(anyString(), anyString(), any(Pageable.class))).thenReturn(mock);
        assertThat(locationHierarchyService.findAllStatesByCountry(filter).getTotalElements()).isZero();
    }

    @Test
    void getAllCitiesByState_ShouldThrowNull_WhenNoResult() {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId("country");
        filter.setStateId("stateid");
        filter.setPage(1);
        filter.setPerPage(10);
        Page mock = new PageImpl(Collections.emptyList());
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(locationHierarchyRepository.findCities(anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(mock);
        assertThat(locationHierarchyService.findAllCitiesByState(filter).getTotalElements()).isZero();
    }

    @Test
    void getAllFacilitiesByCity_ShouldThrowNull_WhenNoResult() {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId("country");
        filter.setStateId("stateid");
        filter.setCityId("cityid");
        filter.setPage(1);
        filter.setPerPage(10);
        Page mock = new PageImpl(Collections.emptyList());
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(locationHierarchyRepository.findFacilities(anyString(), anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(mock);
        assertThat(locationHierarchyService.findAllFacilitiesByCity(filter).getTotalElements()).isZero();
    }

    @Test
    void getAllStatesByCountry_ShouldReturnPage_whenHasValue() {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId("country");
        filter.setPage(1);
        filter.setPerPage(10);
        List<LocationEntity> list = new ArrayList<>();
        LocationEntity locState = new LocationEntity();
        locState.setId("testStateId");
        locState.setType(STATE);
        list.add(locState);
        LocationEntity locCity = new LocationEntity();
        locCity.setId("testCityId");
        locCity.setType(CITY);
        list.add(locCity);
        Page mock = new PageImpl(list);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(locationHierarchyRepository.findStates(anyString(), anyString(), any(Pageable.class))).thenReturn(mock);
        Page<LocationEntity> result = locationHierarchyService.findAllStatesByCountry(filter);
        assertThat(result.getContent().get(0).getId()).isEqualTo("testStateId");
    }

    @Test
    void getAllCitiesByState_ShouldReturnPage_whenHasValue() {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId("country");
        filter.setStateId("stateId");
        filter.setPage(1);
        filter.setPerPage(10);
        List<LocationEntity> list = new ArrayList<>();
        LocationEntity locCity = new LocationEntity();
        locCity.setId("testCityId");
        locCity.setType(CITY);
        list.add(locCity);
        Page mock = new PageImpl(list);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(locationHierarchyRepository.findCities(anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(mock);
        Page<LocationEntity> result = locationHierarchyService.findAllCitiesByState(filter);
        assertThat(result.getContent().get(0).getId()).isEqualTo("testCityId");
    }

    @Test
    void getAllFacilitiesByCity_ShouldReturnPage_whenHasValue() {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId("country");
        filter.setStateId("StateId");
        filter.setCityId("cityId");
        filter.setPage(1);
        filter.setPerPage(10);
        List<LocationEntity> list = new ArrayList<>();
        LocationEntity locFacility = new LocationEntity();
        locFacility.setId("testFacility");
        locFacility.setType(FACILITY);
        list.add(locFacility);
        Page mock = new PageImpl(list);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(locationHierarchyRepository.findFacilities(anyString(), anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(mock);
        Page<LocationEntity> result = locationHierarchyService.findAllFacilitiesByCity(filter);
        assertThat(result.getContent().get(0).getId()).isEqualTo("testFacility");
    }

    @Test
    void testSetUpLocationHierarchies_ShouldReturnNull_WhenNullValue() {
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        locationHierarchyService.setUpLocationHierarchies(null, null, null, null, packageJourneySegmentEntity);
        assertThat(packageJourneySegmentEntity.getStartLocationHierarchy()).isNull();
        assertThat(packageJourneySegmentEntity.getEndLocationHierarchy()).isNull();
    }

    @Test
    void testSetUpLocationHierarchies_ShouldReturnLocationHierarchy_WhenLocationHasValues() {
        String organizationId = "organizationid";
        Address startAddress = new Address();
        startAddress.setCountry("SCountry");
        startAddress.setCountryName("SCountryName");
        startAddress.setCountryId("SCountry-ID");
        startAddress.setState("SState");
        startAddress.setStateName("SStateName");
        startAddress.setStateId("SState-ID");
        startAddress.setCity("SCity");
        startAddress.setCityName("SCityName");
        startAddress.setCityId("SCity-ID");
        Facility startFacilityDomain = new Facility();
        startFacilityDomain.setId("startFacilityDomain");
        startFacilityDomain.setExternalId("startFacilityDomain");
        Address endAddress = new Address();
        endAddress.setCountry("ECountry");
        endAddress.setCountryName("ECountryName");
        endAddress.setCountryId("ECountry-ID");
        endAddress.setState("EState");
        endAddress.setStateName("EStateName");
        endAddress.setStateId("EState-ID");
        endAddress.setCity("ECity");
        endAddress.setCityName("ECityName");
        endAddress.setCityId("ECity-ID");
        Facility endFacilityDomain = new Facility();
        endFacilityDomain.setId("endFacilityDomain");
        endFacilityDomain.setExternalId("endFacilityDomain");
        List<LocationEntity> locationEntities = createLocations(startAddress, endAddress, startFacilityDomain.getExternalId(), endFacilityDomain.getExternalId());
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        LocationEntity startFacilityEntity = new LocationEntity();
        startFacilityEntity.setType(FACILITY);
        startFacilityEntity.setExternalId(startFacilityDomain.getExternalId());
        locationHierarchy.setFacility(startFacilityEntity);
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setExternalId("TestLocationEntityID");
        locationEntity.setId("TestLocationEntityID");

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(locationService.findByOrganizationIdAndExternalIds(anyList())).thenReturn(locationEntities);
        when(locationHierarchyRepository.findLHWithNullFacility(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of(locationHierarchy));
        when(locationService.createLocation(anyList(), anyString(), any(LocationType.class), anyString(), anyString()))
                .thenReturn(locationEntity);

        locationHierarchyService.setUpLocationHierarchies(startAddress, startFacilityDomain, endAddress, endFacilityDomain, packageJourneySegmentEntity);
        assertThat(packageJourneySegmentEntity.getStartLocationHierarchy()).isNotNull();
        assertThat(packageJourneySegmentEntity.getEndLocationHierarchy()).isNotNull();
        verify(locationHierarchyDuplicateValidator, times(2)).validateLocationHierarchy(any(LocationHierarchyEntity.class));
    }

    private List<LocationEntity> createLocations(Address startAddress, Address endAddress, String startFacility, String endFacility) {
        LocationEntity startCountry = new LocationEntity();
        startCountry.setType(COUNTRY);
        startCountry.setExternalId(startAddress.getCountryId());

        LocationEntity startState = new LocationEntity();
        startState.setType(STATE);
        startState.setExternalId(startAddress.getStateId());

        LocationEntity startCity = new LocationEntity();
        startCity.setType(CITY);
        startCity.setExternalId(startAddress.getCityId());

        LocationEntity startFacilityEntity = new LocationEntity();
        startFacilityEntity.setType(FACILITY);
        startFacilityEntity.setExternalId(startFacility);

        LocationEntity endCountry = new LocationEntity();
        endCountry.setType(COUNTRY);
        endCountry.setExternalId(endAddress.getCountryId());

        LocationEntity endState = new LocationEntity();
        endState.setType(STATE);
        endState.setExternalId(endAddress.getStateId());

        LocationEntity endCity = new LocationEntity();
        endCity.setType(CITY);
        endCity.setExternalId(endAddress.getCityId());

        LocationEntity endFacilityEntity = new LocationEntity();
        endFacilityEntity.setType(FACILITY);
        endFacilityEntity.setExternalId(endFacility);

        return List.of(startCountry, startState, startCity, endCountry, endState, endCity, startFacilityEntity, endFacilityEntity);
    }

    @Test
    void givenIdsWithData_WhenFindAllById_ReturnMappedTuppleAsLocationHierarchyEntity() {
        //GIVEN:
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(BaseEntity_.ID, String.class)).thenReturn("1");
        when(tuple.get(COUNTRY_CODE, String.class)).thenReturn("PH");
        when(tuple.get(STATE_CODE, String.class)).thenReturn("MM");
        when(tuple.get(CITY_CODE, String.class)).thenReturn("MANDA");
        when(tuple.get(FACILITY_CODE, String.class)).thenReturn("FLC001");
        when(tuple.get(FACILITY_LOCATION_CODE, String.class)).thenReturn("FLC001_LOC");
        when(tuple.get(FACILITY_NAME, String.class)).thenReturn("JRS Express");
        when(tuple.get(PREFIX_COUNTRY.concat(CODE), String.class)).thenReturn("PH");
        when(tuple.get(PREFIX_COUNTRY.concat(ID), String.class)).thenReturn("123");
        when(tuple.get(PREFIX_COUNTRY.concat(EXTERNAL_ID), String.class)).thenReturn("321");
        when(tuple.get(PREFIX_COUNTRY.concat(NAME), String.class)).thenReturn("Philippines");
        when(tuple.get(PREFIX_STATE.concat(CODE), String.class)).thenReturn("MM");
        when(tuple.get(PREFIX_STATE.concat(ID), String.class)).thenReturn("2");
        when(tuple.get(PREFIX_STATE.concat(EXTERNAL_ID), String.class)).thenReturn("231");
        when(tuple.get(PREFIX_STATE.concat(NAME), String.class)).thenReturn("Metro Manila");
        when(tuple.get(PREFIX_CITY.concat(CODE), String.class)).thenReturn("MANDA");
        when(tuple.get(PREFIX_CITY.concat(ID), String.class)).thenReturn("3");
        when(tuple.get(PREFIX_CITY.concat(EXTERNAL_ID), String.class)).thenReturn("312");
        when(tuple.get(PREFIX_CITY.concat(NAME), String.class)).thenReturn("Mandaluyong");
        when(tuple.get(PREFIX_FACILITY.concat(CODE), String.class)).thenReturn("FLC001_LOC");
        when(tuple.get(PREFIX_FACILITY.concat(ID), String.class)).thenReturn("4");
        when(tuple.get(PREFIX_FACILITY.concat(EXTERNAL_ID), String.class)).thenReturn("412");
        when(tuple.get(PREFIX_FACILITY.concat(NAME), String.class)).thenReturn("JRS-Express");

        Set<String> ids = Set.of("1", "2");
        when(locationHierarchyRepository.findAllByIdsIn(ids)).thenReturn(List.of(tuple));
        //WHEN:
        List<LocationHierarchyEntity> locationHierarchyEntities = locationHierarchyService.findAllByIds(ids);
        //THEN:
        verify(locationHierarchyRepository, times(1)).findAllByIdsIn(ids);
        assertThat(locationHierarchyEntities).isNotNull().hasSize(1);
        assertLocationHierarchy(locationHierarchyEntities.get(0));
    }

    @Test
    void givenNullParams_WhenFindAllById_ReturnEmptyListWithOUtInvokingRepository() {
        List<LocationHierarchyEntity> locationHierarchyEntities = locationHierarchyService.findAllByIds(null);
        assertThat(locationHierarchyEntities).isNotNull().isEmpty();
        verifyNoInteractions(locationHierarchyRepository);
    }

    @Test
    void givenFacility_WhenSetUpLocationHierarchyFromFacilityAndSave_ThenGenerateLocationHierarchyFromFacility() {
        //GIVEN:
        Address startAddress = new Address();
        startAddress.setCountryId("countryExtId");
        startAddress.setStateId("stateExtId");
        startAddress.setCityId("cityExtId");
        startAddress.setCountryName("countryName");
        startAddress.setStateName("stateName");
        startAddress.setCityName("cityName");

        Facility startFacility = new Facility();
        startFacility.setExternalId("facilityExtId");
        startFacility.setLocation(startAddress);
        startFacility.setName("facilityName");

        LocationEntity country = new LocationEntity();
        country.setId("countryId");
        country.setType(COUNTRY);
        country.setName("countryName");
        LocationEntity state = new LocationEntity();
        state.setId("stateId");
        state.setType(STATE);
        state.setName("stateName");
        LocationEntity city = new LocationEntity();
        city.setId("cityId");
        city.setType(CITY);
        city.setName("cityName");
        LocationEntity facility = new LocationEntity();
        facility.setId("facilityId");
        facility.setType(FACILITY);
        facility.setName("facilityName");
        facility.setCode("facilityCode");

        List<LocationEntity> locationEntities = List.of(country, state, city, facility);

        when(locationService.findByOrganizationIdAndExternalIds(List.of("countryExtId", "stateExtId", "cityExtId", "facilityExtId", "")))
                .thenReturn(locationEntities);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");

        when(locationService.createLocation(locationEntities, startAddress.getCountryId(), COUNTRY, startAddress.getCountryName(), startAddress.getCountryName())).thenReturn(country);
        when(locationService.createLocation(locationEntities, startAddress.getStateId(), STATE, startAddress.getStateName(), startAddress.getStateName())).thenReturn(state);
        when(locationService.createLocation(locationEntities, startAddress.getCityId(), CITY, startAddress.getCityName(), startAddress.getCityName())).thenReturn(city);
        when(locationService.createLocation(locationEntities, startFacility.getExternalId(), FACILITY, startFacility.getName(), startFacility.getCode())).thenReturn(facility);
        //WHEN:
        locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(startFacility);
        //THEN:
        verify(locationHierarchyRepository).saveAndFlush(locationHierarchyEntityArgumentCaptor.capture());
        assertThat(locationHierarchyEntityArgumentCaptor.getValue()).isNotNull();
    }

    @Test
    void givenDataIntegrityViolationException_WhenSetUpLocationHierarchyFromFacilityAndSave_ThenRefetchToGetData() {
        //GIVEN:
        Address startAddress = new Address();
        startAddress.setCountryId("countryExtId");
        startAddress.setStateId("stateExtId");
        startAddress.setCityId("cityExtId");
        startAddress.setCountryName("countryName");
        startAddress.setStateName("stateName");
        startAddress.setCityName("cityName");

        Facility startFacility = new Facility();
        startFacility.setExternalId("facilityExtId");
        startFacility.setLocation(startAddress);
        startFacility.setName("facilityName");

        LocationEntity country = new LocationEntity();
        country.setId("countryId");
        country.setType(COUNTRY);
        country.setName("countryName");
        LocationEntity state = new LocationEntity();
        state.setId("stateId");
        state.setType(STATE);
        state.setName("stateName");
        LocationEntity city = new LocationEntity();
        city.setId("cityId");
        city.setType(CITY);
        city.setName("cityName");
        LocationEntity facility = new LocationEntity();
        facility.setId("facilityId");
        facility.setType(FACILITY);
        facility.setName("facilityName");
        facility.setCode("facilityCode");

        List<LocationEntity> locationEntities = List.of(country, state, city, facility);

        when(locationService.findByOrganizationIdAndExternalIds(List.of("countryExtId", "stateExtId", "cityExtId", "facilityExtId", "")))
                .thenReturn(locationEntities);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");

        when(locationService.createLocation(locationEntities, startAddress.getCountryId(), COUNTRY, startAddress.getCountryName(), startAddress.getCountryName())).thenReturn(country);
        when(locationService.createLocation(locationEntities, startAddress.getStateId(), STATE, startAddress.getStateName(), startAddress.getStateName())).thenReturn(state);
        when(locationService.createLocation(locationEntities, startAddress.getCityId(), CITY, startAddress.getCityName(), startAddress.getCityName())).thenReturn(city);
        when(locationService.createLocation(locationEntities, startFacility.getExternalId(), FACILITY, startFacility.getName(), startFacility.getCode())).thenReturn(facility);
        when(locationHierarchyRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("location unique error"));
        //WHEN:
        locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(startFacility);
        //THEN:
        verify(locationHierarchyRepository, times(1)).findByLocationExternalId("countryExtId", "cityExtId", "stateExtId", "facilityExtId", "orgId");
    }


    private void assertLocationHierarchy(LocationHierarchyEntity locationHierarchy) {
        assertThat(locationHierarchy).isNotNull();
        assertThat(locationHierarchy.getId()).isEqualTo("1");
        assertThat(locationHierarchy.getCountry()).isNotNull();
        assertThat(locationHierarchy.getCountry().getId()).isEqualTo("123");
        assertThat(locationHierarchy.getCountry().getExternalId()).isEqualTo("321");
        assertThat(locationHierarchy.getCountry().getName()).isEqualTo("Philippines");
        assertThat(locationHierarchy.getCountryCode()).isEqualTo("PH");
        assertThat(locationHierarchy.getState()).isNotNull();
        assertThat(locationHierarchy.getState().getId()).isEqualTo("2");
        assertThat(locationHierarchy.getState().getExternalId()).isEqualTo("231");
        assertThat(locationHierarchy.getState().getName()).isEqualTo("Metro Manila");
        assertThat(locationHierarchy.getStateCode()).isEqualTo("MM");
        assertThat(locationHierarchy.getCity()).isNotNull();
        assertThat(locationHierarchy.getCity().getExternalId()).isEqualTo("312");
        assertThat(locationHierarchy.getCity().getId()).isEqualTo("3");
        assertThat(locationHierarchy.getCity().getName()).isEqualTo("Mandaluyong");
        assertThat(locationHierarchy.getCityCode()).isEqualTo("MANDA");
        assertThat(locationHierarchy.getFacility()).isNotNull();
        assertThat(locationHierarchy.getFacility().getId()).isEqualTo("4");
        assertThat(locationHierarchy.getFacility().getExternalId()).isEqualTo("412");
        assertThat(locationHierarchy.getFacility().getName()).isEqualTo("JRS-Express");
        assertThat(locationHierarchy.getFacilityCode()).isEqualTo("FLC001");
        assertThat(locationHierarchy.getFacilityName()).isEqualTo("JRS Express");
        assertThat(locationHierarchy.getFacilityLocationCode()).isEqualTo("FLC001_LOC");
    }

    @Test
    void givenValidParams_whenFinByExternalId_thenReturnData() {
        LocationHierarchyEntity mockData = mock(LocationHierarchyEntity.class);
        when(locationHierarchyRepository.findByLocationExternalId(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(mockData);

        LocationHierarchyEntity actualResult = locationHierarchyService
                .findByLocationExternalIdWithNoFacility("ext_country_id", "ext_city_id", "ext_state_id");

        assertThat(actualResult).isNotNull();
        verify(locationHierarchyRepository, times(1))
                .findByLocationExternalId(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    void givenLocationTypeIsFacilityAndNewlyCreated_whenReceiveLocationMessage_thenCreateLocationAndLocationHierarchy()
            throws JsonProcessingException {
        final String givenPayload = "{\"message\":\"Assuming Valid Payload\"}";
        final String givenTransactionId = UUID.randomUUID().toString();
        final QPortalLocation qPortalLocationMock = new QPortalLocation();
        qPortalLocationMock.setCountryId(UUID.randomUUID().toString());
        qPortalLocationMock.setStateProvinceId(UUID.randomUUID().toString());
        qPortalLocationMock.setCityId(UUID.randomUUID().toString());
        final QPortalChange qPortalChangeMock = mock(QPortalChange.class);
        when(qPortalChangeMock.getChanges()).thenReturn(qPortalLocationMock);
        when(objectMapper.readValue(anyString(), eq(QPortalChange.class))).thenReturn(qPortalChangeMock);
        when(locationService.getLocationType(any(QPortalLocation.class))).thenReturn(FACILITY);
        when(locationService.createOrUpdateLocation(any(QPortalLocation.class))).thenReturn(Optional.of(mock(LocationEntity.class)));
        final List<LocationEntity> ancestorLocations = List.of(createLocation(COUNTRY), createLocation(STATE), createLocation(CITY));
        when(locationService.findByOrganizationIdAndExternalIds(anyList())).thenReturn(ancestorLocations);

        locationHierarchyService.receiveLocationMessage(givenPayload, givenTransactionId);

        verify(locationService, times(1)).createOrUpdateLocation(any(QPortalLocation.class));
        verify(userDetailsProvider, times(2)).getCurrentOrganizationId();
        verify(locationService, times(1)).findByOrganizationIdAndExternalIds(anyList());
        verify(locationHierarchyRepository, times(1)).save(any(LocationHierarchyEntity.class));
    }

    @Test
    void givenLocationTypeIsFacilityAndExistingInDB_whenReceiveLocationMessage_thenUpdateLocationAndLocationHierarchy()
            throws JsonProcessingException {
        final String givenPayload = "{\"message\":\"Assuming Valid Payload\"}";
        final String givenTransactionId = UUID.randomUUID().toString();
        final QPortalLocation qPortalLocationMock = new QPortalLocation();
        qPortalLocationMock.setCountryId(UUID.randomUUID().toString());
        qPortalLocationMock.setStateProvinceId(UUID.randomUUID().toString());
        qPortalLocationMock.setCityId(UUID.randomUUID().toString());
        qPortalLocationMock.setName("NEW_NAME");
        qPortalLocationMock.setCode("NEW_CODE");
        qPortalLocationMock.setLocationCode("NEW_LOCATION_CODE");
        final QPortalChange qPortalChangeMock = mock(QPortalChange.class);
        when(qPortalChangeMock.getChanges()).thenReturn(qPortalLocationMock);
        when(objectMapper.readValue(anyString(), eq(QPortalChange.class))).thenReturn(qPortalChangeMock);
        when(locationService.getLocationType(any(QPortalLocation.class))).thenReturn(FACILITY);
        final LocationEntity facilityLocation = createLocation(FACILITY);
        when(locationService.createOrUpdateLocation(any(QPortalLocation.class))).thenReturn(Optional.of(facilityLocation));
        final List<LocationEntity> ancestorLocations = List.of(createLocation(COUNTRY), createLocation(STATE), createLocation(CITY));
        when(locationService.findByOrganizationIdAndExternalIds(anyList())).thenReturn(ancestorLocations);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(UUID.randomUUID().toString());
        final LocationHierarchyEntity givenLocationHierarchy = new LocationHierarchyEntity();
        givenLocationHierarchy.setFacilityName("OLD_NAME");
        givenLocationHierarchy.setFacilityCode("OLD_CODE");
        givenLocationHierarchy.setFacilityLocationCode("OLD_FACILITY_CODE");
        when(locationHierarchyRepository.findByCountryAndStateAndCityAndFacilityAndOrganizationId(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(givenLocationHierarchy));

        locationHierarchyService.receiveLocationMessage(givenPayload, givenTransactionId);

        verify(locationService, times(1)).createOrUpdateLocation(any(QPortalLocation.class));
        verify(userDetailsProvider, times(2)).getCurrentOrganizationId();
        verify(locationService, times(1)).findByOrganizationIdAndExternalIds(anyList());

        final ArgumentCaptor<LocationHierarchyEntity> captor = ArgumentCaptor.forClass(LocationHierarchyEntity.class);
        verify(locationHierarchyRepository, times(1)).save(captor.capture());
        LocationHierarchyEntity actualLocationHierarchyUpdated = captor.getValue();
        assertThat(actualLocationHierarchyUpdated.getFacilityName()).isEqualTo(qPortalLocationMock.getName());
        assertThat(actualLocationHierarchyUpdated.getFacilityCode()).isEqualTo(qPortalLocationMock.getCode());
        assertThat(actualLocationHierarchyUpdated.getFacilityLocationCode()).isEqualTo(qPortalLocationMock.getLocationCode());
    }

    private LocationEntity createLocation(final LocationType locationType) {
        final LocationEntity location = new LocationEntity();
        location.setId(UUID.randomUUID().toString());
        location.setType(locationType);
        return location;
    }

    @Test
    void givenUnsupportedLocationType_whenReceiveLocationMessage_thenDoNotProcess()
            throws JsonProcessingException {
        final String givenPayload = "{\"message\":\"Assuming Valid Payload\"}";
        final String givenTransactionId = UUID.randomUUID().toString();
        final QPortalChange qPortalChangeMock = mock(QPortalChange.class);
        when(qPortalChangeMock.getChanges()).thenReturn(mock(QPortalLocation.class));
        when(objectMapper.readValue(anyString(), eq(QPortalChange.class)))
                .thenReturn(qPortalChangeMock);
        when(locationService.getLocationType(any(QPortalLocation.class)))
                .thenReturn(null);

        locationHierarchyService.receiveLocationMessage(givenPayload, givenTransactionId);

        verify(locationService, times(1))
                .getLocationType(any(QPortalLocation.class));
        verifyNoInteractions(locationHierarchyRepository, userDetailsProvider);
    }

    @Test
    void givenSupportedLocationTypeButNotFacility_whenReceiveLocationMessage_thenCreateLocation()
            throws JsonProcessingException {
        final String givenPayload = "{\"message\":\"Assuming Valid Payload\"}";
        final String givenTransactionId = UUID.randomUUID().toString();
        final QPortalChange qPortalChangeMock = mock(QPortalChange.class);
        when(qPortalChangeMock.getChanges()).thenReturn(mock(QPortalLocation.class));
        when(objectMapper.readValue(anyString(), eq(QPortalChange.class)))
                .thenReturn(qPortalChangeMock);
        when(locationService.getLocationType(any(QPortalLocation.class)))
                .thenReturn(COUNTRY);

        locationHierarchyService.receiveLocationMessage(givenPayload, givenTransactionId);

        verify(locationService, times(1))
                .getLocationType(any(QPortalLocation.class));
        verify(locationService, times(1))
                .createOrUpdateLocation(any(QPortalLocation.class));
        verifyNoInteractions(locationHierarchyRepository, userDetailsProvider);
    }

    @Test
    void givenInvalidJsonPayload_whenReceiveLocationMessage_thenDoNotProcess()
            throws JsonProcessingException {
        final String givenPayload = "given an invalid json";
        final String givenTransactionId = UUID.randomUUID().toString();

        when(objectMapper.readValue(anyString(), eq(QPortalChange.class)))
                .thenThrow(new JsonMappingException("Unable to parse."));

        assertThrows(LocationMessageException.class,
                () -> locationHierarchyService.receiveLocationMessage(givenPayload, givenTransactionId));

        verifyNoInteractions(locationHierarchyRepository, locationService, userDetailsProvider);
    }

    @Test
    void givenNullPayload_whenReceiveLocationMessage_thenDoNotProcess() {
        final String givenPayload = null;
        final String givenTransactionId = UUID.randomUUID().toString();

        locationHierarchyService.receiveLocationMessage(givenPayload, givenTransactionId);

        verifyNoInteractions(objectMapper, locationHierarchyRepository, locationService, userDetailsProvider);
    }

}
