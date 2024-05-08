package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.service.LocationHierarchyAsyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneDataHelperTest {
    public static final String TEST_ORG_ID = "org123";
    @InjectMocks
    private NetworkLaneDataHelper helper;
    @Mock
    private LocationHierarchyAsyncService locationHierarchyAsyncService;

    private NetworkLaneEntity entity;

    @BeforeEach
    void setup() {
        this.entity = new NetworkLaneEntity();
        entity.setOrganizationId(TEST_ORG_ID);
    }

    @Test
    void testDestinationSetUsingFacilityLocation() {
        Address location = new Address();
        Facility facility = createFacility("", location);
        mockLocationHierarchyServiceForAddress(location, createLocationHierarchy("lh-123"));

        executeDestinationTest(facility, location, null);
    }

    @Test
    void testDestinationSetUsingFacility() {
        Facility facility = createFacility("1234", null);
        mockLocationHierarchyServiceForFacility(facility, createLocationHierarchy("lh-123"));

        executeDestinationTest(facility, null, facility);
    }

    @Test
    void testOriginSetUsingFacility() {
        Facility facility = createFacility("1234", null);
        mockLocationHierarchyServiceForFacility(facility, createLocationHierarchy("lh-123"));

        executeOriginTest(facility, null, facility);
    }

    @Test
    void testOriginSetUsingFacilityLocation() {
        Address location = new Address();
        Facility facility = createFacility("", location);
        mockLocationHierarchyServiceForAddress(location, createLocationHierarchy("lh-123"));

        executeOriginTest(facility, location, null);
    }

    @Test
    void testSingleSegment() {
        NetworkLaneEntity mockEntity = mock(NetworkLaneEntity.class);

        NetworkLaneSegmentEntity singleSegment = new NetworkLaneSegmentEntity();

        when(mockEntity.getNetworkLaneSegmentList())
                .thenReturn(Collections.singletonList(singleSegment));

        helper.setSegmentTypeOnNetworkLaneSegments(mockEntity);

        assertThat(singleSegment.getType()).isEqualTo(SegmentType.LAST_MILE);
    }

    @Test
    void testThreeSegments() {
        NetworkLaneEntity mockEntity = mock(NetworkLaneEntity.class);

        NetworkLaneSegmentEntity firstSegment = new NetworkLaneSegmentEntity();
        NetworkLaneSegmentEntity middleSegment = new NetworkLaneSegmentEntity();
        NetworkLaneSegmentEntity lastSegment = new NetworkLaneSegmentEntity();

        when(mockEntity.getNetworkLaneSegmentList())
                .thenReturn(Arrays.asList(firstSegment, middleSegment, lastSegment));

        helper.setSegmentTypeOnNetworkLaneSegments(mockEntity);

        assertThat(firstSegment.getType()).isEqualTo(SegmentType.FIRST_MILE);
        assertThat(middleSegment.getType()).isEqualTo(SegmentType.MIDDLE_MILE);
        assertThat(lastSegment.getType()).isEqualTo(SegmentType.LAST_MILE);
    }

    @Test
    void testEnrichLaneSegmentTimezone() {
        NetworkLaneEntity mockEntity = mock(NetworkLaneEntity.class);

        NetworkLaneSegmentEntity firstSegment = new NetworkLaneSegmentEntity();
        firstSegment.setTransportType(TransportType.GROUND);
        firstSegment.setStartLocationHierarchy(createLocationHierarchyWithCity("Asia/Manila UTC+08:00", "Makati"));
        firstSegment.setEndLocationHierarchy(createStartLocationHierarchyWithFacility("Asia/Manila UTC+08:00", "JRS Facility"));
        firstSegment.setPickUpTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        firstSegment.setDropOffTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        NetworkLaneSegmentEntity middleSegment = new NetworkLaneSegmentEntity();
        middleSegment.setTransportType(TransportType.AIR);
        middleSegment.setStartLocationHierarchy(createStartLocationHierarchyWithFacility("Asia/Manila UTC+08:00", "JRS Facility"));
        middleSegment.setEndLocationHierarchy(createStartLocationHierarchyWithFacility("Australia/Sydney UTC+11:00", "Aus Facility"));
        middleSegment.setArrivalTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        middleSegment.setDepartureTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        middleSegment.setLockOutTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        middleSegment.setRecoveryTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        NetworkLaneSegmentEntity lastSegment = new NetworkLaneSegmentEntity();
        lastSegment.setTransportType(TransportType.GROUND);
        lastSegment.setPickUpTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        lastSegment.setDropOffTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        lastSegment.setStartLocationHierarchy(createStartLocationHierarchyWithFacility("Australia/Sydney UTC+11:00", "Aus Facility"));
        lastSegment.setEndLocationHierarchy(createLocationHierarchyWithCity("Australia/Sydney UTC+11:00", "Australia"));

        when(mockEntity.getNetworkLaneSegmentList())
                .thenReturn(Arrays.asList(firstSegment, middleSegment, lastSegment));

        helper.enrichLaneSegmentTimezoneFields(mockEntity);
        assertTimezone(firstSegment, "UTC+08:00", "UTC+08:00");
        assertTimezone(middleSegment, "UTC+08:00", "UTC+11:00");
        assertTimezone(lastSegment, "UTC+11:00", "UTC+11:00");

    }

    private void assertTimezone(NetworkLaneSegmentEntity networkLaneSegment, String startLocationTimezone, String endLocationTimezone) {
        if (networkLaneSegment.getTransportType() == TransportType.AIR) {
            assertThat(networkLaneSegment.getLockOutTimezone()).isEqualTo(startLocationTimezone);
            assertThat(networkLaneSegment.getRecoveryTimezone()).isEqualTo(endLocationTimezone);
            assertThat(networkLaneSegment.getArrivalTimezone()).isEqualTo(endLocationTimezone);
            assertThat(networkLaneSegment.getDepartureTimezone()).isEqualTo(startLocationTimezone);

            assertThat(networkLaneSegment.getPickUpTimezone()).isNull();
            assertThat(networkLaneSegment.getDropOffTimezone()).isNull();

        } else {
            assertThat(networkLaneSegment.getLockOutTimezone()).isNull();
            assertThat(networkLaneSegment.getRecoveryTimezone()).isNull();
            assertThat(networkLaneSegment.getArrivalTimezone()).isNull();
            assertThat(networkLaneSegment.getDepartureTimezone()).isNull();

            assertThat(networkLaneSegment.getPickUpTimezone()).isEqualTo(startLocationTimezone);
            assertThat(networkLaneSegment.getDropOffTimezone()).isEqualTo(endLocationTimezone);
        }
    }


    @Test
    void testEmptyOrNullSegmentList() {
        // Setup for empty list
        NetworkLaneEntity mockEntityEmpty = mock(NetworkLaneEntity.class);
        when(mockEntityEmpty.getNetworkLaneSegmentList()).thenReturn(Collections.emptyList());

        // Setup for null list
        NetworkLaneEntity mockEntityNull = mock(NetworkLaneEntity.class);
        when(mockEntityNull.getNetworkLaneSegmentList()).thenReturn(null);

        helper.setSegmentTypeOnNetworkLaneSegments(mockEntityEmpty);
        verifyNoMoreInteractions(mockEntityEmpty);  // Ensure that no more interactions occur on the mock

        helper.setSegmentTypeOnNetworkLaneSegments(mockEntityNull);
        verifyNoMoreInteractions(mockEntityNull);
    }

    // Helpers

    private Facility createFacility(String id, Address location) {
        Facility facility = new Facility();
        facility.setId(id);
        facility.setLocation(location);
        return facility;
    }

    private void mockLocationHierarchyServiceForAddress(Address input, LocationHierarchyEntity output) {
        when(locationHierarchyAsyncService.setUpLocationHierarchies(input, TEST_ORG_ID))
                .thenReturn(output);
    }

    private void mockLocationHierarchyServiceForFacility(Facility input, LocationHierarchyEntity output) {
        when(locationHierarchyAsyncService.setUpLocationHierarchies(input, TEST_ORG_ID))
                .thenReturn(output);
    }

    private void verifyLocationHierarchyServiceCalledWithAddress(Address input) {
        verify(locationHierarchyAsyncService, times(1)).setUpLocationHierarchies(input, TEST_ORG_ID);
    }

    private void verifyLocationHierarchyServiceCalledWithFacility(Facility input) {
        verify(locationHierarchyAsyncService, times(1)).setUpLocationHierarchies(input, TEST_ORG_ID);
    }

    private LocationHierarchyEntity createLocationHierarchy(String id) {
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setId(id);
        return locationHierarchyEntity;
    }

    private NetworkLane setupDomainWithSegmentEndFacility(Facility facility) {
        NetworkLaneSegment segment = new NetworkLaneSegment();
        segment.setSequence("1");
        segment.setEndFacility(facility);
        return setupDomainWithSegment(segment);
    }

    private NetworkLane setupDomainWithSegmentStartFacility(Facility facility) {
        NetworkLaneSegment segment = new NetworkLaneSegment();
        segment.setSequence("1");
        segment.setStartFacility(facility);
        return setupDomainWithSegment(segment);
    }

    private NetworkLane setupDomainWithSegment(NetworkLaneSegment segment) {
        NetworkLane domain = new NetworkLane();
        domain.setNetworkLaneSegments(List.of(segment));
        return domain;
    }

    private void executeDestinationTest(Facility facility, Address expectedAddress, Facility expectedFacility) {
        NetworkLane domain = setupDomainWithSegmentEndFacility(facility);
        helper.setupDestinationUsingNetworkLaneSegment(domain, entity);
        verifyAndAssert(expectedAddress, expectedFacility, domain.getDestination(), domain.getDestinationFacility());
    }

    private void executeOriginTest(Facility facility, Address expectedAddress, Facility expectedFacility) {
        NetworkLane domain = setupDomainWithSegmentStartFacility(facility);
        helper.setupOriginUsingNetworkLaneSegment(domain, entity);
        verifyAndAssert(expectedAddress, expectedFacility, domain.getOrigin(), domain.getOriginFacility());
    }

    private void verifyAndAssert(Address expectedAddress, Facility expectedFacility, Address actualAddress, Facility actualFacility) {
        if (expectedAddress != null) {
            verifyLocationHierarchyServiceCalledWithAddress(expectedAddress);
        } else {
            verifyLocationHierarchyServiceCalledWithFacility(expectedFacility);
        }
        assertThat(actualAddress).isEqualTo(expectedAddress);
        assertThat(actualFacility).isEqualTo(expectedFacility);
    }

    private LocationHierarchyEntity createStartLocationHierarchyWithFacility(String timezone, String facility) {

        LocationEntity facilityEntity = new LocationEntity();
        facilityEntity.setName(facility);
        facilityEntity.setTimezone(timezone);

        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setFacility(facilityEntity);
        return locationHierarchyEntity;
    }

    private LocationHierarchyEntity createLocationHierarchyWithCity(String timezone, String city) {
        LocationEntity cityEntity = new LocationEntity();
        cityEntity.setName(city);
        cityEntity.setTimezone(timezone);

        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setCity(cityEntity);
        return locationHierarchyEntity;
    }

}
