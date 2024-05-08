package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.api.exception.ShipmentJourneyException;
import com.quincus.shipment.api.exception.ShipmentJourneyMismatchException;
import com.quincus.shipment.impl.helper.SegmentReferenceHolder;
import com.quincus.shipment.impl.helper.SegmentReferenceProvider;
import com.quincus.shipment.impl.mapper.ShipmentJourneyMapper;
import com.quincus.shipment.impl.repository.ShipmentJourneyRepository;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestDataFactory;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.validator.PackageJourneySegmentAlertGenerator;
import com.quincus.shipment.impl.validator.PackageJourneySegmentValidator;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentJourneyServiceTest {

    private static final String GIVEN_SHIPMENT_ID = "SHP-ID";
    private static final String GIVEN_JOURNEY_ID = "SHP-JOURNEY-ID";
    private static final String GIVEN_ORG_ID = "ORG-ID";
    private static final String GIVEN_ORDER_ID = "ORDER-ID";

    @InjectMocks
    private ShipmentJourneyService shipmentJourneyService;
    @Mock
    private ShipmentJourneyRepository journeyRepository;
    @Mock
    private QLoggerAPI qLoggerAPI;
    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private ShipmentJourneyAsyncService shipmentJourneyAsyncService;
    @Mock
    private ShipmentFetchService shipmentFetchService;
    @Mock
    private ShipmentJourneyWriteService shipmentJourneyWriteService;
    @Mock
    private PackageJourneySegmentAlertGenerator packageJourneySegmentAlertGenerator;
    @Mock
    private PackageJourneySegmentValidator packageJourneySegmentValidator;
    @Mock
    private SegmentReferenceProvider segmentReferenceProvider;
    @Mock
    private SegmentReferenceHolder segmentReferenceHolder;

    private static Stream<Arguments> provideJourneyWithSegments() {
        PackageJourneySegment allowed1 = TestDataFactory.createPackageJourneySegment("allowed1", "1", "1");
        PackageJourneySegment allowed2 = TestDataFactory.createPackageJourneySegment("allowed2", "2", "2");
        PackageJourneySegment notAllowed3 = TestDataFactory.createPackageJourneySegment("not-allowed2", "3", "3");
        PackageJourneySegment notAllowed4 = TestDataFactory.createPackageJourneySegment("not-allowed4", "4", "4");

        ShipmentJourney journey1 = new ShipmentJourney();
        journey1.addPackageJourneySegment(allowed1);
        journey1.addPackageJourneySegment(allowed2);

        ShipmentJourney journey2 = new ShipmentJourney();
        journey2.addPackageJourneySegment(notAllowed3);
        journey2.addPackageJourneySegment(notAllowed4);

        ShipmentJourney journey3 = new ShipmentJourney();
        journey3.addPackageJourneySegment(allowed1);
        journey3.addPackageJourneySegment(notAllowed4);

        return Stream.of(
                Arguments.of(Named.of("Journey - all locations allowed", journey1), true),
                Arguments.of(Named.of("Journey - no locations allowed", journey2), false),
                Arguments.of(Named.of("Journey - some locations allowed", journey3), true)
        );
    }

    @Test
    void validateAndUpdateShipmentJourney_validData_shouldNotThrowException() {
        ShipmentJourney shipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(new Alert("Test1", AlertType.ERROR));
        alerts.add(new Alert("Test2", AlertType.WARNING));

        PackageJourneySegment segmentDomain = new PackageJourneySegment();
        segmentDomain.setRefId("0");
        segmentDomain.setTransportType(TransportType.AIR);
        segmentDomain.setAlerts(alerts);

        shipmentJourney.addPackageJourneySegment(segmentDomain);

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(GIVEN_JOURNEY_ID);
        PackageJourneySegmentEntity segmentEntity = TestDataFactory.createPackageJourneySegmentEntity("0", "1");
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        final String givenTrackingId = UUID.randomUUID().toString();
        ShipmentEntity shipmentEntity = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, GIVEN_SHIPMENT_ID);
        shipmentEntity.setShipmentTrackingId(givenTrackingId);
        shipmentEntity.setOrder(new OrderEntity());

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);
        when(shipmentFetchService.findShipmentsForShipmentJourneyUpdate(any(), any(String.class), any(String.class)))
                .thenReturn(List.of(shipmentEntity));
        when(shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(any(), any(), any()))
                .thenReturn(shipmentJourney);

        List<String> result = shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney);

        assertThat(result)
                .isNotNull()
                .contains(givenTrackingId);
        verify(shipmentJourneyWriteService, times(1))
                .updateShipmentJourneyAndUpdateSegments(any(ShipmentJourney.class), any(ShipmentJourneyEntity.class), any(OrderEntity.class));
        verify(shipmentJourneyAsyncService, times(1))
                .sendShipmentJourneyUpdates(anyList(), any(ShipmentJourney.class), any(ShipmentJourney.class));
    }

    @Test
    void validateAndUpdateShipmentJourney_allSegmentLocationsNotPermitted_shouldThrowException() {
        final ShipmentJourney shipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED, new PackageJourneySegment());
        final PackageJourneySegmentEntity segmentEntity = TestDataFactory.createPackageJourneySegmentEntity("1", "1");
        final ShipmentJourneyEntity shipmentJourneyEntity = TestDataFactory.createShipmentJourneyEntity(GIVEN_JOURNEY_ID, segmentEntity);

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        Shipment shipment = new Shipment();
        shipment.setOrder(new Order());
        shipment.setShipmentJourney(shipmentJourney);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(false);

        String expectedErrorMsg = String.format(ShipmentJourneyService.ERR_UPDATE_LOCATION_NOT_COVERED, shipmentJourneyEntity.getId());
        assertThatThrownBy(() -> shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney))
                .isInstanceOfSatisfying(SegmentLocationNotAllowedException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));

        verify(journeyRepository, never()).save(any(ShipmentJourneyEntity.class));
        verify(shipmentJourneyWriteService, never())
                .updateShipmentJourneyEntityFromDomain(any(ShipmentJourney.class), any(ShipmentJourneyEntity.class), any());
        verify(qLoggerAPI, never()).publishShipmentJourneyUpdatedEvent(any(), any(), any(), any());
    }

    @Test
    void validateAndUpdateShipmentJourney_shipmentJourneyMismatch_shouldThrowException() {
        final ShipmentJourney shipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);
        shipmentJourney.addPackageJourneySegment(new PackageJourneySegment());

        final PackageJourneySegmentEntity segmentEntity = TestDataFactory.createPackageJourneySegmentEntity("1", "1");
        final ShipmentJourneyEntity shipmentJourneyEntity = TestDataFactory.createShipmentJourneyEntity(GIVEN_JOURNEY_ID, segmentEntity);

        ShipmentJourneyEntity shipmentJourneyEntity2 = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId("SHP-JOURNEY-ID-MISMATCH");

        ShipmentEntity shipmentEntity = mock(ShipmentEntity.class);
        shipmentEntity.setId(GIVEN_SHIPMENT_ID);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity2);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);

        assertThatThrownBy(() -> shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney))
                .isInstanceOf(ShipmentJourneyMismatchException.class);

        verify(shipmentJourneyWriteService, never())
                .updateShipmentJourneyEntityFromDomain(any(ShipmentJourney.class), any(ShipmentJourneyEntity.class), any());
        verify(journeyRepository, never()).save(any(ShipmentJourneyEntity.class));
    }

    @Test
    void validateAndUpdateShipmentJourney_shipmentJourneyIdNotFound_shouldThrowException() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setShipmentId(GIVEN_SHIPMENT_ID);
        shipmentJourney.setJourneyId(GIVEN_JOURNEY_ID);
        shipmentJourney.addPackageJourneySegment(new PackageJourneySegment());

        Shipment shipment = new Shipment();
        shipment.setOrder(new Order());
        shipment.setShipmentJourney(shipmentJourney);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);
        assertThatThrownBy(() -> shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney))
                .isInstanceOf(ShipmentJourneyMismatchException.class);

        verify(shipmentJourneyWriteService, never())
                .updateShipmentJourneyEntityFromDomain(any(ShipmentJourney.class), any(ShipmentJourneyEntity.class), any());
        verify(journeyRepository, never()).save(any(ShipmentJourneyEntity.class));
    }

    @Test
    void validateAndUpdateShipmentJourney_journeyShipmentOrderMismatch_shouldThrowException() {
        final ShipmentJourney shipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED, new PackageJourneySegment());
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);
        when(shipmentFetchService.findShipmentsForShipmentJourneyUpdate(objectMapper, GIVEN_ORDER_ID, GIVEN_JOURNEY_ID))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney))
                .isInstanceOf(ShipmentJourneyMismatchException.class);

        verify(shipmentJourneyWriteService, never()).updateShipmentJourneyEntityFromDomain(any(ShipmentJourney.class), any(ShipmentJourneyEntity.class), any());
        verify(journeyRepository, never()).save(any(ShipmentJourneyEntity.class));
    }

    @Test
    void validateAndUpdateShipmentJourney_shipmentJourneyUpdateError_shouldThrowException() {
        ShipmentJourney shipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);
        shipmentJourney.addPackageJourneySegment(new PackageJourneySegment());

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(GIVEN_JOURNEY_ID);
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("SEGMENT-1");
        segmentEntity.setRefId("1");
        segmentEntity.setSequence("1");
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        final ShipmentEntity shipmentEntity = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, GIVEN_SHIPMENT_ID);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);
        when(shipmentFetchService.findShipmentsForShipmentJourneyUpdate(any(), any(String.class), any(String.class)))
                .thenReturn(List.of(shipmentEntity));
        when(shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(any(), any(), any()))
                .thenThrow(new ShipmentJourneyException(""));

        assertThatThrownBy(() -> shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney))
                .isInstanceOf(ShipmentJourneyException.class);

        verify(shipmentJourneyWriteService, times(1))
                .updateShipmentJourneyAndUpdateSegments(any(), any(), any());
    }

    @Test
    void validateAndUpdateShipmentJourney_FromUI_withoutValidRefIdAndSegmentType_shouldNotThrowException() {
        ShipmentJourney shipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);
        shipmentJourney.setPackageJourneySegments(List.of(
                createPackageJourneySegment(TransportType.GROUND, "instruction 0"),
                createPackageJourneySegment(TransportType.AIR, "instruction 1"),
                createPackageJourneySegment(TransportType.AIR, "instruction 2"),
                createPackageJourneySegment(TransportType.GROUND, "instruction 3")));

        final ShipmentJourneyEntity shipmentJourneyEntity = TestDataFactory.createShipmentJourneyEntity(
                GIVEN_JOURNEY_ID, TestDataFactory.createPackageJourneySegmentEntity("1", "1"));

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        ShipmentEntity shipmentEntity = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, GIVEN_SHIPMENT_ID);
        shipmentEntity.setOrder(new OrderEntity());

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);
        when(shipmentFetchService.findShipmentsForShipmentJourneyUpdate(any(), any(String.class), any(String.class))).thenReturn(List.of(shipmentEntity));
        when(shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(any(), any(), any()))
                .thenReturn(shipmentJourney);

        assertThat(shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney)).isNotNull();

        verify(shipmentJourneyWriteService, times(1))
                .updateShipmentJourneyAndUpdateSegments(any(ShipmentJourney.class), any(ShipmentJourneyEntity.class), any(OrderEntity.class));
        verify(shipmentJourneyAsyncService, times(1))
                .sendShipmentJourneyUpdates(anyList(), any(ShipmentJourney.class), any(ShipmentJourney.class));
    }

    private PackageJourneySegment createPackageJourneySegment(TransportType transportType, String instruction) {
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setRefId("0");
        packageJourneySegment.setSequence("0");
        packageJourneySegment.setJourneyId(GIVEN_JOURNEY_ID);
        packageJourneySegment.setTransportType(transportType);
        packageJourneySegment.setInstruction(instruction);
        return packageJourneySegment;
    }

    @Test
    void validateAndUpdateShipmentJourney_shipmentNotFound_shouldThrowException() {
        final ShipmentJourney shipmentJourney = TestDataFactory.createShipmentJourney(
                GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED, new PackageJourneySegment());

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);
        when(shipmentFetchService.findShipmentsForShipmentJourneyUpdate(any(), any(String.class), any(String.class))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney))
                .isInstanceOf(ShipmentJourneyMismatchException.class);

        verify(shipmentJourneyWriteService, never())
                .updateShipmentJourneyEntityFromDomain(any(ShipmentJourney.class), any(ShipmentJourneyEntity.class), any());
        verify(journeyRepository, never()).save(any(ShipmentJourneyEntity.class));
    }

    @ParameterizedTest
    @MethodSource("provideJourneyWithSegments")
    void isJourneyAnyLocationAllowed_withSegments_shouldReturnExpected(ShipmentJourney journey, boolean expected) {
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class)))
                .thenAnswer(o -> {
                    PackageJourneySegment segment = o.getArgument(0);
                    return segment.getSegmentId().startsWith("allowed");
                });
        assertThat(shipmentJourneyService.isAnyLocationAllowedOnPackageJourneySegments(journey)).isEqualTo(expected);

        verify(packageJourneySegmentService, atLeastOnce())
                .isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class));
    }

    @Test
    void isJourneyAnyLocationAllowed_noSegments_shouldReturnFalse() {
        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(Collections.emptyList());

        assertThat(shipmentJourneyService.isAnyLocationAllowedOnPackageJourneySegments(journey)).isFalse();
    }

    @Test
    void givenOrderWithMultipleShipments_whenUpdatingSingleShipmentJourney_thenUpdateRelatedShipmentsByOrder() {
        final OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        // Shipment 1
        final ShipmentJourney shipmentJourney1 = TestDataFactory.createShipmentJourney(
                GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED, new PackageJourneySegment());
        final ShipmentJourneyEntity shipmentJourneyEntity1 = TestDataFactory.createShipmentJourneyEntity(
                GIVEN_JOURNEY_ID, TestDataFactory.createPackageJourneySegmentEntity("1", "1"));
        final ShipmentEntity shipmentEntity1 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity1, organizationEntity, GIVEN_SHIPMENT_ID);

        // Shipment 2
        final String givenSecondShipmentId = "SHP-ID2";
        final String givenSecondShipmentJourneyId = "SHP-JOURNEY-ID2";
        final ShipmentJourneyEntity shipmentJourneyEntity2 = TestDataFactory.createShipmentJourneyEntity(
                givenSecondShipmentJourneyId, TestDataFactory.createPackageJourneySegmentEntity("1", "1"));
        final ShipmentEntity shipmentEntity2 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity2, organizationEntity, givenSecondShipmentId);
        final ShipmentJourney updatedShipmentJourney = TestDataFactory.createShipmentJourney(
                givenSecondShipmentId, givenSecondShipmentJourneyId, GIVEN_ORDER_ID, JourneyStatus.PLANNED, new PackageJourneySegment());

        given(shipmentFetchService.findShipmentsForShipmentJourneyUpdate(any(), any(String.class), any(String.class)))
                .willReturn(List.of(shipmentEntity1, shipmentEntity2));
        given(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class)))
                .willReturn(true)
                .willReturn(true);
        given(shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(any(), any(), any()))
                .willReturn(updatedShipmentJourney);

        // when
        List<String> trackingIdList = shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney1);

        // then
        assertThat(trackingIdList)
                .isNotNull()
                .hasSize(2);
        verify(shipmentJourneyWriteService, times(1))
                .updateShipmentJourneyAndUpdateSegments(any(), any(), any());
        verify(shipmentJourneyAsyncService, times(1))
                .sendShipmentJourneyUpdates(any(), any(), any());
    }

    @Test
    void testUpdateShipmentJourney_WithValidData_ShouldReturnUpdatedShipments() {

        ShipmentJourney shipmentJourneyUpdates = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(new Alert("Test1", AlertType.ERROR));
        alerts.add(new Alert("Test2", AlertType.WARNING));

        PackageJourneySegment segmentDomain1 = new PackageJourneySegment();
        segmentDomain1.setRefId("0");
        segmentDomain1.setSequence("0");
        segmentDomain1.setTransportType(TransportType.AIR);
        segmentDomain1.setAlerts(alerts);

        PackageJourneySegment segmentDomain2 = new PackageJourneySegment();
        segmentDomain2.setRefId("0");
        segmentDomain2.setSequence("1");
        segmentDomain2.setTransportType(TransportType.AIR);
        segmentDomain2.setAlerts(alerts);

        shipmentJourneyUpdates.setPackageJourneySegments(List.of(segmentDomain1, segmentDomain2));

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(GIVEN_JOURNEY_ID);
        PackageJourneySegmentEntity segmentEntity = TestDataFactory.createPackageJourneySegmentEntity("0", "1");
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        ShipmentEntity shipmentEntity1 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, "shipment1");
        shipmentEntity1.setOrder(new OrderEntity());

        ShipmentEntity shipmentEntity2 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, "shipment1");
        shipmentEntity1.setOrder(new OrderEntity());

        ShipmentJourney updatedShipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID + "1", GIVEN_ORDER_ID, JourneyStatus.PLANNED);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegment.class))).thenReturn(true);
        when(shipmentFetchService.findShipmentsForShipmentJourneyUpdate(any(), any(String.class), any(String.class)))
                .thenReturn(List.of(shipmentEntity1, shipmentEntity2));
        when(shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(any(), any(), any()))
                .thenReturn(updatedShipmentJourney);

        List<String> updatedShipments = shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourneyUpdates);

        assertThat(updatedShipments)
                .hasSize(2)
                .doesNotContainNull();
        verify(shipmentJourneyWriteService, times(1)).updateShipmentJourneyAndUpdateSegments(any(), any(), any());
        verify(shipmentJourneyAsyncService, times(1)).sendShipmentJourneyUpdates(any(), any(), any());
    }

    @Test
    void updateShipmentJourneyEntityFromDomain_shipmentJourneyNull_shouldNotUpdateShipmentJourneyEntity() {
        ShipmentJourney domain = TestUtil.getInstance().createSingleShipmentData().getShipmentJourney();
        ShipmentJourneyEntity entity = ShipmentJourneyMapper.mapDomainToEntity(domain);

        Order order = new Order();
        order.setOpsType("P2P");
        order.setPickupTimezone("UTC+08:00");
        order.setDeliveryTimezone("UTC+09:00");

        shipmentJourneyWriteService.updateShipmentJourneyEntityFromDomain(null, entity, order);

        assertThat(entity.getId())
                .withFailMessage("Journey ID mismatch.")
                .isEqualTo(domain.getJourneyId());

        assertThat(entity.getStatus())
                .withFailMessage("Journey Status mismatch.")
                .isEqualTo(domain.getStatus());

        List<PackageJourneySegment> pjsDomain = domain.getPackageJourneySegments();
        List<PackageJourneySegmentEntity> pjsEntity = entity.getPackageJourneySegments();
        assertThat(pjsEntity)
                .withFailMessage("Package Journey Segment Size mismatch.")
                .hasSameSizeAs(pjsDomain);
    }

    @Test
    void testCreate_ShouldReturnShipmentJourneyEntity() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setJourneyId("journeyId");
        when(segmentReferenceProvider.generateReference(any())).thenReturn(segmentReferenceHolder);

        ShipmentJourneyEntity result = shipmentJourneyService.create(shipmentJourney, new Order());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(shipmentJourney.getJourneyId());

        verify(packageJourneySegmentValidator, times(1)).validatePackageJourneySegments(shipmentJourney);
        verify(packageJourneySegmentAlertGenerator, times(1)).generateAlertPackageJourneySegments(shipmentJourney, false);
    }

    @Test
    void testUpdate_ShouldReturnShipmentJourneyEntity() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setJourneyId("journeyId");

        Order order = new Order();
        order.setOpsType("P2P");
        order.setPickupTimezone("UTC+08:00");
        order.setDeliveryTimezone("UTC+09:00");

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(shipmentJourney.getJourneyId());

        boolean segmentsUpdated = true;

        ShipmentJourneyEntity result = shipmentJourneyService.update(shipmentJourney, shipmentJourneyEntity, order,
                segmentsUpdated);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(shipmentJourney.getJourneyId());

        verify(packageJourneySegmentValidator, times(1)).validatePackageJourneySegments(shipmentJourney);
        verify(packageJourneySegmentAlertGenerator, times(1)).generateAlertPackageJourneySegments(shipmentJourney, true);
        verify(shipmentJourneyWriteService, times(1)).updateShipmentJourneyEntityFromDomain(shipmentJourney, shipmentJourneyEntity, order);
    }

    @Test
    void testCreateShipmentJourney() {
        Shipment shipment = new Shipment();
        shipment.setOrder(new Order());
        shipment.setShipmentJourney(new ShipmentJourney());
        when(segmentReferenceProvider.generateReference(any())).thenReturn(segmentReferenceHolder);

        shipmentJourneyService.createShipmentJourneyEntity(shipment, Collections.emptyList(), false);

        verify(packageJourneySegmentService, never()).updateFacilityAndPartner(any(), any(), any(), any());
        verify(journeyRepository, never()).save(any(ShipmentJourneyEntity.class));
    }

    @Test
    void testUpdateShipmentJourney() {
        Shipment shipment = mock(Shipment.class);
        ShipmentJourney shipmentJourney = mock(ShipmentJourney.class);
        Order order = mock(Order.class);
        ShipmentEntity existingEntity = mock(ShipmentEntity.class);

        when(shipment.getShipmentJourney()).thenReturn(shipmentJourney);
        when(existingEntity.getShipmentTrackingId()).thenReturn("testId");
        when(shipment.getShipmentTrackingId()).thenReturn("testId");
        when(existingEntity.getShipmentJourney()).thenReturn(mock(ShipmentJourneyEntity.class));
        when(shipment.getOrder()).thenReturn(order);
        when(journeyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        shipmentJourneyService.createShipmentJourneyEntity(shipment, Collections.singletonList(existingEntity), true);

        verify(packageJourneySegmentService, times(1)).updateFacilityAndPartner(any(), any(), any(), any());
    }

    @Test
    void testUpdateShipmentJourneyWithSegmentUpdateFalse() {
        Shipment shipment = new Shipment();
        shipment.setShipmentTrackingId("testId");
        Order order = new Order();
        shipment.setOrder(order);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipment.setShipmentJourney(shipmentJourney);
        ShipmentEntity existingEntity = mock(ShipmentEntity.class);

        when(existingEntity.getShipmentTrackingId()).thenReturn("testId");
        when(existingEntity.getShipmentJourney()).thenReturn(mock(ShipmentJourneyEntity.class));

        shipmentJourneyService.createShipmentJourneyEntity(shipment, Collections.singletonList(existingEntity), false);

        verify(packageJourneySegmentService, never()).updateFacilityAndPartner(any(), any(), any(), any());
    }
}
