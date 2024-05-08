package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.Vehicle;
import com.quincus.shipment.api.helper.MilestoneCodeUtil;
import com.quincus.shipment.impl.helper.SegmentReferenceHolder;
import com.quincus.shipment.impl.helper.SegmentReferenceProvider;
import com.quincus.shipment.impl.helper.segment.SegmentUpdateChecker;
import com.quincus.shipment.impl.mapper.DriverMapper;
import com.quincus.shipment.impl.mapper.VehicleMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.SegmentLockoutTimePassedRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.SegmentLockoutTimePassedEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TupleDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ASSIGNMENT_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ASSIGNMENT_UPDATED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_ON_ROUTE;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DISPATCH_SCHEDULED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_PICKUP;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_DELIVERY_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_ORDER_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_PICKUP_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_ARRIVED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_DEPARTED;
import static com.quincus.shipment.api.constant.SegmentStatus.CANCELLED;
import static com.quincus.shipment.api.constant.SegmentStatus.COMPLETED;
import static com.quincus.shipment.api.constant.SegmentStatus.IN_PROGRESS;
import static com.quincus.shipment.impl.service.PackageJourneySegmentService.ZONED_DATE_TIME_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentServiceTest {
    private static final List<MilestoneCode> MILESTONE_CODES_WITH_SEGMENT_UPDATES = List.of(
            DSP_DELIVERY_ON_ROUTE, SHP_FLIGHT_DEPARTED, DSP_PICKUP_SUCCESSFUL
            , DSP_DELIVERY_SUCCESSFUL, SHP_FLIGHT_ARRIVED
            , OM_PICKUP_CANCELED, OM_DELIVERY_CANCELED, OM_ORDER_CANCELED);
    private static final String PICKUP_TIME_ZONE = "UTC+05:00";
    private static final String DROP_OFF_TIME_ZONE = "UTC-09:00";
    @InjectMocks
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private PackageJourneySegmentRepository packageJourneySegmentRepository;
    @Mock
    private SegmentLockoutTimePassedRepository segmentLockoutTimePassedRepository;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    @Mock
    private VehicleMapper vehicleMapper;
    @Mock
    private DriverMapper driverMapper;
    @Mock
    private LocationHierarchyService locationHierarchyService;
    @Mock
    private SegmentReferenceProvider segmentReferenceProvider;
    @Mock
    private AddressService addressService;
    @Mock
    private FacilityService facilityService;
    @Mock
    private PartnerService partnerService;
    @Mock
    private SegmentUpdateChecker segmentUpdateChecker;
    @Mock
    private SegmentReferenceHolder segmentReferenceHolder;

    private static Stream<Arguments> provideMilestoneEventStatusCodes() {
        return Stream.of(
                arguments(DSP_DELIVERY_ON_ROUTE, IN_PROGRESS),
                arguments(DSP_DELIVERY_SUCCESSFUL, SegmentStatus.COMPLETED),
                arguments(DSP_PICKUP_SUCCESSFUL, IN_PROGRESS),
                arguments(OM_PICKUP_CANCELED, SegmentStatus.CANCELLED),
                arguments(OM_DELIVERY_CANCELED, SegmentStatus.CANCELLED)
        );
    }

    private static Stream<Arguments> provideDriverAndVehicleReassignmentMilestoneCodes() {
        return MilestoneCodeUtil.MILESTONE_STATUS_CODE_FOR_UPDATE_DRIVER_VEHICLE_INFO.stream().map(Arguments::arguments);
    }

    private static PackageJourneySegment generateSegmentFromLockoutTime(String lockoutTime) {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(UUID.randomUUID().toString());
        segment.setTransportType(TransportType.AIR);
        segment.setLockOutTime(lockoutTime);
        return segment;
    }

    private static Stream<Arguments> provideSegmentsWithLockoutTime() {

        return Stream.of(
                arguments(generateSegmentFromLockoutTime(null), false),
                arguments(generateSegmentFromLockoutTime("2023-03-04 05:27:02 +0800"), true),
                arguments(generateSegmentFromLockoutTime("2199-03-04 05:27:02 +0800"), false)
        );
    }

    private static Stream<Arguments> provideStatusUpdatingMilestoneCodesAndExpectedStatus() {
        return Stream.of(
                Arguments.of(DSP_DELIVERY_ON_ROUTE, IN_PROGRESS),
                Arguments.of(SHP_FLIGHT_DEPARTED, IN_PROGRESS),
                Arguments.of(DSP_PICKUP_SUCCESSFUL, IN_PROGRESS),
                Arguments.of(DSP_DELIVERY_SUCCESSFUL, COMPLETED),
                Arguments.of(SHP_FLIGHT_ARRIVED, COMPLETED),
                Arguments.of(OM_PICKUP_CANCELED, CANCELLED),
                Arguments.of(OM_DELIVERY_CANCELED, CANCELLED),
                Arguments.of(OM_ORDER_CANCELED, CANCELLED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneEventStatusCodes")
    @DisplayName("GIVEN milestone event WHEN updateSegmentStatusByMilestoneEvent THEN return expected status")
    void returnExpectedStatusWhenUpdateSegmentStatusByMilestoneEvent(MilestoneCode milestoneEventStatusCode,
                                                                     SegmentStatus expectedStatus) {

        String segmentId = UUID.randomUUID().toString();

        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId(segmentId);
        entity.setStatus(SegmentStatus.PLANNED);

        when(packageJourneySegmentRepository.findById(segmentId)).thenReturn(Optional.of(entity));

        Milestone milestone = new Milestone();
        milestone.setSegmentId(segmentId);
        milestone.setMilestoneCode(milestoneEventStatusCode);

        packageJourneySegmentService.updateSegmentStatusByMilestoneEvent(milestone,
                UUID.randomUUID().toString());

        assertThat(entity.getStatus()).isEqualTo(expectedStatus);
        verify(packageJourneySegmentRepository, times(1)).findById(segmentId);
        verify(packageJourneySegmentRepository, times(1)).save(any(PackageJourneySegmentEntity.class));
    }

    @Test
    void givenAllMilestoneCodes_whenUpdateSegmentStatusByMilestoneEvent_returnTrueWhenMilestoneCodeHasStatusUpdate() {

        String segmentId = UUID.randomUUID().toString();
        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId(segmentId);
        entity.setStatus(SegmentStatus.PLANNED);

        when(packageJourneySegmentRepository.findById(segmentId)).thenReturn(Optional.of(entity));

        for (MilestoneCode milestoneEventStatusCode : MilestoneCode.values()) {
            Milestone milestone = new Milestone();
            milestone.setSegmentId(segmentId);
            milestone.setMilestoneCode(milestoneEventStatusCode);

            boolean hasUpdate = packageJourneySegmentService.updateSegmentStatusByMilestoneEvent(milestone, UUID.randomUUID().toString());
            if (MILESTONE_CODES_WITH_SEGMENT_UPDATES.contains(milestoneEventStatusCode)) {
                assertThat(hasUpdate).isTrue();
            } else {
                assertThat(hasUpdate).isFalse();
            }
        }
    }

    @Test
    void givenMilestoneCodeDspRouteOnPickup_whenUpdateSegmentStatusByMilestoneEvent_returnFalse() {

        String segmentId = UUID.randomUUID().toString();
        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId(segmentId);
        entity.setStatus(SegmentStatus.PLANNED);

        Milestone milestone = new Milestone();
        milestone.setSegmentId(segmentId);
        milestone.setMilestoneCode(MilestoneCode.DSP_ON_ROUTE_TO_PICKUP);

        boolean hasUpdate = packageJourneySegmentService.updateSegmentStatusByMilestoneEvent(milestone, UUID.randomUUID().toString());

        assertThat(hasUpdate).isFalse();
        verifyNoInteractions(packageJourneySegmentRepository);
    }

    @Test
    @DisplayName("GIVEN PackageJourneySegmentEntity WHEN setTimezones THEN segment entity should contain correct timezones")
    void testTimezones() {
        Order order = new Order();
        order.setPickupTimezone("UTC+05:00");
        order.setDeliveryTimezone("UTC-09:00");
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setTransportType(TransportType.GROUND);
        LocationHierarchyEntity startLocationHierarchy = new LocationHierarchyEntity();
        LocationEntity startCity = new LocationEntity();
        startCity.setTimezone("Asia/Gaza UTC+02:00");
        startLocationHierarchy.setCity(startCity);
        LocationHierarchyEntity endLocationHierarchy = new LocationHierarchyEntity();
        LocationEntity endCity = new LocationEntity();
        endCity.setTimezone("HST UTC-10:00");
        endLocationHierarchy.setCity(endCity);
        packageJourneySegmentEntity.setStartLocationHierarchy(startLocationHierarchy);
        packageJourneySegmentEntity.setEndLocationHierarchy(endLocationHierarchy);

        packageJourneySegmentEntity.setPickUpTime("2023-03-14 05:27:02 +0500");
        packageJourneySegmentEntity.setDropOffTime("2023-03-14 14:27:02 -0900");
        packageJourneySegmentEntity.setPickUpCommitTime("2023-03-14 17:27:02 -0800");
        packageJourneySegmentEntity.setDropOffCommitTime("2023-03-14 17:27:02 -0800");
        packageJourneySegmentEntity.setPickUpActualTime("2023-03-14 17:27:02 -0800");
        packageJourneySegmentEntity.setDropOffActualTime("2023-03-14 17:27:02 -0800");
        packageJourneySegmentEntity.setDepartureTime("2023-03-14 17:27:02 -0800");
        packageJourneySegmentEntity.setArrivalTime("2023-03-14 17:27:02 -0800");
        packageJourneySegmentEntity.setLockOutTime("2023-03-14 17:27:02 -0800");
        packageJourneySegmentEntity.setRecoveryTime("2023-03-14 17:27:02 -0800");

        packageJourneySegmentService.setTimezones(order.getPickupTimezone(), order.getDeliveryTimezone(), packageJourneySegmentEntity);

        //Timezone offset changes when DST in use
        assertThat(packageJourneySegmentEntity.getPickUpTimezone()).isIn("UTC+02:00", "UTC+03:00");
        assertThat(packageJourneySegmentEntity.getDropOffTimezone()).isEqualTo("UTC-10:00");
        assertThat(packageJourneySegmentEntity.getPickUpCommitTimezone()).isIn("UTC+02:00", "UTC+03:00");
        assertThat(packageJourneySegmentEntity.getDropOffCommitTimezone()).isEqualTo("UTC-10:00");
        assertThat(packageJourneySegmentEntity.getPickUpActualTimezone()).isIn("UTC+02:00", "UTC+03:00");
        assertThat(packageJourneySegmentEntity.getDropOffActualTimezone()).isEqualTo("UTC-10:00");

        packageJourneySegmentEntity.setTransportType(TransportType.AIR);
        packageJourneySegmentService.setTimezones(order.getPickupTimezone(), order.getDeliveryTimezone(), packageJourneySegmentEntity);

        assertThat(packageJourneySegmentEntity.getDepartureTimezone()).isIn("UTC+02:00", "UTC+03:00");
        assertThat(packageJourneySegmentEntity.getArrivalTimezone()).isEqualTo("UTC-10:00");
        assertThat(packageJourneySegmentEntity.getLockOutTimezone()).isIn("UTC+02:00", "UTC+03:00");
        assertThat(packageJourneySegmentEntity.getRecoveryTimezone()).isEqualTo("UTC-10:00");
    }

    @Test
    void getAllSegmentsFromShipments_shipmentListWithSegments_shouldReturnSegmentList() {
        List<Shipment> refShipmentList = new ArrayList<>();
        Shipment shipment = new Shipment();
        shipment.setId("SHIP-01");
        refShipmentList.add(shipment);
        List<Tuple> segObjDummy = createDummyPartialSegmentList(2);

        when(packageJourneySegmentRepository.findAllSegmentsFromAllShipmentIds(anyList())).thenReturn(segObjDummy);

        List<PackageJourneySegment> result = packageJourneySegmentService.getAllSegmentsFromShipments(refShipmentList);

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllSegmentsFromShipments_shipmentListEmptySegments_shouldReturnEmptyList() {
        List<Shipment> refShipmentList = new ArrayList<>();
        Shipment shipment = new Shipment();
        shipment.setId("SHIP-01");
        refShipmentList.add(shipment);
        List<Tuple> segObjDummy = Collections.emptyList();

        when(packageJourneySegmentRepository.findAllSegmentsFromAllShipmentIds(anyList())).thenReturn(segObjDummy);

        List<PackageJourneySegment> result = packageJourneySegmentService.getAllSegmentsFromShipments(refShipmentList);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllSegmentsFromShipments_emptyList_shouldReturnEmptyList() {
        assertThat(packageJourneySegmentService.getAllSegmentsFromShipments(Collections.emptyList())).isEmpty();
    }

    @Test
    void cacheLockoutTimePassedSegment_segmentArgument_shouldExecuteSave() {
        String segmentId = "SEGMENT-01";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);

        packageJourneySegmentService.cacheLockoutTimePassedSegment(segment);

        ArgumentCaptor<SegmentLockoutTimePassedEntity> argumentCaptor = ArgumentCaptor
                .forClass(SegmentLockoutTimePassedEntity.class);

        verify(segmentLockoutTimePassedRepository, times(1))
                .save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getSegmentId()).isEqualTo(segmentId);
    }

    @Test
    void unCacheLockoutTimePassedSegment_segmentIdArgument_shouldExecuteDelete() {
        String segmentId = "SEGMENT-01";

        packageJourneySegmentService.unCacheLockoutTimePassedSegment(segmentId);

        verify(segmentLockoutTimePassedRepository, times(1))
                .deleteBySegmentId(segmentId);
    }

    @Test
    void isSegmentAllFacilitiesAllowed_firstSegment_shouldCheckEndFacility() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Facility endFacility = new Facility();
        endFacility.setId("1");
        endFacility.setExternalId("x1");
        segment.setEndFacility(endFacility);

        when(facilityLocationPermissionChecker.isFacilityLocationCovered(endFacility)).thenReturn(true);
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(null)).thenReturn(false);
        assertThat(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(segment)).isTrue();
    }

    @Test
    void isSegmentAllFacilitiesAllowed_middleSegment_shouldCheckBothFacility() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Facility startFacility = new Facility();
        startFacility.setId("1");
        startFacility.setExternalId("x1");
        segment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        endFacility.setId("2");
        endFacility.setExternalId("x2");
        segment.setEndFacility(endFacility);

        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any(Facility.class)))
                .thenReturn(false).thenReturn(true);
        assertThat(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(segment)).isTrue();
    }

    @Test
    void isSegmentAllFacilitiesAllowed_lastSegment_shouldCheckStartFacility() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Facility startFacility = new Facility();
        startFacility.setId("1");
        startFacility.setExternalId("x1");
        segment.setStartFacility(startFacility);

        when(facilityLocationPermissionChecker.isFacilityLocationCovered(startFacility)).thenReturn(true);
        assertThat(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(segment)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideDriverAndVehicleReassignmentMilestoneCodes")
    void testUpdateSegmentWithDriverAndVehicleInfoFromMilestoneSuccess(MilestoneCode mileStoneForVehicleAndDriverAssignment) {
        //GIVEN:
        String orgId = "organization_id";
        String segmentId = "segment_id";

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(mileStoneForVehicleAndDriverAssignment);
        milestone.setSegmentId(segmentId);
        milestone.setOrganizationId(orgId);

        PackageJourneySegmentEntity mockEntity = mock(PackageJourneySegmentEntity.class);

        Vehicle vehicleData = createVehicleTestData();
        Driver driverData = createDriverTestData();
        when(driverMapper.milestoneToDriver(milestone)).thenReturn(driverData);
        when(vehicleMapper.milestoneToVehicle(milestone)).thenReturn(vehicleData);

        //WHEN:
        boolean hasUpdate = packageJourneySegmentService.updateSegmentDriverAndVehicleFromMilestone(mockEntity, milestone);
        //THEN:
        assertThat(hasUpdate).isTrue();
        verify(mockEntity, times(1)).setVehicle(vehicleData);
        verify(mockEntity, times(1)).setDriver(driverData);
    }

    @Test
    void testUpdateSegmentWithDriverAndVehicleInfoFromMilestoneShouldNotContinueWhenNonValidMilestoneCode() {
        //GIVEN:
        String orgId = "organization_id";
        String segmentId = "segment_id";

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_PICKUP_FAILED);
        milestone.setSegmentId(segmentId);
        milestone.setOrganizationId(orgId);

        PackageJourneySegmentEntity mockEntity = mock(PackageJourneySegmentEntity.class);

        //WHEN:
        boolean hasUpdate = packageJourneySegmentService.updateSegmentDriverAndVehicleFromMilestone(mockEntity, milestone);
        //THEN:
        assertThat(hasUpdate).isFalse();
        verifyNoInteractions(packageJourneySegmentRepository);
        verifyNoInteractions(vehicleMapper);
        verifyNoInteractions(driverMapper);

    }

    @ParameterizedTest
    @MethodSource("provideSegmentsWithLockoutTime")
    void isSegmentLockoutTimeMissed_variousLockout_shouldReturnBoolean(PackageJourneySegment segment, boolean expected) {
        boolean result = packageJourneySegmentService.isSegmentLockoutTimeMissed(segment);
        assertThat(result).isEqualTo(expected);
    }

    private List<Tuple> createDummyPartialSegmentList(int size) {
        List<Tuple> partialSegmentList = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            String segmentId = UUID.randomUUID().toString();
            String journeyId = UUID.randomUUID().toString();
            String refId = String.format("%s", size);
            String sequence = String.format("%s", size);
            String status = "PLANNED";
            String transportType = "AIR";
            String lockoutTime = "2023-03-14 20:27:02 -0800";
            String lockoutTimeTimezone = "UTC-08:00";

            Tuple segmentTuple = TupleDataFactory.ofSegmentsFromShipments(segmentId, journeyId, refId, sequence, status,
                    transportType, lockoutTime, lockoutTimeTimezone);
            partialSegmentList.add(segmentTuple);
        }

        return partialSegmentList;
    }

    @Test
    void testUpdateOnSiteTime_DriverPickUpStatus() {
        //GIVEN:
        String orgId = "organization_id";
        String segmentId = "segment_id";

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_PICKUP);
        milestone.setSegmentId(segmentId);
        milestone.setOrganizationId(orgId);
        milestone.setMilestoneTime(OffsetDateTime.of(2023, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC));

        PackageJourneySegmentEntity mockEntity = mock(PackageJourneySegmentEntity.class);

        //WHEN:
        boolean hasUpdate = packageJourneySegmentService.updateOnSiteTime(mockEntity, milestone);
        //THEN:
        assertThat(hasUpdate).isTrue();
        verify(mockEntity, times(1)).setPickUpOnSiteTime(any());
        verify(mockEntity, times(1)).setPickUpOnSiteTimezone(any());
    }

    @Test
    void testUpdateOnSiteTime_DriverDropOffStatus() {
        //GIVEN:
        String orgId = "organization_id";
        String segmentId = "segment_id";

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_DELIVERY);
        milestone.setSegmentId(segmentId);
        milestone.setOrganizationId(orgId);
        milestone.setMilestoneTime(OffsetDateTime.of(2023, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC));

        PackageJourneySegmentEntity mockEntity = mock(PackageJourneySegmentEntity.class);

        //WHEN:
        boolean hasUpdate = packageJourneySegmentService.updateOnSiteTime(mockEntity, milestone);
        //THEN:
        assertThat(hasUpdate).isTrue();
        verify(mockEntity, times(1)).setDropOffOnSiteTime(any());
        verify(mockEntity, times(1)).setDropOffOnSiteTimezone(any());
    }

    @Test
    void givenMilestoneWithNoCode_whenUpdateSegmentByMilestone_thenFalseResult() {
        String uuid = UUID.randomUUID().toString();
        Milestone milestone = new Milestone();
        milestone.setSegmentId("seg1");

        boolean result = packageJourneySegmentService.updateSegmentByMilestone(milestone, uuid, true);

        assertThat(result).isFalse();
    }

    @Test
    void givenMilestoneWithNoSegment_whenUpdateSegmentByMilestone_thenFalseResult() {
        String uuid = UUID.randomUUID().toString();
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);

        boolean result = packageJourneySegmentService.updateSegmentByMilestone(milestone, uuid, true);

        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("allMilestoneCodeWithUpdateToSegment")
    void givenMilestoneCodesThatHasUpdateToSegment_whenUpdateSegmentByMilestone_thenTrueResult(MilestoneCode milestoneCodesWithUpdates) {
        String uuid = UUID.randomUUID().toString();
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(milestoneCodesWithUpdates);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setSegmentId("seg1");


        if (EnumSet.of(DSP_DISPATCH_SCHEDULED, DSP_ASSIGNMENT_UPDATED, DSP_ASSIGNMENT_CANCELED)
                .contains(milestoneCodesWithUpdates)) {
            Driver driver = new Driver();
            driver.setId(UUID.randomUUID().toString());
            driver.setName("DriverNameUpdate");
            when(driverMapper.milestoneToDriver(any())).thenReturn(driver);

            Vehicle vehicle = new Vehicle();
            vehicle.setId(UUID.randomUUID().toString());
            vehicle.setType("VehicleTypeUpdate");
            when(vehicleMapper.milestoneToVehicle(any())).thenReturn(vehicle);
        }

        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org1");
        when(packageJourneySegmentRepository.findByIdAndOrganizationId("seg1", "org1")).thenReturn(Optional.of(entity));

        boolean result = packageJourneySegmentService.updateSegmentByMilestone(milestone, uuid, true);
        verify(packageJourneySegmentRepository, times(1)).save(any());

        assertThat(result).isTrue();
    }

    @Test
    void testUpdateActualTime_pickUpActualTime() {
        //GIVEN:
        String orgId = "organization_id";
        String segmentId = "segment_id";

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_PICKUP_SUCCESSFUL);
        milestone.setSegmentId(segmentId);
        milestone.setOrganizationId(orgId);
        milestone.setMilestoneTime(OffsetDateTime.of(2023, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC));

        PackageJourneySegmentEntity mockEntity = mock(PackageJourneySegmentEntity.class);

        //WHEN:
        assertThat(packageJourneySegmentService.updateActualTime(mockEntity, milestone)).isTrue();
        //THEN:
        verify(mockEntity, times(1)).setPickUpActualTime(any());
        verify(mockEntity, times(1)).setPickUpActualTimezone(any());

    }

    @Test
    void testUpdateActualTime_dropOffActualTime() {
        //GIVEN:
        String orgId = "organization_id";
        String segmentId = "segment_id";

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DELIVERY_SUCCESSFUL);
        milestone.setSegmentId(segmentId);
        milestone.setOrganizationId(orgId);
        milestone.setMilestoneTime(OffsetDateTime.of(2023, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC));

        PackageJourneySegmentEntity mockEntity = mock(PackageJourneySegmentEntity.class);

        //WHEN:
        assertThat(packageJourneySegmentService.updateActualTime(mockEntity, milestone)).isTrue();
        //THEN:
        verify(mockEntity, times(1)).setDropOffActualTime(any());
        verify(mockEntity, times(1)).setDropOffActualTimezone(any());
    }

    @ParameterizedTest
    @MethodSource("provideStatusUpdatingMilestoneCodesAndExpectedStatus")
    void updateSegmentStatusByMilestoneEvent_supportedMilestoneCode_shouldUpdateAndReturnTrue(MilestoneCode milestoneCode,
                                                                                              SegmentStatus segmentStatus) {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(milestoneCode);

        boolean result = packageJourneySegmentService.updateSegmentStatusByMilestone(segmentEntity, milestone);

        assertThat(segmentEntity.getStatus()).isEqualTo(segmentStatus);
        assertThat(result).isTrue();
    }

    @Test
    void updateSegmentStatusByMilestoneEvent_unsupportedMilestoneCode_shouldReturnFalse() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);

        assertThat(packageJourneySegmentService.updateSegmentStatusByMilestone(segmentEntity, milestone)).isFalse();
    }

    @Test
    void updateSegmentStatusByMilestoneEvent_existingStatus_shouldReturnFalse() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setStatus(IN_PROGRESS);
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DELIVERY_ON_ROUTE);

        assertThat(packageJourneySegmentService.updateSegmentStatusByMilestone(segmentEntity, milestone)).isFalse();
    }

    @Test
    void save_validSegmentEntity_shouldSaveAndReturnDomain() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentEntity");

        when(packageJourneySegmentRepository.save(any(PackageJourneySegmentEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        PackageJourneySegment segment = packageJourneySegmentService.save(segmentEntity);

        assertThat(segment).isNotNull();
        assertThat(segment.getSegmentId()).isEqualTo(segmentEntity.getId());

        verify(packageJourneySegmentRepository, times(1))
                .save(any(PackageJourneySegmentEntity.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverAndVehicleReassignmentMilestoneCodes")
    void updateSegmentDriverAndVehicleFromMilestone_milestoneCodeUpdatingDriver_shouldUpdateDriverAndVehicle(MilestoneCode milestoneCode) {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();

        Driver driver1 = new Driver();
        driver1.setId("driver1");
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setId("vehicle1");

        segmentEntity.setDriver(driver1);
        segmentEntity.setVehicle(vehicle1);

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(milestoneCode);

        Driver driver2 = new Driver();
        driver2.setId("driver2");
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId("vehicle2");

        when(driverMapper.milestoneToDriver(milestone)).thenReturn(driver2);
        when(vehicleMapper.milestoneToVehicle(milestone)).thenReturn(vehicle2);

        assertThat(packageJourneySegmentService.updateSegmentDriverAndVehicleFromMilestone(segmentEntity, milestone)).isTrue();

        assertThat(segmentEntity.getDriver()).isEqualTo(driver2);
        assertThat(segmentEntity.getVehicle()).isEqualTo(vehicle2);
    }

    @ParameterizedTest
    @MethodSource("provideDriverAndVehicleReassignmentMilestoneCodes")
    void updateSegmentDriverAndVehicleFromMilestone_repeatMilestoneCode_shouldReturnFalse(MilestoneCode milestoneCode) {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();

        Driver driver1 = new Driver();
        driver1.setId("driver1");
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setId("vehicle1");

        segmentEntity.setDriver(driver1);
        segmentEntity.setVehicle(vehicle1);

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(milestoneCode);

        Driver driver2 = new Driver();
        driver2.setId("driver1");
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId("vehicle1");

        when(driverMapper.milestoneToDriver(milestone)).thenReturn(driver2);
        when(vehicleMapper.milestoneToVehicle(milestone)).thenReturn(vehicle2);

        assertThat(packageJourneySegmentService.updateSegmentDriverAndVehicleFromMilestone(segmentEntity, milestone)).isFalse();

        assertThat(segmentEntity.getDriver()).isEqualTo(driver1);
        assertThat(segmentEntity.getVehicle()).isEqualTo(vehicle1);
    }

    @Test
    void updateSegmentDriverAndVehicleFromMilestone_driverCanceledMilestoneCode_shouldUpdateAndReturnTrue() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();

        Driver driver1 = new Driver();
        driver1.setId("driver1");
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setId("vehicle1");

        segmentEntity.setDriver(driver1);
        segmentEntity.setVehicle(vehicle1);

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_ASSIGNMENT_CANCELED);

        Driver driver2 = new Driver();
        Vehicle vehicle2 = new Vehicle();

        when(driverMapper.milestoneToDriver(milestone)).thenReturn(driver2);
        when(vehicleMapper.milestoneToVehicle(milestone)).thenReturn(vehicle2);

        assertThat(packageJourneySegmentService.updateSegmentDriverAndVehicleFromMilestone(segmentEntity, milestone)).isTrue();

        assertThat(segmentEntity.getDriver()).isEqualTo(driver2);
        assertThat(segmentEntity.getVehicle()).isEqualTo(vehicle2);
    }

    @Test
    void updateSegmentDriverAndVehicleFromMilestone_otherMilestoneCode_shouldReturnFalse() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);

        assertThat(packageJourneySegmentService.updateSegmentDriverAndVehicleFromMilestone(segmentEntity, milestone)).isFalse();

        assertThat(segmentEntity.getDriver()).isNull();
        assertThat(segmentEntity.getVehicle()).isNull();
    }

    @Test
    void updateOnSiteTimeFromMilestone_milestoneCodeUpdatingPickUpOnSiteTime_shouldUpdatePickUpOnSiteTime() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setPickUpTimezone("GMT+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_PICKUP);
        OffsetDateTime milestoneTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        milestone.setMilestoneTime(milestoneTime);

        assertThat(packageJourneySegmentService.updateOnSiteTimeFromMilestone(segmentEntity, milestone)).isTrue();

        assertThat(segmentEntity.getPickUpOnSiteTime()).isNotNull();
        assertThat(segmentEntity.getPickUpOnSiteTimezone()).isNotNull();
        assertThat(segmentEntity.getDropOffOnSiteTime()).isNull();
        assertThat(segmentEntity.getDropOffOnSiteTimezone()).isNull();
    }

    @Test
    void updateOnSiteTimeFromMilestone_milestoneCodeUpdatingPickUpOnSiteTimeRepeat_shouldReturnFalse() {
        OffsetDateTime refTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        ZoneId refZoneId = ZoneId.from(refTime);
        ZonedDateTime refZonedDateTime = refTime.atZoneSameInstant(refZoneId);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setPickUpTimezone("UTC+08:00");
        segmentEntity.setPickUpOnSiteTime(refZonedDateTime.format(ZONED_DATE_TIME_FORMAT));
        segmentEntity.setPickUpOnSiteTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_PICKUP);
        milestone.setMilestoneTime(refTime);

        assertThat(packageJourneySegmentService.updateOnSiteTimeFromMilestone(segmentEntity, milestone)).isFalse();
    }

    @Test
    void updateOnSiteTimeFromMilestone_milestoneCodeUpdatingDropOffOnSiteTime_shouldUpdateDropOffOnSiteTime() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setDropOffTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_DELIVERY);
        OffsetDateTime milestoneTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        milestone.setMilestoneTime(milestoneTime);

        assertThat(packageJourneySegmentService.updateOnSiteTimeFromMilestone(segmentEntity, milestone)).isTrue();

        assertThat(segmentEntity.getPickUpOnSiteTime()).isNull();
        assertThat(segmentEntity.getPickUpOnSiteTimezone()).isNull();
        assertThat(segmentEntity.getDropOffOnSiteTime()).isNotNull();
        assertThat(segmentEntity.getDropOffOnSiteTimezone()).isNotNull();
    }

    @Test
    void updateOnSiteTimeFromMilestone_milestoneCodeUpdatingDropOffOnSiteTimeRepeat_shouldReturnFalse() {
        OffsetDateTime refTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        ZoneId refZoneId = ZoneId.from(refTime);
        ZonedDateTime refZonedDateTime = refTime.atZoneSameInstant(refZoneId);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setDropOffTimezone("UTC+08:00");
        segmentEntity.setDropOffOnSiteTime(refZonedDateTime.format(ZONED_DATE_TIME_FORMAT));
        segmentEntity.setDropOffOnSiteTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_DELIVERY);
        milestone.setMilestoneTime(refTime);

        assertThat(packageJourneySegmentService.updateOnSiteTimeFromMilestone(segmentEntity, milestone)).isFalse();
    }

    @Test
    void updateOnSiteTimeFromMilestone_otherMilestoneCode_shouldReturnFalse() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setPickUpTimezone("UTC+08:00");
        segmentEntity.setDropOffTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        OffsetDateTime milestoneTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        milestone.setMilestoneTime(milestoneTime);

        assertThat(packageJourneySegmentService.updateOnSiteTimeFromMilestone(segmentEntity, milestone)).isFalse();

        assertThat(segmentEntity.getPickUpOnSiteTime()).isNull();
        assertThat(segmentEntity.getPickUpOnSiteTimezone()).isNull();
        assertThat(segmentEntity.getDropOffOnSiteTime()).isNull();
        assertThat(segmentEntity.getDropOffOnSiteTimezone()).isNull();
    }

    @Test
    void updateActualTimeFromMilestone_milestoneCodeUpdatingPickUpActualTime_shouldUpdatePickUpActualTime() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setPickUpTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_PICKUP_SUCCESSFUL);
        OffsetDateTime milestoneTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        milestone.setMilestoneTime(milestoneTime);

        assertThat(packageJourneySegmentService.updateActualTimeFromMilestone(segmentEntity, milestone)).isTrue();

        assertThat(segmentEntity.getPickUpActualTime()).isNotNull();
        assertThat(segmentEntity.getPickUpActualTimezone()).isNotNull();
        assertThat(segmentEntity.getDropOffActualTime()).isNull();
        assertThat(segmentEntity.getDropOffActualTimezone()).isNull();
    }

    @Test
    void updateActualTimeFromMilestone_milestoneCodeUpdatingPickUpActualTimeRepeat_shouldReturnFalse() {
        OffsetDateTime refTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        ZoneId refZoneId = ZoneId.from(refTime);
        ZonedDateTime refZonedDateTime = refTime.atZoneSameInstant(refZoneId);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setPickUpTimezone("UTC+08:00");
        segmentEntity.setPickUpActualTime(refZonedDateTime.format(ZONED_DATE_TIME_FORMAT));
        segmentEntity.setPickUpActualTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_PICKUP_SUCCESSFUL);
        milestone.setMilestoneTime(refTime);

        assertThat(packageJourneySegmentService.updateActualTimeFromMilestone(segmentEntity, milestone)).isFalse();
    }

    @Test
    void updateActualTimeFromMilestone_milestoneCodeUpdatingDropOffActualTime_shouldUpdateDropOffActualTime() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setDropOffTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DELIVERY_SUCCESSFUL);
        OffsetDateTime milestoneTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        milestone.setMilestoneTime(milestoneTime);

        assertThat(packageJourneySegmentService.updateActualTimeFromMilestone(segmentEntity, milestone)).isTrue();

        assertThat(segmentEntity.getPickUpActualTime()).isNull();
        assertThat(segmentEntity.getPickUpActualTimezone()).isNull();
        assertThat(segmentEntity.getDropOffActualTime()).isNotNull();
        assertThat(segmentEntity.getDropOffActualTimezone()).isNotNull();
    }

    @Test
    void updateActualTimeFromMilestone_milestoneCodeUpdatingDropOffActualTimeRepeat_shouldReturnFalse() {
        OffsetDateTime refTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        ZoneId refZoneId = ZoneId.from(refTime);
        ZonedDateTime refZonedDateTime = refTime.atZoneSameInstant(refZoneId);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setDropOffTimezone("UTC+08:00");
        segmentEntity.setDropOffActualTime(refZonedDateTime.format(ZONED_DATE_TIME_FORMAT));
        segmentEntity.setDropOffActualTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DELIVERY_SUCCESSFUL);
        milestone.setMilestoneTime(refTime);

        assertThat(packageJourneySegmentService.updateActualTimeFromMilestone(segmentEntity, milestone)).isFalse();
    }

    @Test
    void updateActualTimeFromMilestone_otherMilestoneCode_shouldReturnFalse() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setPickUpTimezone("UTC+08:00");
        segmentEntity.setDropOffTimezone("UTC+08:00");
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        OffsetDateTime milestoneTime = OffsetDateTime.of(LocalDateTime.now(Clock.systemUTC()), ZoneOffset.ofHours(8));
        milestone.setMilestoneTime(milestoneTime);

        assertThat(packageJourneySegmentService.updateActualTimeFromMilestone(segmentEntity, milestone)).isFalse();

        assertThat(segmentEntity.getPickUpActualTime()).isNull();
        assertThat(segmentEntity.getPickUpActualTimezone()).isNull();
        assertThat(segmentEntity.getDropOffActualTime()).isNull();
        assertThat(segmentEntity.getDropOffActualTimezone()).isNull();
    }

    @Test
    void partnerReferenceWasAlreadyFetch_whenUpsertFacilityAndPartner_useReferenceAndNoNewPartnersCreated() {
        //GIVEN:
        Partner partner = new Partner();
        partner.setId("testExtId");

        Facility startFacility = new Facility();
        startFacility.setExternalId("startFacilityExternalId");
        Address startAddress = new Address();
        startAddress.setCountryId("startCountryId");
        startAddress.setStateId("startStateId");
        startAddress.setCityId("startCityId");
        startFacility.setLocation(startAddress);

        Facility endFacility = new Facility();
        endFacility.setExternalId("endFacilityExternalId");
        Address endAddress = new Address();
        endAddress.setCountryId("endCountryId");
        endAddress.setStateId("endStateId");
        endAddress.setCityId("endCityId");
        endFacility.setLocation(endAddress);

        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setPartner(partner);
        packageJourneySegment.setRefId("1");
        packageJourneySegment.setSequence("1");
        packageJourneySegment.setStartFacility(startFacility);
        packageJourneySegment.setEndFacility(endFacility);

        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.addPackageJourneySegment(packageJourneySegment);

        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setShipmentJourney(shipmentJourney);
        shipmentDomain.setOrder(new Order());

        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("1");
        packageJourneySegmentEntity.setSequence("1");

        Map<String, PartnerEntity> mockPartnerReference = new HashMap<>();
        mockPartnerReference.put("testExtId", new PartnerEntity());

        LocationHierarchyEntity startLH = new LocationHierarchyEntity();
        LocationHierarchyEntity endLH = new LocationHierarchyEntity();
        Map<String, LocationHierarchyEntity> locationHierarchyReference = new HashMap<>();
        locationHierarchyReference.put("startCountryIdstartStateIdstartCityIdstartFacilityExternalId", startLH);
        locationHierarchyReference.put("endCountryIdendStateIdendCityIdendFacilityExternalId", endLH);

        SegmentReferenceHolder mockReference = mock(SegmentReferenceHolder.class);
        when(mockReference.getPartnerBySegmentId()).thenReturn(mockPartnerReference);
        when(mockReference.getLocationHierarchyByFacilityExtId()).thenReturn(locationHierarchyReference);
        when(segmentReferenceProvider.generateReference(any())).thenReturn(mockReference);
        when(segmentUpdateChecker.isSegmentMatch(packageJourneySegment, packageJourneySegmentEntity)).thenReturn(true);
        //WHEN:
        packageJourneySegmentService.updateFacilityAndPartner(List.of(packageJourneySegment), List.of(packageJourneySegmentEntity), PICKUP_TIME_ZONE, DROP_OFF_TIME_ZONE);
        //THEN:
        assertThat(packageJourneySegmentEntity.getPartner()).isNotNull();
        assertThat(packageJourneySegmentEntity.getStartLocationHierarchy()).isNotNull();
        assertThat(packageJourneySegmentEntity.getEndLocationHierarchy()).isNotNull();
        verifyNoMoreInteractions(partnerService);
        verifyNoMoreInteractions(locationHierarchyService);
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(startFacility);
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(endFacility);
        verify(addressService, times(1)).saveAddress(startFacility.getLocation(), startLH);
        verify(addressService, times(1)).saveAddress(endFacility.getLocation(), endLH);
    }

    @Test
    void partnerAndFacilityNoReferenceNotFound_whenUpsertFacilityAndPartner_useCreatedFromServiceAndPutInReference() {
        //GIVEN:
        Partner partner = new Partner();
        partner.setId("testExtId");

        Facility startFacility = new Facility();
        startFacility.setExternalId("startFacilityExternalId");
        Address startAddress = new Address();
        startAddress.setCountryId("startCountryId");
        startAddress.setStateId("startStateId");
        startAddress.setCityId("startCityId");
        startFacility.setLocation(startAddress);

        Facility endFacility = new Facility();
        endFacility.setExternalId("endFacilityExternalId");
        Address endAddress = new Address();
        endAddress.setCountryId("endCountryId");
        endAddress.setStateId("endStateId");
        endAddress.setCityId("endCityId");
        endFacility.setLocation(endAddress);

        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setPartner(partner);
        packageJourneySegment.setRefId("1");
        packageJourneySegment.setSequence("1");
        packageJourneySegment.setStartFacility(startFacility);
        packageJourneySegment.setEndFacility(endFacility);

        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("1");
        packageJourneySegmentEntity.setSequence("1");

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegmentEntity);

        Map<String, PartnerEntity> partnerReference = new HashMap<>();
        Map<String, LocationHierarchyEntity> locationHierarchyReference = new HashMap<>();

        SegmentReferenceHolder mockReference = mock(SegmentReferenceHolder.class);
        when(mockReference.getPartnerBySegmentId()).thenReturn(partnerReference);
        when(mockReference.getLocationHierarchyByFacilityExtId()).thenReturn(locationHierarchyReference);
        when(segmentReferenceProvider.generateReference(any())).thenReturn(mockReference);

        PartnerEntity createdPartnerEntity = new PartnerEntity();
        createdPartnerEntity.setExternalId("testPartnerId");
        when(partnerService.createAndSavePartnerFromQPortal(anyString())).thenReturn(createdPartnerEntity);

        LocationHierarchyEntity startLH = new LocationHierarchyEntity();
        LocationHierarchyEntity endLH = new LocationHierarchyEntity();
        when(segmentUpdateChecker.isSegmentMatch(packageJourneySegment, packageJourneySegmentEntity)).thenReturn(true);
        when(locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(startFacility)).thenReturn(startLH);
        when(locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(endFacility)).thenReturn(endLH);
        //WHEN:
        packageJourneySegmentService.updateFacilityAndPartner(List.of(packageJourneySegment), List.of(packageJourneySegmentEntity)
                , "UTC+05:00", "UTC-09:00");
        //THEN:
        assertThat(shipmentJourneyEntity.getPackageJourneySegments().get(0).getPartner()).isNotNull();
        assertThat(shipmentJourneyEntity.getPackageJourneySegments().get(0).getStartLocationHierarchy()).isNotNull();
        assertThat(shipmentJourneyEntity.getPackageJourneySegments().get(0).getEndLocationHierarchy()).isNotNull();
        assertThat(partnerReference).containsKey("testPartnerId");
        assertThat(locationHierarchyReference).containsKey("startCountryIdstartStateIdstartCityIdstartFacilityExternalId")
                .containsKey("endCountryIdendStateIdendCityIdendFacilityExternalId");
        verify(partnerService).createAndSavePartnerFromQPortal(anyString());
        verify(locationHierarchyService, times(1)).setUpLocationHierarchyFromFacilityAndSave(startFacility);
        verify(locationHierarchyService, times(1)).setUpLocationHierarchyFromFacilityAndSave(endFacility);
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(startFacility);
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(endFacility);
        verify(addressService, times(1)).saveAddress(startFacility.getLocation(), startLH);
        verify(addressService, times(1)).saveAddress(endFacility.getLocation(), endLH);
    }

    @Test
    void givenPackageJourneySegmentWithNullFacility_ShouldBeAbleToHandleAndNoError() {
        //GIVEN:
        Partner partner = new Partner();
        partner.setId("testExtId");

        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setPartner(partner);
        packageJourneySegment.setRefId("1");
        packageJourneySegment.setSequence("1");

        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("1");
        packageJourneySegmentEntity.setSequence("1");

        Map<String, PartnerEntity> mockPartnerReference = new HashMap<>();
        mockPartnerReference.put("testExtId", new PartnerEntity());

        LocationHierarchyEntity startLH = new LocationHierarchyEntity();
        LocationHierarchyEntity endLH = new LocationHierarchyEntity();
        Map<String, LocationHierarchyEntity> locationHierarchyReference = new HashMap<>();
        locationHierarchyReference.put("startFacilityExternalId", startLH);
        locationHierarchyReference.put("endFacilityExternalId", endLH);

        SegmentReferenceHolder mockReference = mock(SegmentReferenceHolder.class);
        when(segmentUpdateChecker.isSegmentMatch(packageJourneySegment, packageJourneySegmentEntity)).thenReturn(true);
        when(mockReference.getPartnerBySegmentId()).thenReturn(mockPartnerReference);
        when(mockReference.getLocationHierarchyByFacilityExtId()).thenReturn(locationHierarchyReference);
        when(segmentReferenceProvider.generateReference(any())).thenReturn(mockReference);
        //WHEN:
        packageJourneySegmentService.updateFacilityAndPartner(List.of(packageJourneySegment)
                , List.of(packageJourneySegmentEntity), PICKUP_TIME_ZONE, DROP_OFF_TIME_ZONE);
        //THEN:
        verifyNoMoreInteractions(partnerService);
        verifyNoMoreInteractions(locationHierarchyService);
        verifyNoMoreInteractions(addressService);
    }

    @Test
    void testFindSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull_ShouldInvokeRepositoryFindSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull() {
        String airlineCode = "airlineCode";
        String flightNumber = "flightNumber";
        String departureDate = "departureDate";
        String origin = "origin";
        String destination = "destination";

        packageJourneySegmentService.findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(airlineCode, flightNumber, departureDate, origin, destination);

        verify(packageJourneySegmentRepository, times(1)).findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(airlineCode, flightNumber, departureDate, origin, destination);
    }

    @Test
    void initializeLocationHierarchiesFromFacilities_validParams_shouldUpdateEntity() {
        PackageJourneySegmentEntity existingSegment = new PackageJourneySegmentEntity();
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        Partner newPartner = new Partner();
        newPartner.setId("partner-1");
        updatedSegment.setPartner(newPartner);
        Facility startFacility = new Facility();
        startFacility.setExternalId("facility-1");
        startFacility.setLocation(new Address());
        updatedSegment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        endFacility.setExternalId("facility-2");
        endFacility.setLocation(new Address());
        updatedSegment.setEndFacility(endFacility);

        LocationHierarchyEntity startFacilityEntity = new LocationHierarchyEntity();
        LocationHierarchyEntity endFacilityEntity = new LocationHierarchyEntity();
        PartnerEntity dummyPartner = new PartnerEntity();

        doReturn(new HashMap<String, PartnerEntity>()).when(segmentReferenceHolder).getPartnerBySegmentId();
        doReturn(new HashMap<String, LocationHierarchyEntity>()).when(segmentReferenceHolder).getLocationHierarchyByFacilityExtId();
        when(partnerService.createAndSavePartnerFromQPortal(any())).thenReturn(dummyPartner);
        when(locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(startFacility)).thenReturn(startFacilityEntity);
        when(locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(endFacility)).thenReturn(endFacilityEntity);
        doReturn("org-1").when(userDetailsProvider).getCurrentOrganizationId();

        PackageJourneySegmentEntity updatedEntity = packageJourneySegmentService.initializeLocationHierarchiesFromFacilities(segmentReferenceHolder, existingSegment, updatedSegment, "UTC+08:00", "UTC+09:00");
        assertThat(updatedEntity).isEqualTo(existingSegment);

        verify(partnerService, times(1)).createAndSavePartnerFromQPortal(any());
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(startFacility);
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(endFacility);
        verify(addressService, times(2)).saveAddress(any(Address.class), any(LocationHierarchyEntity.class));
        verify(facilityService, times(1)).setupFlightOriginAndDestination(updatedSegment, existingSegment);
    }

    @Test
    void initializeLocationHierarchiesFromFacilities_noSegmentEntity_shouldReturnNull() {
        assertThat(packageJourneySegmentService.initializeLocationHierarchiesFromFacilities(segmentReferenceHolder, null, new PackageJourneySegment(), "", "")).isNull();
    }

    @Test
    void initializeLocationHierarchiesFromFacilities_noRefSegment_shouldReturnEntity() {
        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        assertThat(packageJourneySegmentService.initializeLocationHierarchiesFromFacilities(segmentReferenceHolder, entity, null, "", "")).isEqualTo(entity);
    }

    @Test
    void initializeSegmentFacilitiesAndPartner_validParams_shouldUpdateEntity() {
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        Partner newPartner = new Partner();
        newPartner.setId("partner-1");
        updatedSegment.setPartner(newPartner);
        Facility startFacility = new Facility();
        startFacility.setExternalId("facility-1");
        startFacility.setLocation(new Address());
        updatedSegment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        endFacility.setExternalId("facility-2");
        endFacility.setLocation(new Address());
        updatedSegment.setEndFacility(endFacility);

        LocationHierarchyEntity startFacilityEntity = new LocationHierarchyEntity();
        LocationHierarchyEntity endFacilityEntity = new LocationHierarchyEntity();
        PartnerEntity dummyPartner = new PartnerEntity();

        doReturn(new HashMap<String, PartnerEntity>()).when(segmentReferenceHolder).getPartnerBySegmentId();
        doReturn(new HashMap<String, LocationHierarchyEntity>()).when(segmentReferenceHolder).getLocationHierarchyByFacilityExtId();
        when(partnerService.createAndSavePartnerFromQPortal(any())).thenReturn(dummyPartner);
        when(locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(startFacility)).thenReturn(startFacilityEntity);
        when(locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(endFacility)).thenReturn(endFacilityEntity);
        doReturn("org-1").when(userDetailsProvider).getCurrentOrganizationId();

        PackageJourneySegmentEntity updatedEntity = packageJourneySegmentService.initializeSegmentFacilitiesAndPartner(segmentReferenceHolder, updatedSegment, "UTC+08:00", "UTC+09:00");
        assertThat(updatedEntity).isNotNull();

        verify(partnerService, times(1)).createAndSavePartnerFromQPortal(any());
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(startFacility);
        verify(facilityService, times(1)).enrichFacilityWithLocationFromQPortal(endFacility);
        verify(addressService, times(2)).saveAddress(any(Address.class), any(LocationHierarchyEntity.class));
        verify(facilityService, times(1)).setupFlightOriginAndDestination(any(), any());
    }

    @Test
    void enrichSegmentWithJourneyInformation_validParam_shouldEnrich() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        String journeyId = "journey-id";
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId(journeyId);

        PackageJourneySegmentEntity enrichedSegment = packageJourneySegmentService.enrichSegmentWithJourneyInformation(segmentEntity, journeyEntity);
        assertThat(enrichedSegment.getShipmentJourney()).isEqualTo(journeyEntity);
        assertThat(enrichedSegment.getShipmentJourneyId()).isEqualTo(journeyId);
    }

    @Test
    void enrichSegmentWithJourneyInformation_noSegmentEntity_shouldReturnNull() {
        String journeyId = "journey-id";
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId(journeyId);

        assertThat(packageJourneySegmentService.enrichSegmentWithJourneyInformation(null, journeyEntity)).isNull();
    }

    @Test
    void enrichSegmentWithJourneyInformation_noJourneyEntity_shouldNotEnrich() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();

        PackageJourneySegmentEntity segmentEntity1 = packageJourneySegmentService.enrichSegmentWithJourneyInformation(segmentEntity, null);
        assertThat(segmentEntity1).isEqualTo(segmentEntity);
        assertThat(segmentEntity1.getShipmentJourney()).isNull();
        assertThat(segmentEntity1.getShipmentJourneyId()).isNull();
    }

    @Test
    void enrichSegmentWithOrderInformation_validParam_shouldEnrich() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        Order order = new Order();
        order.setOpsType("P2P");
        PackageJourneySegmentEntity enrichedSegment = packageJourneySegmentService.enrichSegmentWithOrderOpsTypeInformation(segmentEntity, order);
        assertThat(enrichedSegment).isNotNull();
        assertThat(enrichedSegment.getOpsType()).isEqualTo(order.getOpsType());
    }

    @Test
    void enrichSegmentWithOrderInformation_noSegmentEntity_shouldReturnNull() {
        Order order = new Order();
        order.setOpsType("P2P");
        PackageJourneySegmentEntity enrichedSegment = packageJourneySegmentService.enrichSegmentWithOrderOpsTypeInformation(null, order);
        assertThat(enrichedSegment).isNull();
    }

    @Test
    void enrichSegmentWithOrderInformation_noOrder_shouldNotEnrich() {
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        PackageJourneySegmentEntity enrichedSegment = packageJourneySegmentService.enrichSegmentWithOrderOpsTypeInformation(segmentEntity, null);
        assertThat(enrichedSegment).isNotNull();
        assertThat(enrichedSegment.getOpsType()).isNull();
    }

    private static Stream<Arguments> provideSegmentsWithRefId() {
        return Stream.of(
                Arguments.of(
                        createSegments("1", "2", "3"),
                        List.of("1", "2", "3")
                ),
                Arguments.of(
                        createSegments(null, null, null),
                        List.of("0", "1", "2")
                ),
                Arguments.of(
                        createSegments("1", "2", null),
                        List.of("1", "2", "3")
                ),
                Arguments.of(
                        createSegments("0", "3", null),
                        List.of("0", "3", "4")
                ),
                Arguments.of(
                        createSegments("0", null, "2", "3", null, "5"),
                        List.of("0", "6", "2", "3", "7", "5")
                ),
                Arguments.of(
                        createSegments("a", "2", "3"),
                        List.of("a", "2", "3")
                ),
                Arguments.of(
                        createSegments("a", null, null, "c", null),
                        List.of("a", "0", "1", "c", "2")
                ),
                Arguments.of(
                        createSegments("9", null, "7"),
                        List.of("9", "10", "7")
                ),
                Arguments.of(
                        createSegments(null, "a", "2", "b", null),
                        List.of("3", "a", "2", "b", "4")
                ),
                Arguments.of(
                        createSegments("a", null, "b", null, "c"),
                        List.of("a", "0", "b", "1", "c")
                ),
                Arguments.of(
                        createSegments("1", null, "2"),
                        List.of("1", "3", "2")
                ),
                Arguments.of(
                        createSegments("1", "b", "4", "c", null, "3"),
                        List.of("1", "b", "4", "c", "5", "3")
                ),
                Arguments.of(
                        createSegments("1", "", "", "", null, "3"),
                        List.of("1", "4", "5", "6", "7", "3")
                )
        );
    }

    private static List<PackageJourneySegment> createSegments(String... refIds) {
        return Stream.of(refIds)
                .map(refId -> {
                    PackageJourneySegment segment = new PackageJourneySegment();
                    segment.setRefId(refId);
                    return segment;
                })
                .toList();
    }

    private static Stream<Arguments> allMilestoneCodeWithUpdateToSegment() {
        return Stream.of(
                arguments(DSP_DELIVERY_ON_ROUTE)
                , arguments(SHP_FLIGHT_DEPARTED)
                , arguments(DSP_PICKUP_SUCCESSFUL)
                , arguments(DSP_DELIVERY_SUCCESSFUL)
                , arguments(SHP_FLIGHT_ARRIVED)
                , arguments(OM_PICKUP_CANCELED)
                , arguments(OM_DELIVERY_CANCELED)
                , arguments(OM_ORDER_CANCELED)
                , arguments(DSP_DISPATCH_SCHEDULED)
                , arguments(DSP_ASSIGNMENT_UPDATED)
                , arguments(DSP_ASSIGNMENT_CANCELED)
                , arguments(DSP_DRIVER_ARRIVED_FOR_PICKUP)
                , arguments(DSP_DRIVER_ARRIVED_FOR_DELIVERY)
        );
    }

    private Vehicle createVehicleTestData() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("vehicleId");
        vehicle.setName("vehicleName");
        vehicle.setNumber("vehicleNumber");
        vehicle.setType("vehicleType");
        return vehicle;
    }

    private Driver createDriverTestData() {
        Driver driver = new Driver();
        driver.setId("driverId");
        driver.setName("driverName");
        driver.setPhoneCode("driverPhoneCode");
        driver.setPhoneNumber("driverPhoneNumber");
        return driver;
    }
}