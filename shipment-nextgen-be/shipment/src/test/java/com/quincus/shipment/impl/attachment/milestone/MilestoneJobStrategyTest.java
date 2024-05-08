package com.quincus.shipment.impl.attachment.milestone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.api.QPortalUtils;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.shipment.api.MilestonePostProcessApi;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.csv.MilestoneCsv;
import com.quincus.shipment.api.exception.JobRecordExecutionException;
import com.quincus.shipment.impl.helper.MilestoneHubLocationHandler;
import com.quincus.shipment.impl.helper.MilestoneTimezoneHelper;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.mapper.MilestoneMapperImpl;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.service.ShipmentService;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.validator.MilestoneCsvValidator;
import com.quincus.web.common.exception.model.ApiCallException;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static com.quincus.shipment.api.constant.SegmentStatus.PLANNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneJobStrategyTest {

    private final static String ORGANIZATION_ID = UUID.randomUUID().toString();
    private MilestoneJobStrategy milestoneJobStrategy;

    @Mock
    private JobMetricsService<MilestoneCsv> jobMetricsService;

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private PackageJourneySegmentService segmentService;

    @Mock
    private MilestoneService milestoneService;

    @Mock
    private MilestoneCsvValidator validator;

    @Mock
    private QPortalApi qPortalApi;

    @Mock
    private MilestonePostProcessApi milestonePostProcessApi;
    @Mock
    private MilestoneHubLocationHandler milestoneHubLocationHandler;
    @Mock
    private MilestoneTimezoneHelper milestoneTimezoneHelper;

    @Captor
    private ArgumentCaptor<Milestone> milestoneArgumentCaptor;

    private static Stream<Arguments> providePickupOrDeliverySuccessfulMilestoneCodeAndName() {
        return Stream.of(
                Arguments.of(MilestoneCode.DSP_PICKUP_SUCCESSFUL, "Pickup Successful"),
                Arguments.of(MilestoneCode.DSP_DELIVERY_SUCCESSFUL, "Delivery Successful")
        );
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        MilestoneMapper milestoneMapper;
        if ((testInfo.getTags().contains("requiresEntityManagerMock"))) {
            milestoneMapper = TestUtil.getInstance().getMilestoneMapper();
        } else {
            milestoneMapper = new MilestoneMapperImpl();
        }

        milestoneJobStrategy = new MilestoneJobStrategy(jobMetricsService, shipmentService, segmentService,
                milestoneService, validator, milestoneMapper, qPortalApi, milestonePostProcessApi, milestoneHubLocationHandler, milestoneTimezoneHelper);
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_requiredFieldsOnly_shouldSaveMilestone() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).minusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenCallRealMethod();
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.updateSegmentStatusByMilestone(any(PackageJourneySegmentEntity.class), any(Milestone.class))).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));
        when(segmentService.save(segmentEntity)).thenReturn(segment);

        assertThatNoException()
                .isThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv));

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, times(1)).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(milestoneArgumentCaptor.capture(), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, times(1)).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
        verify(milestoneHubLocationHandler, times(1)).configureMilestoneHubWithUserHubInfo(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(any(Milestone.class), any(PackageJourneySegment.class));

        Milestone actualMilestone = milestoneArgumentCaptor.getValue();

        assertThat(actualMilestone).isNotNull();
        assertThat(actualMilestone.isSegmentUpdatedFromMilestone()).isTrue();
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_allFields_shouldSaveMilestone() {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        String fromCountry = "Singapore";
        String fromState = "Central";
        String fromCity = "Singapore City";
        String fromFacility = "Warehouse A";
        String toCountry = "USA";
        String toState = "California";
        String toCity = "Los Angeles";
        String toFacility = "Warehouse B";
        String latitude = "34.0522";
        String longitude = "-118.2437";
        String hub = "LA Hub";
        String driverName = "Peter Parker";
        String driverPhoneCode = "+1";
        String driverPhoneNumber = "1234567890";
        String vehicleType = "Truck";
        String vehicleName = "Truck A";
        String vehicleNumber = "ABC123";
        String senderName = "Company A";
        String receiverName = "Company B";
        String eta = "2023-05-10T16:00:00+08:00";
        String notes = "Handle with care.";

        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);
        milestoneCsv.setFromCountry(fromCountry);
        milestoneCsv.setFromState(fromState);
        milestoneCsv.setFromCity(fromCity);
        milestoneCsv.setFromFacility(fromFacility);
        milestoneCsv.setToCountry(toCountry);
        milestoneCsv.setToState(toState);
        milestoneCsv.setToCity(toCity);
        milestoneCsv.setToFacility(toFacility);
        milestoneCsv.setLatitude(latitude);
        milestoneCsv.setLongitude(longitude);
        milestoneCsv.setHub(hub);
        milestoneCsv.setDriverName(driverName);
        milestoneCsv.setDriverPhoneCode(driverPhoneCode);
        milestoneCsv.setDriverPhoneNumber(driverPhoneNumber);
        milestoneCsv.setVehicleType(vehicleType);
        milestoneCsv.setVehicleName(vehicleName);
        milestoneCsv.setVehicleNumber(vehicleNumber);
        milestoneCsv.setSenderName(senderName);
        milestoneCsv.setReceiverName(receiverName);
        milestoneCsv.setEta(eta);
        milestoneCsv.setNotes(notes);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        QPortalMilestone qPortalMilestone = new QPortalMilestone();
        qPortalMilestone.setId("milestone-id-1");
        qPortalMilestone.setName("qPortal Milestone");
        qPortalMilestone.setCode(code);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(List.of(qPortalMilestone));
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));
        try (MockedStatic<QPortalUtils> mocked = mockStatic(QPortalUtils.class)) {
            mocked.when(() -> QPortalUtils.lookupIdFromName(any(), anyList()))
                    .thenAnswer(i -> getTempId(i.getArgument(0)));

            when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
            when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
            when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);

            assertThatNoException()
                    .isThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv));
        }

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneRefId(qPortalMilestone.getId());
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setFromCountryId(getTempId(fromCountry));
        expectedEntity.setFromStateId(getTempId(fromState));
        expectedEntity.setFromCityId(getTempId(fromState));
        expectedEntity.setFromLocationId(getTempId(fromFacility));
        expectedEntity.setToCountryId(getTempId(toCountry));
        expectedEntity.setToStateId(getTempId(toState));
        expectedEntity.setToCityId(getTempId(toState));
        expectedEntity.setToLocationId(getTempId(toFacility));
        Coordinate coordinate = new Coordinate();
        coordinate.setLat(new BigDecimal(latitude));
        coordinate.setLon(new BigDecimal(longitude));
        expectedEntity.setMilestoneCoordinates(coordinate);
        expectedEntity.setHubId(getTempId(hub));
        expectedEntity.setDriverId(getTempId(driverName));
        expectedEntity.setDriverName(driverName);
        expectedEntity.setDriverPhoneCode(driverPhoneCode);
        expectedEntity.setDriverPhoneNumber(driverPhoneNumber);
        expectedEntity.setVehicleId(getTempId(vehicleName));
        expectedEntity.setVehicleName(vehicleName);
        expectedEntity.setVehicleType(vehicleType);
        expectedEntity.setVehicleNumber(vehicleNumber);
        expectedEntity.setSenderName(senderName);
        expectedEntity.setReceiverName(receiverName);
        expectedEntity.setEta(eta);
        expectedEntity.setAdditionalInfo(new MilestoneAdditionalInfo());
        expectedEntity.getAdditionalInfo().setRemarks(notes);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        // when there is hubid enrich hublocationId
        verify(milestoneHubLocationHandler, times(1)).enrichMilestoneHubIdWithLocationIds(any());
        verify(milestoneTimezoneHelper, times(1)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(any(Milestone.class), any(PackageJourneySegment.class));
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_noMilestoneName_shouldSaveMilestone() {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String dateTime = "2023-05-08T16:00:00+08:00";
        String latitude = "34.0522";
        String longitude = "-118.2437";
        String notes = "Handle with care.";

        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneTime(dateTime);
        milestoneCsv.setLatitude(latitude);
        milestoneCsv.setLongitude(longitude);
        milestoneCsv.setNotes(notes);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        QPortalMilestone qPortalMilestone = new QPortalMilestone();
        qPortalMilestone.setId("milestone-id-1");
        qPortalMilestone.setName("qPortal Milestone");
        qPortalMilestone.setCode(code);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(List.of(qPortalMilestone));
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));
        try (MockedStatic<QPortalUtils> mocked = mockStatic(QPortalUtils.class)) {
            mocked.when(() -> QPortalUtils.lookupIdFromName(any(), anyList()))
                    .thenAnswer(i -> getTempId(i.getArgument(0)));

            when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
            when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
            when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);

            assertThatNoException()
                    .isThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv));
        }

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneRefId(qPortalMilestone.getId());
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(qPortalMilestone.getName());
        expectedEntity.setMilestoneTime(dateTime);
        Coordinate coordinate = new Coordinate();
        coordinate.setLat(new BigDecimal(latitude));
        coordinate.setLon(new BigDecimal(longitude));
        expectedEntity.setMilestoneCoordinates(coordinate);
        expectedEntity.setAdditionalInfo(new MilestoneAdditionalInfo());
        expectedEntity.getAdditionalInfo().setRemarks(notes);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_milestoneEarlierThanExisting_shouldSaveMilestoneOnly() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).plusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenCallRealMethod();
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));

        assertThatNoException()
                .isThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv));

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, never()).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(milestoneArgumentCaptor.capture(), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, times(1)).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));

        Milestone actualMilestone = milestoneArgumentCaptor.getValue();

        assertThat(actualMilestone).isNotNull();
        assertThat(actualMilestone.isSegmentUpdatedFromMilestone()).isFalse();
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_segmentIdNotFound_shouldThrowException() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).minusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(String.format("Line %d", milestoneCsv.getRecordNumber()))
                .hasMessageContaining("Data inconsistency");

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, never()).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, never())
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, never()).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
    }

    @Test
    void execute_validateDataCountFailed_shouldThrowException() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);

        doAnswer(i -> {
            List<String> errorList = i.getArgument(1);
            errorList.add("Test Execution");
            return null;
        }).when(validator).validateFixedColumnSize(eq(milestoneCsv), anyList());

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(String.format("Line %d", milestoneCsv.getRecordNumber()))
                .hasMessageContaining("Test Execution")
                .hasMessageNotContaining(" | ");

        verify(milestoneService, never()).save(any());
    }

    @Test
    void execute_validateRequiredFieldsFailed_shouldThrowException() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);

        doAnswer(i -> {
            List<String> errorList = i.getArgument(1);
            errorList.add("Test Execution");
            return null;
        }).when(validator).validateDataAnnotations(eq(milestoneCsv), anyList());

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(String.format("Line %d", milestoneCsv.getRecordNumber()))
                .hasMessageContaining("Test Execution")
                .hasMessageNotContaining(" | ");

        verify(milestoneService, never()).save(any());
    }

    @Test
    void execute_validateConditionalOrCombinationFieldsFailed_shouldThrowException() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);

        doAnswer(i -> {
            List<String> errorList = i.getArgument(1);
            errorList.add("Test Execution 1");
            return null;
        }).when(validator).validateMilestoneCode(eq(milestoneCsv.getMilestoneCode()), anyList());
        doAnswer(i -> {
            List<String> errorList = i.getArgument(1);
            errorList.add("Test Execution 2");
            return null;
        }).when(validator).validateDateTimeFormat(any(), anyList());
        doAnswer(i -> {
            List<String> errorList = i.getArgument(1);
            errorList.add("Test Execution 3");
            return null;
        }).when(validator).validateLocationCombinationAndCoordinates(eq(milestoneCsv), anyList());

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(String.format("Line %d", milestoneCsv.getRecordNumber()))
                .hasMessageContaining("Test Execution 1")
                .hasMessageContaining("Test Execution 2")
                .hasMessageContaining("Test Execution 3")
                .hasMessageContaining(" | ");

        verify(milestoneService, never()).save(any());
    }

    @Test
    void execute_validateQPortalFieldsFailed_shouldThrowException() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        doAnswer(i -> {
            List<String> errorList = i.getArgument(2);
            errorList.add("Test Execution 1");
            return null;
        }).when(validator).validateQPortalLocationCombination(any(), anyList(), anyList());
        doAnswer(i -> {
            List<String> errorList = i.getArgument(2);
            errorList.add("Test Execution 2");
            return null;
        }).when(validator).validateQPortalLocation(any(), anyList(), anyList());
        doAnswer(i -> {
            List<String> errorList = i.getArgument(2);
            errorList.add("Test Execution 3");
            return null;
        }).when(validator).validateQPortalDriver(any(), anyList(), anyList());
        doAnswer(i -> {
            List<String> errorList = i.getArgument(2);
            errorList.add("Test Execution 4");
            return null;
        }).when(validator).validateQPortalVehicle(any(), anyList(), anyList());

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(String.format("Line %d", milestoneCsv.getRecordNumber()))
                .hasMessageContaining("Test Execution 1")
                .hasMessageContaining("Test Execution 2")
                .hasMessageContaining("Test Execution 3")
                .hasMessageContaining("Test Execution 4")
                .hasMessageContaining(" | ");

        verify(milestoneService, never()).save(any());
    }

    @Test
    void execute_noShipment_shouldThrowException() {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(null);

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(MilestoneJobStrategy.ERR_SHIPMENT_NOT_FOUND);

        verify(milestoneService, never()).save(any());
    }

    @Test
    void execute_noShipmentJourney_shouldThrowException() {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(MilestoneJobStrategy.ERR_SHIPMENT_NOT_FOUND);

        verify(milestoneService, never()).save(any());
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_noShipmentWithActiveSegment_shouldThrowException() {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        shipment.setShipmentJourney(journey);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(MilestoneJobStrategy.ERR_SHIPMENT_NOT_FOUND);

        verify(milestoneService, never()).save(any());
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_existingMilestone_shouldSaveMilestone() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        MilestoneEntity existingMilestone = new MilestoneEntity();
        existingMilestone.setOrganizationId(ORGANIZATION_ID);
        ShipmentEntity existingShipment = new ShipmentEntity();
        existingShipment.setId(shipmentId);
        existingMilestone.setShipment(existingShipment);
        existingMilestone.setShipmentId(shipmentId);
        PackageJourneySegmentEntity existingSegment = new PackageJourneySegmentEntity();
        existingSegment.setId(segmentId);
        existingSegment.setStatus(PLANNED);
        existingMilestone.setSegment(existingSegment);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(existingMilestone);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));

        assertThatNoException()
                .isThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv));

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, never()).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_saveFailed_shouldThrowException() {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.save(any())).thenThrow(new RuntimeException("Test execution."));

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining("Exception encountered while attempting to persist milestone.")
                .hasMessageContaining("Test execution.");

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);
        expectedEntity.setSegment(segmentEntity);

        verify(milestoneService, times(1)).save(expectedEntity);
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_createAndSendShipmentMilestoneFails_shouldThrowError() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).minusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenCallRealMethod();
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.updateSegmentStatusByMilestone(any(PackageJourneySegmentEntity.class), any(Milestone.class))).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));
        when(segmentService.save(segmentEntity)).thenReturn(segment);
        doThrow(new KafkaException("Kafka Dummy Exception."))
                .when(milestonePostProcessApi).createAndSendShipmentMilestone(any(Milestone.class), eq(shipment),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining("Kafka Dummy Exception.");

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, times(1)).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, never()).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
    }

    @ParameterizedTest
    @MethodSource("providePickupOrDeliverySuccessfulMilestoneCodeAndName")
    @Tag("requiresEntityManagerMock")
    void execute_createAndSendAPIGWebhooksFails_shouldThrowError(MilestoneCode milestoneCode, String milestoneName)
            throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = milestoneCode.toString();
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(milestoneName);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).minusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(milestoneCode, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenCallRealMethod();
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.updateSegmentStatusByMilestone(any(PackageJourneySegmentEntity.class), any(Milestone.class))).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));
        when(segmentService.save(segmentEntity)).thenReturn(segment);
        doThrow(new ApiCallException("Connection Exception.", HttpStatus.BAD_REQUEST))
                .when(milestonePostProcessApi).createAndSendAPIGWebhooks(any(Milestone.class), eq(shipment),
                        any(PackageJourneySegment.class));

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining("Connection Exception.");

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(milestoneName);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, times(1)).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, times(1)).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_createAndSendSegmentDispatchFails_shouldThrowError() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).minusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenCallRealMethod();
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.updateSegmentStatusByMilestone(any(PackageJourneySegmentEntity.class), any(Milestone.class))).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));
        when(segmentService.save(segmentEntity)).thenReturn(segment);
        doThrow(new KafkaException("Kafka Dummy Exception."))
                .when(milestonePostProcessApi).createAndSendSegmentDispatch(any(Milestone.class), eq(shipment),
                        any(PackageJourneySegment.class));

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining("Kafka Dummy Exception.");

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, times(1)).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, times(1)).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_createAndSendQShipSegmentFails_shouldThrowError() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).minusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenCallRealMethod();
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(true);
        when(segmentService.updateSegmentStatusByMilestone(any(PackageJourneySegmentEntity.class), any(Milestone.class))).thenReturn(true);
        when(segmentService.findBySegmentId(segmentId)).thenReturn(Optional.of(segmentEntity));
        when(segmentService.save(segmentEntity)).thenReturn(segment);
        doThrow(new KafkaException("Kafka Dummy Exception."))
                .when(milestonePostProcessApi).createAndSendQShipSegment(any(Milestone.class), eq(shipment), any(PackageJourneySegment.class));

        assertThatThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining("Kafka Dummy Exception.");

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, times(1)).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, times(1)).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, never()).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
    }

    @Test
    @Tag("requiresEntityManagerMock")
    void execute_notAllShipmentsReceiveMilestone_shouldSaveMilestoneOnly() throws JsonProcessingException {
        String shipmentTrackingId = "SHP1";
        String code = OM_BOOKED.toString();
        String name = "Booked";
        String dateTime = "2023-05-08T16:00:00+08:00";
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setOrganizationId(ORGANIZATION_ID);
        milestoneCsv.setRecordNumber(2);
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);
        milestoneCsv.setShipmentTrackingId(shipmentTrackingId);
        milestoneCsv.setMilestoneCode(code);
        milestoneCsv.setMilestoneName(name);
        milestoneCsv.setMilestoneTime(dateTime);

        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        ShipmentJourney journey = new ShipmentJourney();
        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setStatus(PLANNED);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId(segmentId);

        OffsetDateTime recentMilestoneTime = OffsetDateTime.parse(dateTime).minusHours(1);

        when(qPortalApi.listLocations(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listDrivers(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listVehicles(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(qPortalApi.listMilestones(ORGANIZATION_ID)).thenReturn(Collections.emptyList());
        when(shipmentService.findShipmentFromTrackingIdForMilestoneBatch(eq(shipmentTrackingId), any())).thenReturn(shipment);
        when(milestoneService.findMilestoneFromShipmentAndSegment(OM_BOOKED, shipment, segment)).thenReturn(null);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(shipmentId)).thenReturn(recentMilestoneTime);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(), any(), anyString())).thenReturn(false);

        assertThatNoException()
                .isThrownBy(() -> milestoneJobStrategy.execute(milestoneCsv));

        MilestoneEntity expectedEntity = new MilestoneEntity();
        expectedEntity.setMilestoneCode(OM_BOOKED);
        expectedEntity.setMilestoneName(name);
        expectedEntity.setMilestoneTime(dateTime);
        expectedEntity.setOrganizationId(ORGANIZATION_ID);
        expectedEntity.setShipmentId(shipmentId);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        expectedEntity.setShipment(shipmentEntity);
        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setId(segmentId);
        expectedEntity.setSegment(segmentEntity1);

        verify(milestoneService, times(1)).save(expectedEntity);
        verify(segmentService, never()).save(any(PackageJourneySegmentEntity.class));
        verify(milestonePostProcessApi, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
        verify(milestonePostProcessApi, times(1)).createAndSendAPIGWebhooks(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendSegmentDispatch(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendQShipSegment(any(Milestone.class),
                any(Shipment.class), any(PackageJourneySegment.class));
        verify(milestonePostProcessApi, times(1)).createAndSendNotification(any(Milestone.class),
                any(Shipment.class));
    }

    private String getTempId(String name) {
        return name + "-id";
    }
}
