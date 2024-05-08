package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.ResponseCode;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.MilestoneLookup;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.UserLocation;
import com.quincus.shipment.api.exception.IllegalMilestoneException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.impl.config.ShipmentProperties;
import com.quincus.shipment.impl.helper.MilestoneHubLocationHandler;
import com.quincus.shipment.impl.helper.MilestoneTimezoneHelper;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.repository.MilestoneRepository;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.test_utils.TupleDataFactory;
import com.quincus.web.common.exception.model.ObjectNotFoundException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.exception.model.ResourceMismatchException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.persistence.Tuple;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DISPATCH_SCHEDULED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ON_ROUTE_TO_PICKUP;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_CONSOLIDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class MilestoneServiceTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @Spy
    private final MilestoneMapper milestoneMapper = testUtil.getMilestoneMapper();
    @Spy
    private final ObjectMapper objectMapper = testUtil.getObjectMapper();
    @InjectMocks
    private MilestoneService milestoneService;
    @Mock
    private MilestoneRepository milestoneRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private PackageJourneySegmentRepository segmentRepository;
    @Mock
    private Validator validator;
    @Mock
    private QPortalService qPortalService;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    @Mock
    private MessageApi messageApi;
    @Mock
    private ConstraintViolation<Object> mockViolation1;
    @Mock
    private ConstraintViolation<Object> mockViolation2;
    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private MilestoneHubLocationHandler milestoneHubLocationHandler;
    @Mock
    private ShipmentProperties shipmentProperties;
    @Mock
    private AlertService alertService;
    @Mock
    private MilestoneTimezoneHelper milestoneTimezoneHelper;

    private static Stream<Arguments> provideRecentStatusCodes() {
        return Stream.of(
                arguments(false, List.of(DSP_PICKUP_FAILED, DSP_PICKUP_FAILED, DSP_PICKUP_FAILED)),
                arguments(false, List.of(DSP_DELIVERY_FAILED, DSP_DELIVERY_FAILED, DSP_DELIVERY_FAILED)),
                arguments(true, List.of(OM_BOOKED, DSP_PICKUP_FAILED, DSP_PICKUP_FAILED)),
                arguments(true, List.of(DSP_DELIVERY_FAILED, DSP_DELIVERY_FAILED, DSP_PICKUP_FAILED)),
                arguments(true, List.of(DSP_DELIVERY_FAILED, DSP_DELIVERY_FAILED)),
                arguments(true, Collections.emptyList())
        );
    }

    private static Stream<Arguments> provideStatusCode() {
        return Stream.of(
                arguments(true, DSP_PICKUP_FAILED),
                arguments(true, DSP_DELIVERY_FAILED),
                arguments(false, DSP_DISPATCH_SCHEDULED),
                arguments(false, DSP_ON_ROUTE_TO_PICKUP),
                arguments(false, DSP_PICKUP_SUCCESSFUL),
                arguments(false, null)
        );
    }

    private static Stream<Arguments> provideMilestoneCodeAndIsHubActivity() {
        Map<MilestoneCode, Boolean> milestoneHubActivitiesMap =
                Arrays.stream(MilestoneCode.values()).collect(Collectors.toMap(Function.identity(), k -> false));
        milestoneHubActivitiesMap.put(SHP_CONSOLIDATED, true);
        milestoneHubActivitiesMap.put(MilestoneCode.SHP_DECONSOLIDATED, true);
        milestoneHubActivitiesMap.put(MilestoneCode.SHP_DIMS_WEIGHT_UPDATED, true);
        milestoneHubActivitiesMap.put(MilestoneCode.SHP_SORTED_IN_HUB, true);
        return milestoneHubActivitiesMap.entrySet().stream().map(e -> arguments(e.getKey(), e.getValue()));
    }

    private static Stream<Arguments> provideFlightEvents() {
        return Stream.of(
                arguments(FlightEventName.FLIGHT_LANDED, "Flight Arrived", MilestoneCode.SHP_FLIGHT_ARRIVED),
                arguments(FlightEventName.FLIGHT_DEPARTED, "Flight Departed", MilestoneCode.SHP_FLIGHT_DEPARTED)
        );
    }

    private Tuple createDummyMilestone(MilestoneCode code) {
        return TupleDataFactory.ofMilestone(code, "2023-03-03T04:25:17.757Z");
    }

    private Tuple createDummyCurrentMilestone(MilestoneCode code) {
        return TupleDataFactory.ofMilestone(code, OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @ParameterizedTest
    @MethodSource("provideRecentStatusCodes")
    @DisplayName("GIVEN last three milestone events WHEN isRetryableToDispatch THEN return expected")
    void returnExpectedWhenIsRetryableToDispatch(boolean expected, List<MilestoneCode> codes) {
        List<Tuple> milestoneEvents = new ArrayList<>();
        for (MilestoneCode code : codes) {
            milestoneEvents.add(createDummyMilestone(code));
        }
        when(milestoneRepository.findByShipmentId(anyString(), any()))
                .thenReturn(new PageImpl<>(milestoneEvents));
        boolean result = milestoneService.isRetryableToDispatch("001");

        assertThat(result).isEqualTo(expected);
        verify(milestoneRepository, times(1))
                .findByShipmentId("001", PageRequest.of(
                        0,
                        3,
                        Sort.by(ShipmentEntity_.MODIFY_TIME).descending()
                ));
    }

    @ParameterizedTest
    @MethodSource("provideStatusCode")
    @DisplayName("GIVEN statusCode WHEN isFailedStatusCode THEN return expected")
    void returnExpectedWhenIsFailedStatusCode(boolean expected, MilestoneCode code) {
        assertThat(milestoneService.isFailedStatusCode(code)).isEqualTo(expected);
    }

    @Test
    void createMilestone_milestoneArguments_shouldReturnMilestoneDomain() {
        Milestone dummyMilestone = new Milestone();
        dummyMilestone.setMilestoneCode(OM_BOOKED);

        MilestoneEntity savedEntity = new MilestoneEntity();
        savedEntity.setId("1");
        when(milestoneMapper.toEntity(dummyMilestone)).thenReturn(savedEntity);
        when(milestoneRepository.save(savedEntity)).thenReturn(savedEntity);

        milestoneService.createMilestone(dummyMilestone);

        assertThat(dummyMilestone.getId()).isEqualTo("1");
        verify(milestoneRepository, times(1)).save(any(MilestoneEntity.class));
    }

    @Test
    void createMilestoneShipmentCreated_validArguments_shouldCreateMilestone() {
        Shipment shipment = new Shipment();
        Organization org = new Organization("org1");
        Order order = new Order();
        order.setStatus(Root.STATUS_CREATED);
        order.setTimeCreated("2023-08-17T15:18:19.016Z");
        shipment.setOrder(order);
        shipment.setOrganization(org);
        shipment.setUserId("user1");
        shipment.setPartnerId("partner1");
        shipment.setPickUpLocation("fac1-id1");
        shipment.setDeliveryLocation("fac2-id1");
        Address origin = new Address();
        origin.setCountryId("country1-1");
        origin.setStateId("state1-1");
        origin.setCityId("city1-1");
        shipment.setOrigin(origin);
        Address destination = new Address();
        destination.setCountryId("country2-1");
        destination.setStateId("state2-1");
        destination.setCityId("city2-1");
        shipment.setDestination(destination);
        String userFullName = "User Full Name";

        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userDetailsProvider.getCurrentUserFullName()).thenReturn(userFullName);

        Milestone milestone = milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(shipment, OM_BOOKED);

        verify(milestoneRepository, times(1)).save(any(MilestoneEntity.class));
        verify(milestoneHubLocationHandler, times(1)).configureMilestoneHubWithUserHubInfo(any());
        assertThat(milestone.getMilestoneCode()).isEqualTo(OM_BOOKED);
        assertThat(milestone.getMilestoneTime()).isNotNull();
        assertThat(milestone.getMilestoneTime()).hasToString("2023-08-17T15:18:19Z");
        assertThat(milestone.getUserName()).isEqualTo(userFullName);
        assertThat(milestone.getUserId()).isEqualTo(shipment.getUserId());
        assertThat(milestone.getPartnerId()).isEqualTo(shipment.getPartnerId());
        assertThat(milestone.getFromLocationId()).isEqualTo(shipment.getPickUpLocation());

        Address resOrigin = shipment.getOrigin();
        assertThat(milestone.getFromCountryId()).withFailMessage("(From)Country ID mismatch").isEqualTo(resOrigin.getCountryId());
        assertThat(milestone.getFromStateId()).withFailMessage("(From)State ID mismatch").isEqualTo(resOrigin.getStateId());
        assertThat(milestone.getFromCityId()).withFailMessage("(From)City ID mismatch").isEqualTo(resOrigin.getCityId());

        Address resDest = shipment.getDestination();
        assertThat(milestone.getToCountryId()).withFailMessage("(To)Country ID mismatch").isEqualTo(resDest.getCountryId());
        assertThat(milestone.getToStateId()).withFailMessage("(To)State ID mismatch").isEqualTo(resDest.getStateId());
        assertThat(milestone.getToCityId()).withFailMessage("(To)City ID mismatch").isEqualTo(resDest.getCityId());

        assertThat(shipment.getMilestone().getMilestoneCode()).isEqualTo(OM_BOOKED);
    }

    @Test
    void initializeDispatchMilestone_validArguments_shouldReturnMilestoneObject() {
        JsonNode data = testUtil.getDataFromFile("samplepayload/dispatchmodule-milestone-event.json");
        String dspMilestoneStr = data.toString();
        MilestoneLookup dummyQportalMilestone = new MilestoneLookup();
        dummyQportalMilestone.setCode("1502");
        dummyQportalMilestone.setName("Pickup Failed");
        when(qPortalService.listMilestones()).thenReturn(List.of(dummyQportalMilestone));
        assertThatNoException().isThrownBy(() -> milestoneService.initializeDispatchMilestone(dspMilestoneStr, ""));
    }

    @Test
    void getDispatchMessageJson_validArguments_shouldReturnJsonObject() {
        JsonNode data = testUtil.getDataFromFile("samplepayload/dispatchmodule-milestone-event.json");
        String dspMilestoneStr = data.toString();
        JsonNode dspMilestone = milestoneService.getDispatchMessageJson(dspMilestoneStr);
        assertThat(dspMilestone).isNotEmpty();
    }

    @Test
    void getDispatchMessageJson_nullArgument_shouldReturnJsonObject() {
        JsonNode dspMilestone = milestoneService.getDispatchMessageJson(null);
        assertThat(dspMilestone).isEmpty();
    }

    @Test
    void validateDispatchMilestone_validMilestone_shouldNotThrowException() {
        Milestone dspMilestone = new Milestone();
        dspMilestone.setOrganizationId("org1");
        dspMilestone.setShipmentId("shp1");
        dspMilestone.setShipmentTrackingId("shpTracking1");
        dspMilestone.setSegmentId("seg1");
        Set<ConstraintViolation<Milestone>> violations = Collections.emptySet();
        PackageJourneySegmentEntity existingSegment = new PackageJourneySegmentEntity();
        existingSegment.setId("trueSegId");
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(validator.validate(dspMilestone)).thenReturn(violations);
        when(segmentRepository.findByIdAndOrganizationIdAndShipmentId("seg1", "organizationId", "shp1"))
                .thenReturn(Optional.of(existingSegment));

        assertThatNoException().isThrownBy(() -> milestoneService.validateDispatchMilestone(dspMilestone, ""));
    }

    @Test
    void validateDispatchMilestone_validationFailed_shouldThrowConstraintViolationException() {
        Milestone dspMilestone = new Milestone();
        dspMilestone.setOrganizationId("org1");
        dspMilestone.setShipmentId("shp1");
        dspMilestone.setSegmentId("seg1");
        Set<ConstraintViolation<Milestone>> violations = new HashSet<>();
        violations.add(mock(ConstraintViolation.class));
        when(validator.validate(dspMilestone)).thenReturn(violations);

        assertThatThrownBy(() -> milestoneService.validateDispatchMilestone(dspMilestone, ""))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validateDispatchMilestone_segmentNotFound_shouldThrowIllegalMilestoneException() {
        Milestone dspMilestone = new Milestone();
        dspMilestone.setOrganizationId("org1");
        dspMilestone.setShipmentId("shp1");
        dspMilestone.setShipmentTrackingId("shpTracking1");
        dspMilestone.setSegmentId("seg1");
        Set<ConstraintViolation<Milestone>> violations = Collections.emptySet();
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(validator.validate(dspMilestone)).thenReturn(violations);
        when(segmentRepository.findByIdAndOrganizationIdAndShipmentId("seg1", "organizationId", "shp1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> milestoneService.validateDispatchMilestone(dspMilestone, ""))
                .isInstanceOf(IllegalMilestoneException.class);
    }

    @Test
    void convertAndValidateMilestoneFromDispatch_validArguments_shouldCreateMilestoneAndSetShipmentId() {
        String ORG_ID = "organizationId";
        JsonNode data = testUtil.getDataFromFile("samplepayload/dispatchmodule-milestone-event.json");
        String dspMilestoneStr = data.toString();
        Set<ConstraintViolation<Milestone>> violations = Collections.emptySet();
        PackageJourneySegmentEntity existingSegment = new PackageJourneySegmentEntity();
        existingSegment.setId("pjsId");
        MilestoneLookup dummyQportalMilestone = new MilestoneLookup();
        dummyQportalMilestone.setCode("1502");
        dummyQportalMilestone.setName("Pickup Failed");
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(qPortalService.listMilestones()).thenReturn(List.of(dummyQportalMilestone));
        when(validator.validate(any(Milestone.class))).thenReturn(violations);
        when(segmentRepository.findByIdAndOrganizationIdAndShipmentId(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(existingSegment));
        Milestone milestone = milestoneService.convertAndValidateMilestoneFromDispatch(dspMilestoneStr, "");
        assertThat(milestone.getShipmentId()).isNotNull().isEqualTo("4028819785a13d1a0185a13dddfe0021");
    }

    @Test
    void getLatestMilestone_emptyMilestone_shouldReturnNull() {
        assertThat(milestoneService.getLatestMilestone(Collections.emptySet())).isNull();
    }

    @Test
    void getLatestMilestone_milestoneSet_shouldReturnLatestMilestone() {
        MilestoneEntity milestone1 = new MilestoneEntity();
        milestone1.setMilestoneRefId("milestone-1");
        milestone1.setMilestoneName("Order Booked");
        milestone1.setMilestoneCode(OM_BOOKED);
        milestone1.setCreateTime(Instant.now(Clock.systemUTC()).minusSeconds(1000));
        milestone1.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).minusSeconds(1000).toString());
        MilestoneEntity milestone2 = new MilestoneEntity();
        milestone2.setMilestoneRefId("milestone-2");
        milestone2.setMilestoneName("Pickup Successful");
        milestone2.setMilestoneCode(DSP_PICKUP_SUCCESSFUL);
        milestone2.setCreateTime(Instant.now());
        milestone2.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).toString());
        MilestoneEntity milestone3 = new MilestoneEntity();
        milestone3.setMilestoneRefId("milestone-3");
        milestone3.setMilestoneName("Pickup In Progress");
        milestone3.setMilestoneCode(DSP_ON_ROUTE_TO_PICKUP);
        milestone3.setCreateTime(Instant.now().minusSeconds(500));
        milestone3.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).minusSeconds(500).toString());
        Set<MilestoneEntity> milestoneEntities = Set.of(milestone1, milestone2, milestone3);
        ShipmentEntity refShipment = new ShipmentEntity();
        refShipment.setMilestoneEvents(milestoneEntities);

        MilestoneLookup dummyMilestone1 = new MilestoneLookup();
        dummyMilestone1.setCode(OM_BOOKED.toString());
        dummyMilestone1.setName("Booked");
        MilestoneLookup dummyMilestone2 = new MilestoneLookup();
        dummyMilestone2.setCode(DSP_PICKUP_SUCCESSFUL.toString());
        dummyMilestone2.setName("Pick Up Successful");
        MilestoneLookup dummyMilestone3 = new MilestoneLookup();
        dummyMilestone3.setCode(DSP_ON_ROUTE_TO_PICKUP.toString());
        dummyMilestone3.setName("Pick Up In Progress");

        Milestone actualMilestone = milestoneService.getLatestMilestone(refShipment.getMilestoneEvents());
        assertThat(actualMilestone.getMilestoneCode()).isEqualTo(DSP_PICKUP_SUCCESSFUL);
        assertThat(actualMilestone.getMilestoneRefId()).isEqualTo(milestone2.getMilestoneRefId());
        assertThat(actualMilestone.getMilestoneName()).isEqualTo(milestone2.getMilestoneName());
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneCodeAndIsHubActivity")
    void isMilestoneHubActivity(MilestoneCode actualCode, boolean expectedResult) {
        boolean actualResult = milestoneService.isMilestoneHubActivity(actualCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void getMilestoneEventsForShipment_validArguments_shouldAddMilestone() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        milestoneEntity.setMilestoneCode(OM_BOOKED);
        milestoneEntity.setMilestoneTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        milestoneEntity.setMilestoneName("Order Booked");
        shipmentEntity.setMilestoneEvents(Set.of(milestoneEntity));
        Shipment shipment = new Shipment();
        milestoneService.setMilestoneEventsForShipment(shipmentEntity, shipment);
        assertThat(shipment.getMilestone()).withFailMessage("Latest Milestone not set").isNotNull();
        assertThat(shipment.getMilestoneEvents()).withFailMessage("Milestone Events not logged").isNotEmpty();
    }

    @Test
    void setMostRecentMilestone_shipmentContainsMultipleMilestones_shouldAddMostRecent() {
        Shipment shipment = new Shipment();
        Milestone milestone1 = new Milestone();
        milestone1.setCreateTime(Instant.now(Clock.systemUTC()).minusSeconds(1000));
        milestone1.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).minusSeconds(1000));
        milestone1.setMilestoneCode(OM_BOOKED);
        Milestone milestone2 = new Milestone();
        milestone2.setCreateTime(Instant.now(Clock.systemUTC()));
        milestone2.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone2.setMilestoneCode(DSP_ON_ROUTE_TO_PICKUP);
        shipment.setMilestoneEvents(List.of(milestone1, milestone2));
        milestoneService.setMostRecentMilestone(shipment);
        assertThat(milestone2.getMilestoneCode()).isEqualTo(shipment.getMilestone().getMilestoneCode());
    }

    @Test
    void classifyMilestoneEvents_validArguments_shouldClassifyCorrectly() {
        OffsetDateTime refTime = OffsetDateTime.now();
        Instant refInstant = Instant.now();
        Shipment shipment = new Shipment();
        shipment.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment segment = new PackageJourneySegment();
        String segmentId = "segment1";
        segment.setSegmentId(segmentId);
        String partnerId = "partner1";
        Partner partner = new Partner();
        partner.setId(partnerId);
        String facility1Id = "fac1";
        Facility facility1 = new Facility();
        facility1.setExternalId(facility1Id);
        String facility2Id = "fac2";
        Facility facility2 = new Facility();
        facility2.setExternalId(facility2Id);
        segment.setPartner(partner);
        segment.setStartFacility(facility1);
        segment.setEndFacility(facility2);
        shipment.getShipmentJourney().setPackageJourneySegments(List.of(segment));
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        MilestoneEntity milestoneEntity1 = new MilestoneEntity();
        milestoneEntity1.setSegment(segmentEntity);
        milestoneEntity1.setMilestoneCode(OM_BOOKED);
        milestoneEntity1.setCreateTime(refInstant.minusSeconds(120));
        milestoneEntity1.setMilestoneTime(refTime.minusHours(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        milestoneEntity1.setMilestoneName("Order Booked");
        MilestoneEntity milestoneEntity2 = new MilestoneEntity();
        milestoneEntity2.setSegment(segmentEntity);
        milestoneEntity2.setHubId(facility2Id);
        milestoneEntity2.setMilestoneCode(SHP_CONSOLIDATED);
        milestoneEntity2.setCreateTime(refInstant.minusSeconds(60));
        milestoneEntity2.setMilestoneTime(refTime.minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        milestoneEntity2.setMilestoneName("Consolidated");
        MilestoneEntity milestoneEntity3 = new MilestoneEntity();
        milestoneEntity3.setSegment(segmentEntity);
        milestoneEntity3.setPartnerId(partnerId);
        milestoneEntity3.setHubId(facility2Id);
        milestoneEntity3.setMilestoneCode(DSP_DISPATCH_SCHEDULED);
        milestoneEntity3.setCreateTime(refInstant);
        milestoneEntity3.setMilestoneTime(refTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        milestoneEntity3.setMilestoneName("Dispatch Scheduled");
        shipmentEntity.setMilestoneEvents(Set.of(milestoneEntity1, milestoneEntity2, milestoneEntity3));
        milestoneService.setMilestoneEventsForShipment(shipmentEntity, shipment);

        assertThat(segment.getPartner().getVendorEvents()).withFailMessage("Vendor Events does not contain milestone").hasSize(1);
        assertThat(segment.getEndFacility().getHubEvents()).withFailMessage("Hub Events does not contain milestone").hasSize(1);
        assertThat(segment.getStartFacility().getHubEvents()).withFailMessage("Hub Events contain milestone").isEmpty();
        assertThat(segment.getPartner().getVendorEvents().get(0).getMilestoneCode()).withFailMessage("Vendor Event not correctly set").isEqualTo(DSP_DISPATCH_SCHEDULED);
        assertThat(segment.getEndFacility().getHubEvents().get(0).getMilestoneCode()).withFailMessage("Hub Event not correctly set").isEqualTo(SHP_CONSOLIDATED);
    }

    @ParameterizedTest
    @MethodSource("provideFlightEvents")
    @DisplayName("when createMilestoneFromFlightEvent then return expected milestone")
    void returnExpectedWhenCreateMilestoneFromFlightEvent(FlightEventName eventName, String expectedMilestoneName, MilestoneCode expectedCode) {
        Flight flightEvent = new Flight();
        flightEvent.setEventName(eventName);
        FlightStatus flightStatus = new FlightStatus();
        flightStatus.setEventName(eventName);
        String airportName = "NAIA Terminal 3";
        String actualTime = LocalDateTime.now().toString();
        FlightDetails arrival = new FlightDetails();
        arrival.setAirportName(airportName);
        arrival.setActualTime(actualTime);
        FlightDetails departure = new FlightDetails();
        departure.setAirportName(airportName);
        departure.setActualTime(actualTime);
        flightStatus.setArrival(arrival);
        flightStatus.setDeparture(departure);
        flightEvent.setFlightStatus(flightStatus);
        String dummyJourneyId = "dummy_journey_id";
        OrganizationEntity dummyOrganization = new OrganizationEntity();
        dummyOrganization.setId("org_id_001");
        ShipmentMessageDto dummyShipment = new ShipmentMessageDto();
        dummyShipment.setId("shipment_id_001");
        dummyShipment.setUserId("user_id_001");
        dummyShipment.setOrganizationId(dummyOrganization.getId());
        PackageJourneySegment dummySegment = new PackageJourneySegment();
        dummySegment.setSegmentId("segment_id_001");
        dummySegment.setJourneyId(dummyJourneyId);
        dummySegment.setStartFacility(createDummyFacility("startFacilityId", "startFacilityCountry", "startFacilityState", "startFacilityCity"));
        dummySegment.setEndFacility(createDummyFacility("endFacilityId", "endFacilityCountry", "endFacilityState", "endFacilityCity"));
        MilestoneLookup milestoneLookup1 = new MilestoneLookup();
        milestoneLookup1.setId("milestone1-id");
        milestoneLookup1.setCode(MilestoneCode.SHP_FLIGHT_DEPARTED.toString());
        MilestoneLookup milestoneLookup2 = new MilestoneLookup();
        milestoneLookup2.setId("milestone2-id");
        milestoneLookup2.setCode(MilestoneCode.SHP_FLIGHT_ARRIVED.toString());
        doReturn(List.of(milestoneLookup1, milestoneLookup2)).when(qPortalService).listMilestonesByOrganizationId(dummyOrganization.getId());
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        Milestone result = milestoneService.createMilestoneFromFlightEvent(dummyShipment, dummySegment, flightEvent);
        verify(milestoneRepository, times(1)).save(any(MilestoneEntity.class));
        verify(milestoneTimezoneHelper, times(1)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(any(Milestone.class), any(PackageJourneySegment.class));

        assertThat(result.getHubId()).isNotNull();
        assertThat(result.getHubCityId()).isNotNull();
        assertThat(result.getHubCountryId()).isNotNull();
        assertThat(result.getHubStateId()).isNotNull();
        assertThat(result.getMilestoneCode()).isEqualTo(expectedCode);
        assertThat(result.getMilestoneRefId()).isNotNull();
        assertThat(result.getMilestoneName()).isEqualTo(expectedMilestoneName);
    }

    @Test
    void testMilestoneUpdateWithFoundId() {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setExternalOrderId("ext-order-1");
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        shipmentEntity.setShipmentJourney(journey);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any())).thenReturn(true);

        Milestone domain = new Milestone();
        domain.setExternalOrderId("ext-order-1");
        domain.setSegmentId("0");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenReturn(milestoneEntity);
        when(milestoneMapper.toDomain(any(MilestoneEntity.class))).thenReturn(domain);
        when(milestoneRepository.isAllRelatedShipmentSameMilestone(any(), any(), any())).thenReturn(1);

        assertThat(milestoneService.partialUpdate(domain).getHubId()).isEqualTo("changedHubid");
        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
        verify(messageApi, times(1)).sendMilestoneMessage(any(Milestone.class), any(Shipment.class));
        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
        verify(milestoneHubLocationHandler, times(2)).configureMilestoneHubWithUserHubInfo(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(any(Milestone.class), any(PackageJourneySegmentEntity.class));
    }

    @Test
    void testMilestoneUpdateWithExternalOrderIdMatchesOrderIdLabel() {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        shipmentEntity.setShipmentJourney(journey);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderIdLabel("ORDER-LABEL-ABC");
        shipmentEntity.setOrder(orderEntity);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any())).thenReturn(true);

        Milestone domain = new Milestone();
        domain.setExternalOrderId("ORDER-LABEL-ABC");
        domain.setSegmentId("0");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenReturn(milestoneEntity);
        when(milestoneMapper.toDomain(any(MilestoneEntity.class))).thenReturn(domain);
        when(milestoneRepository.isAllRelatedShipmentSameMilestone(any(), any(), any())).thenReturn(1);
        assertThat(milestoneService.partialUpdate(domain).getHubId()).isEqualTo("changedHubid");
        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
        verify(messageApi, times(1)).sendMilestoneMessage(any(Milestone.class), any(Shipment.class));
        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
        verify(milestoneHubLocationHandler, times(2)).configureMilestoneHubWithUserHubInfo(any(Milestone.class));
    }

    @Test
    void testMilestoneUpdateLocationNotAllowed() {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        shipmentEntity.setShipmentJourney(journey);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any())).thenReturn(false);

        Milestone domain = new Milestone();
        domain.setSegmentId("0");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");

        assertThatThrownBy(() -> milestoneService.partialUpdate(domain)).isInstanceOf(SegmentLocationNotAllowedException.class);
    }

    @Test
    void testMilestoneUpdateShipmentNotFound() {
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.empty());
        Milestone domain = new Milestone();
        domain.setSegmentId("test");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");
        assertThatThrownBy(() -> milestoneService.partialUpdate(domain)).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void testMilestoneUpdateCodeAndIdNotFound() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        shipmentEntity.setShipmentJourney(journey);
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        Milestone domain = new Milestone();
        domain.setSegmentId("test");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");
        assertThatThrownBy(() -> milestoneService.partialUpdate(domain)).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void testMilestoneUpdateExternalOrderIdBlank() {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        shipmentEntity.setShipmentJourney(journey);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any())).thenReturn(true);

        Milestone domain = new Milestone();
        domain.setSegmentId("0");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");

        assertThatThrownBy(() -> milestoneService.partialUpdate(domain))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("`external_order_id` must not be blank.");
    }

    @Test
    void testMilestoneUpdateExternalOrderIdMismatchAgainstExternalOrderId() {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        shipmentEntity.setShipmentJourney(journey);
        shipmentEntity.setExternalOrderId("shipment-ext-order-id-1");
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any())).thenReturn(true);

        Milestone domain = new Milestone();
        domain.setSegmentId("0");
        domain.setExternalOrderId("milestone-ext-order-id-1");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");

        assertThatThrownBy(() -> milestoneService.partialUpdate(domain))
                .isInstanceOf(ResourceMismatchException.class);
    }

    @Test
    void testMilestoneUpdateExternalOrderIdMismatchAgainstOrderIdLabel() {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        shipmentEntity.setShipmentJourney(journey);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderIdLabel("ORDER-ID-LABEL-ABC");
        shipmentEntity.setOrder(orderEntity);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any())).thenReturn(true);

        Milestone domain = new Milestone();
        domain.setSegmentId("0");
        domain.setExternalOrderId("ORDER-ID-LABEL-123");
        domain.setExternalOrderId("milestone-ext-order-id-1");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");

        assertThatThrownBy(() -> milestoneService.partialUpdate(domain))
                .isInstanceOf(ResourceMismatchException.class);
    }

    @Test
    void partialUpdate_someShipmentHaveMilestone() {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        shipmentEntity.setShipmentJourney(journey);
        shipmentEntity.setExternalOrderId("ext-order-id-1");
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isFacilityLocationCovered(any())).thenReturn(true);

        Milestone domain = new Milestone();
        domain.setExternalOrderId("ext-order-id-1");
        domain.setSegmentId("0");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenReturn(milestoneEntity);
        when(milestoneMapper.toDomain(any(MilestoneEntity.class))).thenReturn(domain);
        when(milestoneRepository.isAllRelatedShipmentSameMilestone(any(), any(), any())).thenReturn(0);
        assertThat(milestoneService.partialUpdate(domain).getHubId()).isEqualTo("changedHubid");
        verify(messageApi, never()).sendMilestoneMessage(any(Milestone.class), any(Shipment.class));
        verify(messageApi, never()).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
        verify(milestoneHubLocationHandler, never()).enrichMilestoneHubIdWithLocationIds(any(Milestone.class));
        verify(milestoneHubLocationHandler, times(1)).configureMilestoneHubWithUserHubInfo(any(Milestone.class));
    }

    @Test
    void testFindRecentMilestoneByShipmentIdsMilestoneFound() {
        String shpId = "shipment1";
        String milestoneId = "milestone1";
        String milestoneName = "Milestone";
        String tupleVals = String.format("%s,%s,%s", milestoneId, shpId, milestoneName);
        List<Object[]> objects = new ArrayList<>();
        objects.add(tupleVals.split(","));
        when(milestoneRepository.findRecentMilestoneByShipmentIds(List.of(shpId))).thenReturn(objects);

        List<MilestoneEntity> result = milestoneService.findRecentMilestoneByShipmentIds(List.of(shpId));
        assertThat(result).hasSize(1);

        MilestoneEntity milestoneEntity = result.get(0);
        assertThat(milestoneEntity.getId()).isEqualTo(milestoneId);
        assertThat(milestoneEntity.getShipmentId()).isEqualTo(shpId);
        assertThat(milestoneEntity.getMilestoneName()).isEqualTo(milestoneName);
    }

    @Test
    void testFindRecentMilestoneByShipmentIdsNoResults() {
        String shpId = "shipment1";
        when(milestoneRepository.findRecentMilestoneByShipmentIds(List.of(shpId))).thenReturn(Collections.emptyList());

        List<MilestoneEntity> result = milestoneService.findRecentMilestoneByShipmentIds(List.of(shpId));
        assertThat(result).isEmpty();
    }

    @Test
    void testMilestoneUpdateIdNotFound() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("0");
        shipmentEntity.setShipmentJourney(journey);
        journey.addPackageJourneySegment(packageJourneySegmentEntity);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("org-id-1");
        when(shipmentRepository.findById(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(any(MilestoneCode.class), anyString(), any(PackageJourneySegmentEntity.class))).thenReturn(Optional.empty());
        Milestone domain = new Milestone();
        domain.setSegmentId("0");
        domain.setMilestoneCode(OM_BOOKED);
        domain.setHubId("changedHubid");
        domain.setShipmentId("shipmentid");
        assertThatThrownBy(() -> milestoneService.partialUpdate(domain)).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void saveMilestoneFromAPIG_validArguments_shouldSaveMilestone() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/updateMilestoneAPIG.json");
        MilestoneUpdateRequest milestoneUpdateRequest = objectMapper.readValue(data.toString(), MilestoneUpdateRequest.class);
        Set<ConstraintViolation<Object>> violations = Collections.emptySet();
        MilestoneLookup dummyQportalMilestone = new MilestoneLookup();
        dummyQportalMilestone.setCode("1607");
        dummyQportalMilestone.setName("Driver arrived for pick up");
        ShipmentEntity shipmentEntity1 = new ShipmentEntity();
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity1 = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity1.setId("segmentId1-0");
        packageJourneySegmentEntity1.setRefId("0");
        PackageJourneySegmentEntity packageJourneySegmentEntity2 = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity2.setId("segmentId2-0");
        packageJourneySegmentEntity2.setRefId("1");
        shipmentJourneyEntity.addAllPackageJourneySegments(List.of(packageJourneySegmentEntity1, packageJourneySegmentEntity2));
        shipmentEntity1.setId("shipmentId1");
        shipmentEntity1.setShipmentJourney(shipmentJourneyEntity);

        ShipmentEntity shipmentEntity2 = new ShipmentEntity();
        shipmentEntity2.setId("shipmentId2");
        shipmentEntity2.setShipmentJourney(shipmentJourneyEntity);
        List<ShipmentEntity> shipmentEntityList = List.of(shipmentEntity1, shipmentEntity2);

        Tuple milestoneEntity = createDummyMilestone(OM_BOOKED);

        when(milestoneRepository.findByShipmentId(anyString(), any())).thenReturn(new PageImpl<>(List.of(milestoneEntity)));
        when(shipmentRepository.findByOrderNumberAndOrganizationId(anyString(), anyString())).thenReturn(shipmentEntityList);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(qPortalService.listMilestones()).thenReturn(List.of(dummyQportalMilestone));
        when(validator.validate(any())).thenReturn(violations);
        when(messageApi.sendMilestoneMessage(any(Shipment.class), any(TriggeredFrom.class))).thenReturn("This is a test Milestone Message sent to Kafka Milestone topic");
        MilestoneEntity savedEntity = new MilestoneEntity();
        savedEntity.setId("1");
        when(milestoneMapper.toEntity(any())).thenReturn(savedEntity);
        when(milestoneRepository.save(savedEntity)).thenReturn(savedEntity);

        MilestoneUpdateResponse response = milestoneService.saveMilestoneFromAPIG(milestoneUpdateRequest);

        verify(milestoneRepository, times(2)).save(any(MilestoneEntity.class));
        verify(milestoneHubLocationHandler, times(2)).configureMilestoneHubWithUserHubInfo(any());
        verify(milestoneTimezoneHelper, times(2)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(2)).supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(any(Milestone.class), any(PackageJourneySegmentEntity.class));
        assertThat(response.getResponseCode()).isEqualTo(ResponseCode.SCC0000);
    }

    @Test
    void testFindMilestoneFromSegmentShouldCallRepositoryMethod() {
        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);

        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);

        assertThatNoException()
                .isThrownBy(() -> milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment));

        verify(milestoneRepository, times(1))
                .findByMilestoneCodeAndShipmentIdAndSegmentId(OM_BOOKED, shipmentId, segmentId);
    }

    @Test
    void testValidate_NoViolations() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/updateMilestoneAPIG.json");
        MilestoneUpdateRequest milestoneUpdateRequest = objectMapper.readValue(data.toString(), MilestoneUpdateRequest.class);

        assertThatNoException().isThrownBy(() -> milestoneService.validate(milestoneUpdateRequest));
    }

    @Test
    void testUpdateMilestoneTime_ValidMilestoneTime_Success() {
        // Given
        String milestoneId = UUID.randomUUID().toString();
        String validMilestoneTime = "2023-01-01T12:00:00Z";
        MilestoneUpdateTimeRequest milestoneUpdateTimeRequest = new MilestoneUpdateTimeRequest();
        milestoneUpdateTimeRequest.setId(milestoneId);
        milestoneUpdateTimeRequest.setMilestoneTime(validMilestoneTime);

        MilestoneEntity milestoneEntity = new MilestoneEntity();
        milestoneEntity.setId(milestoneId);

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestoneEntity));
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenReturn(milestoneEntity);
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(milestoneEntity.getShipmentId())).thenReturn(true);

        // When
        Milestone updatedMilestone = milestoneService.updateMilestoneTime(milestoneUpdateTimeRequest);

        // Then
        assertThat(updatedMilestone).isNotNull();
        assertThat(updatedMilestone.getMilestoneTime()).isEqualTo(validMilestoneTime);
    }

    @Test
    void testUpdateMilestoneTime_InvalidMilestoneTime_ThrowsQuincusValidationException() {
        // Given
        String milestoneId = "milestone1";
        String invalidMilestoneTime = "3033-01-02T12:00:00Z";
        MilestoneUpdateTimeRequest milestoneUpdateTimeRequest = new MilestoneUpdateTimeRequest();
        milestoneUpdateTimeRequest.setId(milestoneId);
        milestoneUpdateTimeRequest.setMilestoneTime(invalidMilestoneTime);

        // When and Then
        assertThatThrownBy(() -> milestoneService.updateMilestoneTime(milestoneUpdateTimeRequest))
                .isInstanceOf(QuincusValidationException.class);
    }

    @Test
    void createMilestoneFromOpsUpdate_ValidOpsUpdateRequest_CreatesAndReturnsNewMilestone() {
        // Given
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shipmentId");
        shipmentEntity.setPartnerId("partnerId");
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(UUID.randomUUID().toString());
        shipmentEntity.setOrganization(organization);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegment = new PackageJourneySegmentEntity();
        packageJourneySegment.setStatus(SegmentStatus.IN_PROGRESS);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID().toString());
        driver.setPhoneCode("+63");
        driver.setPhoneNumber("9779112212");
        packageJourneySegment.setDriver(driver);
        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setExternalId(UUID.randomUUID().toString());
        packageJourneySegment.setPartner(partnerEntity);
        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegment);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);

        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneTime("2023-05-28T12:30:00+00:00"); // Use an actual OffsetDateTime string here
        infoRequest.setMilestoneCode("1001");
        infoRequest.setMilestoneName("someName");
        infoRequest.setNotes("The customer has informed us that he is only available during the weekdays.");
        HostedFile genericAttachment = new HostedFile(UUID.randomUUID().toString(), "file.pdf", "https://example.com", null, 100L, null);
        HostedFile fileAttachment = new HostedFile(UUID.randomUUID().toString(), "file.jpg", "https://example.com", null, 100L, null);
        infoRequest.setAttachments(List.of(fileAttachment, genericAttachment));
        UserLocation userLocation = new UserLocation();
        userLocation.setLocationId("userLocationId");
        userLocation.setLocationFacilityName("testFacility");
        infoRequest.setUsersLocation(userLocation);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");

        when(userDetailsProvider.getCurrentUserFullName()).thenReturn("fullName");
        when(userDetailsProvider.getCurrentUserId()).thenReturn("userId");

        MilestoneEntity newMilestoneEntity = new MilestoneEntity();
        newMilestoneEntity.setId("milestoneId");
        MilestoneAdditionalInfo milestoneAdditionalInfo = new MilestoneAdditionalInfo();
        milestoneAdditionalInfo.setRemarks(infoRequest.getNotes());
        milestoneAdditionalInfo.setAttachments(infoRequest.getAttachments());
        newMilestoneEntity.setAdditionalInfo(milestoneAdditionalInfo);
        when(milestoneRepository.saveAndFlush(any(MilestoneEntity.class))).thenReturn(newMilestoneEntity);

        String shipmentBaseUrl = "http://localhost:8080";
        when(shipmentProperties.getBaseUrl()).thenReturn(shipmentBaseUrl);

        String readPreSignedPath = "/files/read-pre-signed-link";
        when(shipmentProperties.getReadPreSignedPath()).thenReturn(readPreSignedPath);

        // When
        Milestone result = milestoneService.createMilestoneFromOpsUpdate(shipmentEntity, packageJourneySegment, infoRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(newMilestoneEntity.getId());
        assertThat(result.getShipmentId()).isEqualTo(shipmentEntity.getId());
        assertThat(result.getOrganizationId()).isEqualTo(organization.getId());
        assertThat(result.getPartnerId()).isEqualTo(packageJourneySegment.getPartner().getExternalId());
        assertThat(result.getDriverId()).isEqualTo(packageJourneySegment.getDriver().getId());
        assertThat(result.getDriverPhoneCode()).isEqualTo(packageJourneySegment.getDriver().getPhoneCode());
        assertThat(result.getDriverPhoneNumber()).isEqualTo(packageJourneySegment.getDriver().getPhoneNumber());
        assertThat(result.getMilestoneName()).isEqualTo(infoRequest.getMilestoneName());
        assertThat(result.getMilestoneCode().name()).isEqualTo("OM_DRAFT");
        assertThat(result.getHubId()).isEqualTo(userLocation.getLocationId());
        assertThat(result.getAdditionalInfo().getAttachments()).hasSize(2);
        assertThat(result.getAdditionalInfo().getAttachments()).contains(genericAttachment).contains(fileAttachment);
        assertThat(result.getAdditionalInfo().getRemarks()).isEqualTo(infoRequest.getNotes());

        HostedFile file = result.getAdditionalInfo().getAttachments().get(0);
        assertThat(file.getFileUrl()).isEqualTo(shipmentBaseUrl + readPreSignedPath + "?file_name=file.jpg&directory=shipment_attachments");
        assertThat(file.getDirectFileUrl()).isEqualTo("https://example.com");
        assertThat(file.getFileTimestamp()).isEqualTo(result.getMilestoneTime().toString());

        verify(milestoneRepository, times(1)).saveAndFlush(any(MilestoneEntity.class));
        verify(milestoneHubLocationHandler, times(1)).enrichMilestoneHubIdWithLocationIds(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(any(Milestone.class), any(PackageJourneySegmentEntity.class));
    }

    @Test
    void testUpdateMilestoneTime_SegmentLocationNotCovered_ThrowsSegmentLocationNotAllowedException() {
        // Given
        String milestoneId = UUID.randomUUID().toString();
        String milestoneTime = "2023-01-01T12:00:00Z";
        MilestoneUpdateTimeRequest milestoneUpdateTimeRequest = new MilestoneUpdateTimeRequest();
        milestoneUpdateTimeRequest.setId(milestoneId);
        milestoneUpdateTimeRequest.setMilestoneTime(milestoneTime);

        MilestoneEntity milestoneEntity = new MilestoneEntity();
        milestoneEntity.setId(milestoneId);
        milestoneEntity.setShipmentId("shipmentId");

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestoneEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(milestoneEntity.getShipmentId())).thenReturn(false);

        // When and Then
        assertThatThrownBy(() -> milestoneService.updateMilestoneTime(milestoneUpdateTimeRequest))
                .isInstanceOf(SegmentLocationNotAllowedException.class)
                .hasMessageContaining("Updating the milestone on shipment shipmentId is not permitted because one of the segments does not have location coverage access");

        verify(milestoneRepository, never()).save(any());
    }

    @Test
    void saveMilestoneFromAPIG_validArguments_shouldCallUpdateSegmentStatusByMilestoneEvent() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/updateMilestoneAPIG.json");
        MilestoneUpdateRequest milestoneUpdateRequest = objectMapper.readValue(data.toString(), MilestoneUpdateRequest.class);
        milestoneUpdateRequest.setMilestone("1405");
        Set<ConstraintViolation<Object>> violations = Collections.emptySet();
        MilestoneLookup dummyQportalMilestone = new MilestoneLookup();
        dummyQportalMilestone.setCode("1405");
        dummyQportalMilestone.setName("Pickup Successful");
        ShipmentEntity shipmentEntity1 = new ShipmentEntity();
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity1 = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity1.setId("segmentId1-0");
        packageJourneySegmentEntity1.setRefId("0");
        PackageJourneySegmentEntity packageJourneySegmentEntity2 = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity2.setId("segmentId2-0");
        packageJourneySegmentEntity2.setRefId("1");
        shipmentJourneyEntity.addAllPackageJourneySegments(List.of(packageJourneySegmentEntity1, packageJourneySegmentEntity2));
        shipmentEntity1.setId("shipmentId1");
        shipmentEntity1.setShipmentJourney(shipmentJourneyEntity);

        ShipmentEntity shipmentEntity2 = new ShipmentEntity();
        shipmentEntity2.setId("shipmentId2");
        shipmentEntity2.setShipmentJourney(shipmentJourneyEntity);
        List<ShipmentEntity> shipmentEntityList = List.of(shipmentEntity1, shipmentEntity2);

        when(shipmentRepository.findByOrderNumberAndOrganizationId(anyString(), anyString())).thenReturn(shipmentEntityList);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(qPortalService.listMilestones()).thenReturn(List.of(dummyQportalMilestone));
        when(validator.validate(any())).thenReturn(violations);
        when(messageApi.sendMilestoneMessage(any(Shipment.class), any(TriggeredFrom.class))).thenReturn("This is a test Milestone Message sent to Kafka Milestone topic");
        when(packageJourneySegmentService.updateSegmentStatusByMilestoneEvent(any(Milestone.class), eq(null))).thenReturn(true);
        MilestoneEntity savedEntity = new MilestoneEntity();
        savedEntity.setId("1");
        when(milestoneMapper.toEntity(any())).thenReturn(savedEntity);
        when(milestoneRepository.save(savedEntity)).thenReturn(savedEntity);

        Tuple milestoneEntity = createDummyMilestone(OM_BOOKED);
        when(milestoneRepository.findByShipmentId(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(milestoneEntity)));

        MilestoneUpdateResponse response = milestoneService.saveMilestoneFromAPIG(milestoneUpdateRequest);

        assertThat(response.getResponseCode()).isEqualTo(ResponseCode.SCC0000);

        verify(milestoneRepository, times(2)).save(any(MilestoneEntity.class));
        verify(milestoneHubLocationHandler, times(2)).configureMilestoneHubWithUserHubInfo(any());
        verify(packageJourneySegmentService, times(2)).updateSegmentStatusByMilestoneEvent(any(Milestone.class), eq(null));
        verify(messageApi, times(2)).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
    }

    @Test
    void saveMilestoneFromAPIG_lateMilestone_shouldNotCallUpdateSegmentStatus() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/updateMilestoneAPIG.json");
        MilestoneUpdateRequest milestoneUpdateRequest = objectMapper.readValue(data.toString(), MilestoneUpdateRequest.class);
        milestoneUpdateRequest.setMilestone("1405");
        Set<ConstraintViolation<Object>> violations = Collections.emptySet();
        MilestoneLookup dummyQportalMilestone = new MilestoneLookup();
        dummyQportalMilestone.setCode("1405");
        dummyQportalMilestone.setName("Pickup Successful");
        ShipmentEntity shipmentEntity1 = new ShipmentEntity();
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegmentEntity1 = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity1.setId("segmentId1-0");
        packageJourneySegmentEntity1.setRefId("0");
        PackageJourneySegmentEntity packageJourneySegmentEntity2 = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity2.setId("segmentId2-0");
        packageJourneySegmentEntity2.setRefId("1");
        shipmentJourneyEntity.addAllPackageJourneySegments(List.of(packageJourneySegmentEntity1, packageJourneySegmentEntity2));
        shipmentEntity1.setId("shipmentId1");
        shipmentEntity1.setShipmentJourney(shipmentJourneyEntity);

        ShipmentEntity shipmentEntity2 = new ShipmentEntity();
        shipmentEntity2.setId("shipmentId2");
        shipmentEntity2.setShipmentJourney(shipmentJourneyEntity);
        List<ShipmentEntity> shipmentEntityList = List.of(shipmentEntity1, shipmentEntity2);

        when(shipmentRepository.findByOrderNumberAndOrganizationId(anyString(), anyString())).thenReturn(shipmentEntityList);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(qPortalService.listMilestones()).thenReturn(List.of(dummyQportalMilestone));
        when(validator.validate(any())).thenReturn(violations);

        Tuple milestoneEntity = createDummyCurrentMilestone(OM_BOOKED);
        when(milestoneRepository.findByShipmentId(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(milestoneEntity)));
        MilestoneEntity savedEntity = new MilestoneEntity();
        savedEntity.setId("1");
        when(milestoneMapper.toEntity(any())).thenReturn(savedEntity);
        when(milestoneRepository.save(savedEntity)).thenReturn(savedEntity);

        MilestoneUpdateResponse response = milestoneService.saveMilestoneFromAPIG(milestoneUpdateRequest);

        assertThat(response.getResponseCode()).isEqualTo(ResponseCode.SCC0000);

        verify(milestoneRepository, times(2)).save(any(MilestoneEntity.class));
        verify(milestoneHubLocationHandler, times(2)).configureMilestoneHubWithUserHubInfo(any());
        verify(packageJourneySegmentService, never()).updateSegmentStatusByMilestoneEvent(any(Milestone.class), eq(null));
        verify(messageApi, never()).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
        verify(messageApi, never()).sendMilestoneMessage(any(Shipment.class), any(TriggeredFrom.class));
    }

    @Test
    void isAllShipmentFromOrderHaveSameMilestone_thenShouldReturnCorrespondingValue() {
        String shipmentId = "shipment1";
        String orderId = "order1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        Order order = new Order();
        order.setId(orderId);
        shipment.setOrder(order);

        String segmentId = "segment1";

        MilestoneCode milestoneCode = DSP_DELIVERY_SUCCESSFUL;

        when(milestoneRepository.isAllShipmentFromOrderSameMilestone(orderId, segmentId, milestoneCode.name())).thenReturn(0);

        assertThat(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestoneCode, shipment, segmentId)).isFalse();

        when(milestoneRepository.isAllShipmentFromOrderSameMilestone(orderId, segmentId, milestoneCode.name())).thenReturn(1);

        assertThat(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestoneCode, shipment, segmentId)).isTrue();
    }

    @Test
    void whenSomeShipmentsReceiveDeliverySuccessfulMilestone_thenShouldReturnFalse() {
        String shipmentId = "shipment1";
        String orderId = "order1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        Order order = new Order();
        order.setId(orderId);
        shipment.setOrder(order);

        String segmentId = "segment1";

        MilestoneCode milestoneCode = DSP_DELIVERY_SUCCESSFUL;

        when(milestoneRepository.isAllShipmentFromOrderSameMilestone(orderId, segmentId, milestoneCode.name())).thenReturn(0);

        assertThat(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestoneCode, shipment, segmentId)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenNotAllShipmentsHaveSameMilestoneAndCurrentMilestoneIsPickupSuccessful() {
        String shipmentId = "shipment1";
        String orderId = "order1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        Order order = new Order();
        order.setId(orderId);
        shipment.setOrder(order);

        String segmentId = "segment1";

        MilestoneCode milestoneCode = DSP_PICKUP_SUCCESSFUL;

        when(milestoneRepository.isAllShipmentFromOrderSameMilestone(orderId, segmentId, milestoneCode.name())).thenReturn(0);

        assertThat(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestoneCode, shipment, segmentId)).isTrue();
    }

    @Test
    void testUpdateMilestoneAndPackageJourneySegment_hasUpdate() {
        //Given:
        String uuid = UUID.randomUUID().toString();
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());

        when(packageJourneySegmentService.updateSegmentByMilestone(milestone, uuid, true)).thenReturn(true);
        OffsetDateTime previousMilestoneTime = OffsetDateTime.now().minusHours(1);

        //WHEN:
        milestoneService.updateMilestoneAndPackageJourneySegment(milestone, uuid, previousMilestoneTime);

        //THEN:
        verify(packageJourneySegmentService, times(1)).updateSegmentByMilestone(milestone, uuid, true);
        assertThat(milestone.isSegmentUpdatedFromMilestone()).isTrue();
    }

    @Test
    void testUpdateMilestoneAndPackageJourneySegment_NoUpdateDone() {
        //GIVEN:
        String uuid = UUID.randomUUID().toString();
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());

        when(packageJourneySegmentService.updateSegmentByMilestone(milestone, uuid, true)).thenReturn(false);
        OffsetDateTime previousMilestoneTime = OffsetDateTime.now().minusHours(1);
        //WHEN:
        milestoneService.updateMilestoneAndPackageJourneySegment(milestone, uuid, previousMilestoneTime);
        //THEN:

        assertThat(milestone.isSegmentUpdatedFromMilestone()).isFalse();
    }

    private Facility createDummyFacility(String externalFacilityId, String countryId, String stateId, String cityId) {
        Address address = new Address();
        address.setCountryId(countryId);
        address.setStateId(stateId);
        address.setCityId(cityId);

        Facility facility = new Facility();
        facility.setExternalId(externalFacilityId);
        facility.setLocation(address);
        return facility;
    }

    @Test
    void givenVendorUpdateMilestone_whenCreateVendorUpdateMilestone_ThenCreateAssignmentMilestone() {
        Address startFacilityAddress = new Address();
        startFacilityAddress.setCityId("testCityId");

        Facility startFacility = new Facility();
        startFacility.setExternalId("testStartExtId");
        startFacility.setLocation(startFacilityAddress);

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setStartFacility(startFacility);
        segment.setEndFacility(new Facility());

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(segment));

        Shipment shipment = new Shipment();
        shipment.setShipmentJourney(journey);

        MilestoneEntity savedEntity = new MilestoneEntity();

        when(milestoneRepository.save(savedEntity)).thenReturn(savedEntity);

        Milestone milestone = milestoneService.createVendorUpdateMilestone(shipment, segment, MilestoneCode.SHP_ASSIGNMENT_SCHEDULED);

        assertThat(milestone).isNotNull();
        assertThat(milestone.getMilestoneCode()).isEqualTo(MilestoneCode.SHP_ASSIGNMENT_SCHEDULED);

        verify(milestoneHubLocationHandler, times(1)).enrichMilestoneHubLocationDetailsByHubCityId(milestone);
        verify(milestoneTimezoneHelper, times(1)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
    }


    @Test
    void givenMilestoneFromAPIG_whenCreateMilestoneFromAPIG_ThenCreateMilestone() {
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();

        Milestone milestone = milestoneService.createMilestoneFromAPIG(request);

        assertThat(milestone).isNotNull();
    }
}