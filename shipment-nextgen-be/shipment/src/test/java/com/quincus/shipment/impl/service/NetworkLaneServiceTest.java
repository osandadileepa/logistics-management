package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.exception.NetworkLaneNotFoundException;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
import com.quincus.shipment.impl.helper.NetworkLaneDataHelper;
import com.quincus.shipment.impl.mapper.NetworkLaneCriteriaMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentMapper;
import com.quincus.shipment.impl.repository.NetworkLaneRepository;
import com.quincus.shipment.impl.repository.criteria.NetworkLaneCriteria;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.projection.NetworkLaneProjectionListingPage;
import com.quincus.shipment.impl.repository.specification.NetworkLaneSpecification;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneServiceTest {
    private static final String organizationId = "TestOrgId";
    private static final String serviceTypeCode = "TestServiceType";
    private static final String serviceTypeId = "TestServiceTypeId";
    private static final String originFacilityId = "TestOriginFaciltyId";
    private static final String originCountryId = "TestOriginCountryId";
    private static final String originCountryName = "TestOriginCountryName";
    private static final String originStateId = "TestOriginStateId";
    private static final String originStateName = "TestOriginStateName";
    private static final String originCityId = "TestOriginCityId";
    private static final String originCityName = "TestOriginCityName";

    private static final String destinationCountryId = "TestDestinationCountryId";
    private static final String destinationCountryName = "TestDestinationCountryName";
    private static final String destinationStateId = "TestDestinationStateId";
    private static final String destinationStateName = "TestDestinationStateName";
    private static final String destinationCityId = "TestDestinationCityId";
    private static final String destinationCityName = "TestDestinationCityName";
    private static final String segmentStartFacilityName = "startFacilityName";
    private static final String segmentEndFacilityName = "segmentEndFacilityName";
    private static final String partnerName = "test-partner-name";

    @InjectMocks
    private NetworkLaneService networkLaneService;
    @Mock
    private NetworkLaneRepository networkLaneRepository;
    @Mock
    private NetworkLaneMapper networkLaneMapper;
    @Mock
    private NetworkLaneSegmentMapper networkLaneSegmentMapper;
    @Mock
    private ServiceTypeAsyncService serviceTypeAsyncService;
    @Mock
    private FacilityAsyncService facilityService;
    @Mock
    private PartnerAsyncService partnerAsyncService;
    @Mock
    private LocationHierarchyAsyncService locationHierarchyAsyncService;
    @Mock
    private AddressAsyncService addressAsyncService;
    @Mock
    private NetworkLaneCriteriaMapper networkLaneCriteriaMapper;
    @Mock
    private NetworkLaneProjectionListingPage projectionListingPage;
    @Mock
    private NetworkLaneEnrichmentService networkLaneEnrichmentService;
    @Mock
    private NetworkLaneSegmentService networkLaneSegmentService;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private NetworkLaneDataHelper networkLaneDataHelper;

    @Test
    void testFindNetworkLaneServiceByFilter() {
        // GIVEN:
        NetworkLaneFilter networkLaneFilter = mock(NetworkLaneFilter.class);
        String organizationId = "org_id";
        NetworkLaneCriteria criteria = mock(NetworkLaneCriteria.class);
        NetworkLaneSpecification networkLaneSpecification = mock(NetworkLaneSpecification.class);
        Pageable page = mock(Pageable.class);
        long resultCount = 10L;
        List<NetworkLaneEntity> result = new ArrayList<>();
        List<NetworkLane> networkLanes = new ArrayList<>();

        // Mock interactions
        when(networkLaneCriteriaMapper.mapFilterToCriteria(networkLaneFilter, organizationId)).thenReturn(criteria);
        when(criteria.buildSpecification()).thenReturn(networkLaneSpecification);
        when(networkLaneSpecification.buildPageable()).thenReturn(page);
        when(networkLaneRepository.count(networkLaneSpecification)).thenReturn(resultCount);
        when(projectionListingPage.findAllWithPagination(networkLaneSpecification, page)).thenReturn(result);
        when(networkLaneMapper.mapEntitiesToDomain(result)).thenReturn(networkLanes);
        when(networkLaneFilter.getPageNumber()).thenReturn(0);

        // Invoke the method under test
        NetworkLaneFilterResult finalResult = networkLaneService.findAll(networkLaneFilter, organizationId);

        // Verify the expected behavior
        verify(networkLaneCriteriaMapper).mapFilterToCriteria(networkLaneFilter, organizationId);
        verify(criteria).buildSpecification();
        verify(networkLaneSpecification).buildPageable();
        verify(networkLaneRepository).count(networkLaneSpecification);
        verify(projectionListingPage).findAllWithPagination(networkLaneSpecification, page);
        verify(networkLaneMapper).mapEntitiesToDomain(result);
        verify(networkLaneEnrichmentService).enrichNetworkLanesDurationCalculation(any());
        verify(networkLaneFilter).getPageNumber();

        // Perform assertions
        assertThat(finalResult.getResult()).isEqualTo(networkLanes);
        assertThat(finalResult.filter()).isEqualTo(networkLaneFilter);
        assertThat(finalResult.totalElements()).isEqualTo(resultCount);
    }

    @Test
    void givenAllNetworkLaneDataWhenSaveThenTriggerAllExpectedInvoke() {
        // GIVEN:
        ServiceType serviceType = generateServiceType();
        Facility originFacility = generateFacilityWithAddress(originFacilityId, originCountryId, originCountryName, originStateId, originStateName, originCityId, originCityName);
        Address destinationAddress = generateAddress(destinationCountryId, destinationCountryName, destinationStateId, destinationStateName, destinationCityId, destinationCityName);

        NetworkLane networkLane = new NetworkLane();
        networkLane.setServiceType(serviceType);
        networkLane.setOrganizationId(organizationId);
        networkLane.setOriginFacility(originFacility);
        networkLane.setDestination(destinationAddress);

        Facility startSegmentFacility = generateFacility(segmentStartFacilityName);
        Facility endSegmentFacility = generateFacility(segmentEndFacilityName);
        Partner partner = generatePartner();

        NetworkLaneSegment networkLaneSegment = new NetworkLaneSegment();
        networkLaneSegment.setPartner(partner);
        networkLaneSegment.setStartFacility(startSegmentFacility);
        networkLaneSegment.setEndFacility(endSegmentFacility);
        networkLaneSegment.setOrganizationId(organizationId);
        networkLane.addNetworkLaneSegment(networkLaneSegment);

        NetworkLaneEntity entity = new NetworkLaneEntity();
        entity.setOrganizationId(organizationId);

        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setName(partnerName);
        partnerEntity.setOrganizationId(organizationId);

        NetworkLaneSegmentEntity segmentEntity = new NetworkLaneSegmentEntity();
        segmentEntity.setOrganizationId(organizationId);
        entity.addNetworkLaneSegment(segmentEntity);

        when(networkLaneMapper.mapDomainToEntity(networkLane)).thenReturn(entity);
        when(networkLaneSegmentMapper.mapDomainToEntity(networkLaneSegment)).thenReturn(segmentEntity);

        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        serviceTypeEntity.setCode(serviceTypeCode);
        serviceTypeEntity.setId(serviceTypeId);

        LocationHierarchyEntity locationHierarchyEntity1 = new LocationHierarchyEntity();
        locationHierarchyEntity1.setId("id-1");

        LocationHierarchyEntity locationHierarchyEntity2 = new LocationHierarchyEntity();
        locationHierarchyEntity2.setId("id-2");

        when(addressAsyncService.generateCityAddressFromQPortal(destinationCityName, destinationStateName, destinationCountryName, organizationId)).thenReturn(destinationAddress);
        when(facilityService.generateFacilityFromQPortal(originFacilityId, organizationId)).thenReturn(originFacility);
        when(facilityService.generateFacilityFromQPortal(segmentStartFacilityName, organizationId)).thenReturn(startSegmentFacility);
        when(facilityService.generateFacilityFromQPortal(segmentEndFacilityName, organizationId)).thenReturn(endSegmentFacility);
        when(facilityService.generateFacilityFromQPortal(segmentEndFacilityName, organizationId)).thenReturn(endSegmentFacility);
        when(partnerAsyncService.findOrCreatePartnerByName(partnerName, organizationId)).thenReturn(partnerEntity);
        when(locationHierarchyAsyncService.setUpLocationHierarchies(originFacility, organizationId)).thenReturn(locationHierarchyEntity1);
        when(locationHierarchyAsyncService.setUpLocationHierarchies(destinationAddress, organizationId))
                .thenReturn(locationHierarchyEntity1)
                .thenReturn(locationHierarchyEntity2);

        // When:
        networkLaneService.saveFromBulkUpload(networkLane, organizationId);

        // THEN:
        verify(networkLaneMapper, times(1)).mapDomainToEntity(networkLane);
        verify(networkLaneSegmentMapper, times(1)).mapDomainToEntity(networkLaneSegment);
        verify(locationHierarchyAsyncService, times(1)).setUpLocationHierarchies(originFacility, organizationId);
        verify(locationHierarchyAsyncService, times(1)).setUpLocationHierarchies(destinationAddress, organizationId);
        verify(facilityService, times(1)).generateFacilityFromQPortal(originFacilityId, organizationId);
        verify(facilityService, times(1)).generateFacilityFromQPortal(originFacilityId, organizationId);
        verify(facilityService, times(1)).generateFacilityFromQPortal(segmentStartFacilityName, organizationId);
        verify(facilityService, times(1)).generateFacilityFromQPortal(segmentEndFacilityName, organizationId);
        verify(addressAsyncService, times(1)).generateCityAddressFromQPortal(destinationCityName, destinationStateName, destinationCountryName, organizationId);
        verify(networkLaneRepository, times(1)).saveAndFlush(entity);
        verify(networkLaneDataHelper, times(1)).enrichLaneSegmentTimezoneFields(any());
    }

    @Test
    void givenFirstSegmentHasNoStartFacilityAndEndSegmentNoEndFacilityWhenSaveThenShouldUseOriginAndDestination() {
        // GIVEN:
        ServiceType serviceType = generateServiceType();
        Facility originFacility = generateFacilityWithAddress(originFacilityId, originCountryId, originCountryName, originStateId, originStateName, originCityId, originCityName);
        Address destinationAddress = generateAddress(destinationCountryId, destinationCountryName, destinationStateId, destinationStateName, destinationCityId, destinationCityName);

        NetworkLane networkLane = new NetworkLane();
        networkLane.setServiceType(serviceType);
        networkLane.setOrganizationId(organizationId);
        networkLane.setOriginFacility(originFacility);
        networkLane.setDestination(destinationAddress);

        Partner partner = generatePartner();

        NetworkLaneSegment networkLaneSegment = new NetworkLaneSegment();
        networkLaneSegment.setPartner(partner);
        networkLaneSegment.setOrganizationId(organizationId);
        networkLane.addNetworkLaneSegment(networkLaneSegment);

        NetworkLaneEntity entity = new NetworkLaneEntity();
        entity.setOrganizationId(organizationId);

        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setName(partnerName);
        partnerEntity.setOrganizationId(organizationId);

        NetworkLaneSegmentEntity segmentEntity = new NetworkLaneSegmentEntity();
        segmentEntity.setOrganizationId(organizationId);
        entity.addNetworkLaneSegment(segmentEntity);

        when(networkLaneMapper.mapDomainToEntity(networkLane)).thenReturn(entity);
        when(locationHierarchyAsyncService.setUpLocationHierarchies(networkLane.getOriginFacility(), organizationId)).thenReturn(new LocationHierarchyEntity());
        when(locationHierarchyAsyncService.setUpLocationHierarchies(networkLane.getDestination(), organizationId)).thenReturn(new LocationHierarchyEntity());
        when(networkLaneSegmentMapper.mapDomainToEntity(networkLaneSegment)).thenReturn(segmentEntity);

        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        serviceTypeEntity.setCode(serviceTypeCode);
        serviceTypeEntity.setId(serviceTypeId);
        when(addressAsyncService.generateCityAddressFromQPortal(destinationCityName, destinationStateName, destinationCountryName, organizationId)).thenReturn(destinationAddress);
        when(facilityService.generateFacilityFromQPortal(originFacilityId, organizationId)).thenReturn(originFacility);
        when(partnerAsyncService.findOrCreatePartnerByName(partnerName, organizationId)).thenReturn(partnerEntity);

        // When:
        networkLaneService.saveFromBulkUpload(networkLane, organizationId);

        // THEN:
        assertThat(segmentEntity.getStartLocationHierarchy()).isNotNull();
        assertThat(segmentEntity.getEndLocationHierarchy()).isNotNull();

    }

    @Test
    void givenFirstSegmentHasStartFacilityAndEndSegmentHasEndFacility_WhenSave_ThenShouldUseOriginAndDestination() {
        // GIVEN:
        ServiceType serviceType = generateServiceType();
        Facility originFacility = generateFacilityWithAddress(originFacilityId, originCountryId, originCountryName, originStateId, originStateName, originCityId, originCityName);
        Address destinationAddress = generateAddress(destinationCountryId, destinationCountryName, destinationStateId, destinationStateName, destinationCityId, destinationCityName);

        NetworkLane networkLane = new NetworkLane();
        networkLane.setServiceType(serviceType);
        networkLane.setOrganizationId(organizationId);
        networkLane.setOriginFacility(originFacility);
        networkLane.setDestination(destinationAddress);

        Partner partner = generatePartner();

        Facility startFacility = new Facility();
        startFacility.setId(UUID.randomUUID().toString());
        Facility endFacility = new Facility();
        endFacility.setId(UUID.randomUUID().toString());

        NetworkLaneSegment networkLaneSegment = new NetworkLaneSegment();
        networkLaneSegment.setPartner(partner);
        networkLaneSegment.setOrganizationId(organizationId);
        networkLaneSegment.setStartFacility(startFacility);
        networkLaneSegment.setEndFacility(endFacility);
        networkLane.addNetworkLaneSegment(networkLaneSegment);

        NetworkLaneEntity entity = new NetworkLaneEntity();
        entity.setOrganizationId(organizationId);

        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setName(partnerName);
        partnerEntity.setOrganizationId(organizationId);

        NetworkLaneSegmentEntity segmentEntity = new NetworkLaneSegmentEntity();
        segmentEntity.setOrganizationId(organizationId);
        entity.addNetworkLaneSegment(segmentEntity);

        when(networkLaneMapper.mapDomainToEntity(networkLane)).thenReturn(entity);
        when(locationHierarchyAsyncService.setUpLocationHierarchies(networkLane.getOriginFacility(), organizationId)).thenReturn(new LocationHierarchyEntity());
        when(locationHierarchyAsyncService.setUpLocationHierarchies(networkLane.getDestination(), organizationId)).thenReturn(new LocationHierarchyEntity());
        when(networkLaneSegmentMapper.mapDomainToEntity(networkLaneSegment)).thenReturn(segmentEntity);

        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        serviceTypeEntity.setCode(serviceTypeCode);
        serviceTypeEntity.setId(serviceTypeId);
        when(addressAsyncService.generateCityAddressFromQPortal(destinationCityName, destinationStateName, destinationCountryName, organizationId)).thenReturn(destinationAddress);
        when(facilityService.generateFacilityFromQPortal(originFacilityId, organizationId)).thenReturn(originFacility);
        when(partnerAsyncService.findOrCreatePartnerByName(partnerName, organizationId)).thenReturn(partnerEntity);

        // When:
        networkLaneService.saveFromBulkUpload(networkLane, organizationId);

        // THEN:
        verify(locationHierarchyAsyncService, times(1)).setUpLocationHierarchies(networkLane.getOriginFacility(), organizationId);
        verify(locationHierarchyAsyncService, times(1)).setUpLocationHierarchies(networkLane.getDestination(), organizationId);
        
        assertThat(segmentEntity.getStartLocationHierarchy())
                .isNotNull()
                .isEqualTo(entity.getOrigin());
        assertThat(segmentEntity.getEndLocationHierarchy())
                .isNotNull()
                .isEqualTo(entity.getDestination());

    }

    @Test
    void givenFirstSegmentNoEndFacility_shouldThrowQuincusValidationException() {
        // GIVEN:
        ServiceType serviceType = generateServiceType();
        Facility originFacility = generateFacilityWithAddress(originFacilityId, originCountryId, originCountryName, originStateId, originStateName, originCityId, originCityName);
        Address destinationAddress = generateAddress(destinationCountryId, destinationCountryName, destinationStateId, destinationStateName, destinationCityId, destinationCityName);

        NetworkLane networkLane = new NetworkLane();
        networkLane.setServiceType(serviceType);
        networkLane.setOrganizationId(organizationId);
        networkLane.setOriginFacility(originFacility);
        networkLane.setDestination(destinationAddress);

        Partner partner = generatePartner();

        NetworkLaneSegment networkLaneSegment1 = new NetworkLaneSegment();
        networkLaneSegment1.setPartner(partner);
        networkLaneSegment1.setSequence("1");
        networkLaneSegment1.setOrganizationId(organizationId);

        NetworkLaneSegment networkLaneSegment2 = new NetworkLaneSegment();
        networkLaneSegment2.setPartner(partner);
        networkLaneSegment2.setSequence("2");
        networkLaneSegment2.setOrganizationId(organizationId);


        networkLane.addNetworkLaneSegment(networkLaneSegment1);
        networkLane.addNetworkLaneSegment(networkLaneSegment2);

        NetworkLaneEntity entity = new NetworkLaneEntity();
        entity.setOrganizationId(organizationId);

        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setName(partnerName);
        partnerEntity.setOrganizationId(organizationId);

        NetworkLaneSegmentEntity segmentEntity1 = new NetworkLaneSegmentEntity();
        segmentEntity1.setOrganizationId(organizationId);
        segmentEntity1.setSequence("1");

        NetworkLaneSegmentEntity segmentEntity2 = new NetworkLaneSegmentEntity();
        segmentEntity2.setOrganizationId(organizationId);
        segmentEntity2.setSequence("2");

        entity.addNetworkLaneSegment(segmentEntity1);
        entity.addNetworkLaneSegment(segmentEntity2);

        when(networkLaneMapper.mapDomainToEntity(networkLane)).thenReturn(entity);
        when(locationHierarchyAsyncService.setUpLocationHierarchies(networkLane.getOriginFacility(), organizationId)).thenReturn(new LocationHierarchyEntity());
        when(locationHierarchyAsyncService.setUpLocationHierarchies(networkLane.getDestination(), organizationId)).thenReturn(new LocationHierarchyEntity());
        when(networkLaneSegmentMapper.mapDomainToEntity(networkLaneSegment1)).thenReturn(segmentEntity1);


        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        serviceTypeEntity.setCode(serviceTypeCode);
        serviceTypeEntity.setId(serviceTypeId);
        when(addressAsyncService.generateCityAddressFromQPortal(destinationCityName, destinationStateName, destinationCountryName, organizationId)).thenReturn(destinationAddress);
        when(facilityService.generateFacilityFromQPortal(originFacilityId, organizationId)).thenReturn(originFacility);

        // When Then:
        assertThatThrownBy(() -> networkLaneService.saveFromBulkUpload(networkLane, organizationId))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessage("End Facility on Network lane Connection sequence: 1 is mandatory");
    }

    @Test
    void givenUnexpectedExceptionWhenSaveThenWrapInQuincusException() {
        NetworkLane networkLane = mock(NetworkLane.class);
        when(networkLaneMapper.mapDomainToEntity(any())).thenThrow(NullPointerException.class);
        assertThatThrownBy(() -> networkLaneService.saveFromBulkUpload(networkLane, organizationId))
                .isInstanceOf(QuincusException.class);
    }

    @Test
    void givenQuincusValidationExceptionWhenSaveThenRethrow() {
        NetworkLane networkLane = mock(NetworkLane.class);
        when(networkLaneMapper.mapDomainToEntity(any())).thenThrow(QuincusValidationException.class);
        assertThatThrownBy(() -> networkLaneService.saveFromBulkUpload(networkLane, organizationId))
                .isInstanceOf(QuincusValidationException.class);
    }

    @Test
    void findById_WithExistingId_ShouldReturnNetworkLane() {
        String id = "sampleId";
        NetworkLaneEntity mockEntity = new NetworkLaneEntity();
        NetworkLane mockNetworkLane = new NetworkLane();

        when(networkLaneRepository.findById(id)).thenReturn(Optional.of(mockEntity));
        when(networkLaneMapper.mapEntityToDomain(mockEntity)).thenReturn(mockNetworkLane);

        NetworkLane result = networkLaneService.findById(id);

        assertThat(result).isEqualTo(mockNetworkLane);
        verify(networkLaneRepository).findById(id);
        verify(networkLaneMapper).mapEntityToDomain(mockEntity);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        String id = "nonExistentId";
        when(networkLaneRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> networkLaneService.findById(id))
                .isInstanceOf(NetworkLaneNotFoundException.class)
                .hasMessageContaining("NetworkLane Id nonExistentId not found.");

        verify(networkLaneRepository).findById(id);
        verify(networkLaneMapper, never()).mapEntityToDomain(any());
    }

    @Test
    void testUpdate_WhenNetworkLaneDoesNotExist_ShouldThrowException() {
        final String givenId = UUID.randomUUID().toString();
        final NetworkLane givenNetworkLane = new NetworkLane();
        givenNetworkLane.setId(givenId);

        when(networkLaneRepository.findById(givenId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NetworkLaneNotFoundException.class)
                .isThrownBy(() -> networkLaneService.update(givenNetworkLane))
                .withMessage(String.format("NetworkLane Id %s not found.", givenId));
    }

    @Test
    void testUpdate_SuccessfullyUpdatesNetworkLane() {
        final String givenId = UUID.randomUUID().toString();
        NetworkLane networkLane = new NetworkLane();
        networkLane.setId(givenId);
        NetworkLaneEntity existingEntity = new NetworkLaneEntity();
        existingEntity.addNetworkLaneSegment(createNetworkLaneSegment(UUID.randomUUID().toString()));
        existingEntity.addNetworkLaneSegment(createNetworkLaneSegment(UUID.randomUUID().toString()));
        existingEntity.addNetworkLaneSegment(createNetworkLaneSegment(UUID.randomUUID().toString()));
        NetworkLane updatedNetworkLane = new NetworkLane();

        when(networkLaneRepository.findById(givenId)).thenReturn(Optional.of(existingEntity));
        when(networkLaneMapper.mapEntityToDomain(any())).thenReturn(updatedNetworkLane);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);

        final NetworkLane result = networkLaneService.update(networkLane);

        verify(networkLaneDataHelper, times(1)).setupOriginUsingNetworkLaneSegment(any(), any());
        verify(networkLaneDataHelper, times(1)).setupDestinationUsingNetworkLaneSegment(any(), any());
        verify(networkLaneDataHelper, times(1)).enrichLaneSegmentTimezoneFields(any());
        assertThat(result).isSameAs(updatedNetworkLane);
    }

    private NetworkLaneSegmentEntity createNetworkLaneSegment(String id) {
        final NetworkLaneSegmentEntity networkLaneSegment = new NetworkLaneSegmentEntity();
        networkLaneSegment.setId(id);
        return networkLaneSegment;
    }

    private ServiceType generateServiceType() {
        ServiceType serviceType = new ServiceType();
        serviceType.setCode(serviceTypeCode);
        serviceType.setOrganizationId(organizationId);
        return serviceType;
    }

    private Facility generateFacilityWithAddress(String externalId, String countryId, String countryName, String stateId, String stateName, String cityId, String cityName) {
        Facility facility = generateFacility(externalId);
        facility.setLocation(generateAddress(countryId, countryName, stateId, stateName, cityId, cityName));

        return facility;
    }

    private Facility generateFacility(String facilityName) {
        Facility facility = new Facility();
        facility.setName(facilityName);
        return facility;
    }

    private Partner generatePartner() {
        Partner partner = new Partner();
        partner.setName(partnerName);
        partner.setOrganizationId(organizationId);
        return partner;
    }

    private Address generateAddress(String countryId, String countryName, String stateId, String stateName, String cityId, String cityName) {
        Address address = new Address();
        address.setCityId(cityId);
        address.setCityName(cityName);
        address.setStateId(stateId);
        address.setStateName(stateName);
        address.setCountryId(countryId);
        address.setCountryName(countryName);
        return address;
    }

}
