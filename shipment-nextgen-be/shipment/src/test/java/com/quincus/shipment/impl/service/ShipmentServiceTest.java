package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.api.domain.Root;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.NotificationApi;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.ShipmentResult;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateResponse;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.api.exception.ShipmentInvalidStatusException;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.api.filter.ShipmentFilterResult;
import com.quincus.shipment.impl.enricher.LocationCoverageCriteriaEnricher;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.helper.CreateShipmentHelper;
import com.quincus.shipment.impl.helper.MilestoneHubLocationHandler;
import com.quincus.shipment.impl.helper.MilestoneTimezoneHelper;
import com.quincus.shipment.impl.helper.UpdateShipmentHelper;
import com.quincus.shipment.impl.helper.segment.SegmentUpdateChecker;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.mapper.ShipmentCriteriaMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.projection.ShipmentProjectionListingPage;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TupleDataFactory;
import com.quincus.shipment.impl.validator.PackageJourneySegmentAlertGenerator;
import com.quincus.shipment.impl.validator.ShipmentValidator;
import com.quincus.shipment.impl.valueobject.OrderShipmentMetadata;
import com.quincus.web.common.config.JacksonConfiguration;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import javax.persistence.Tuple;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_ON_ROUTE;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DISPATCH_SCHEDULED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ON_ROUTE_TO_PICKUP;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_ARRIVED_AT_HUB;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_ARRIVED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_DEPARTED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_SORTED_IN_HUB;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {
    private static final String WARN_SHIPMENT_NOT_FOUND_BY_TRACKING_ID_AND_ORGANIZATION_ID = "Shipment not found with shipment tracking id: `%s` and organization id : `%s`";
    @Mock
    List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;
    @InjectMocks
    private ShipmentService shipmentService;
    @Mock
    private AlertService alertService;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private QLoggerAPI qLoggerAPI;
    @Mock
    private MilestoneService milestoneService;
    @Mock
    private CreateShipmentHelper createShipmentHelper;
    @Mock
    private UpdateShipmentHelper updateShipmentHelper;
    @Mock
    private MessageApi messageApi;
    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private PackageJourneySegmentAlertGenerator packageJourneySegmentAlertGenerator;
    @Mock
    private FlightStatsEventService flightStatsEventService;
    @Spy
    private ObjectMapper objectMapper = new JacksonConfiguration().buildObjectMapper();
    @Spy
    private MilestoneMapper milestoneMapper = Mappers.getMapper(MilestoneMapper.class);
    @Spy
    private ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
    @Mock
    private ShipmentProjectionListingPage listingPage;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private PackageDimensionService packageDimensionService;
    @Mock
    private ShipmentCriteriaMapper shipmentCriteriaMapper;
    @Mock
    private LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;
    @Mock
    private UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;
    @Mock
    private PackageJourneySegmentRepository packageJourneySegmentRepository;
    @Mock
    private ShipmentFetchService shipmentFetchService;
    @Mock
    private NotificationApi notificationApi;
    @Mock
    private ShipmentEnrichmentService shipmentEnrichmentService;
    @Mock
    private ShipmentPostProcessService shipmentPostProcessService;
    @Mock
    private ShipmentJourneyService shipmentJourneyService;
    @Mock
    private ShipmentValidator shipmentValidator;
    @Mock
    private MilestoneHubLocationHandler milestoneHubLocationHandler;
    @Mock
    private MilestonePostProcessService milestonePostProcessService;
    @Mock
    private OrderShipmentMetadataService orderShipmentMetadataService;
    @Mock
    private PackageLogService packageLogService;
    @Mock
    private MilestoneTimezoneHelper milestoneTimezoneHelper;
    @Mock
    private SegmentUpdateChecker segmentUpdateChecker;

    @Test
    void create_ShipmentDomainMappedToAnEntity_ShouldReturnShipmentDomain() {
        String orgId = "org1";
        String partnerId = "partner1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("SHP-1");
        shipmentEntity.setPartnerId(partnerId);
        Organization organization = new Organization();
        organization.setId(orgId);

        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setShipmentPackage(new Package());
        String orderId = "orderId";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentEntity.setOrder(orderEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(orgId);
        shipmentEntity.setOrganization(organizationEntity);
        Milestone milestoneDomain = new Milestone();
        milestoneDomain.setMilestoneCode(OM_BOOKED);
        milestoneDomain.setMilestoneName("Shipment Created");
        shipmentDomain.setMilestone(milestoneDomain);
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        Order order = new Order();
        order.setId(orderId);
        shipmentDomain.setOrder(order);

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(userDetailsProvider.getCurrentPartnerId()).thenReturn(partnerId);
        when(createShipmentHelper.createShipmentEntity(any(), any()))
                .thenReturn(shipmentEntity);
        when(shipmentRepository.save(any(ShipmentEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(any(Shipment.class), any(MilestoneCode.class)))
                .thenReturn(new Milestone());

        Shipment createdShipment = shipmentService.create(shipmentDomain, shipmentJourneyEntity);
        assertThat(createdShipment).isNotNull();
        assertThat(createdShipment.getOrganization()).isNotNull();
        assertThat(createdShipment.getOrganization().getId()).isEqualTo(orgId);
        assertThat(createdShipment.getPartnerId()).isEqualTo(partnerId);

        verify(shipmentRepository, times(1)).save(any(ShipmentEntity.class));
        verify(milestoneService, times(1)).createOMTriggeredMilestoneAndSendMilestoneMessage(any(Shipment.class), any(MilestoneCode.class));

        //call was transferred outside
        verify(shipmentPostProcessService, never()).sendUpdateToQship(any(Shipment.class));

        verify(shipmentPostProcessService, times(1)).publishQloggerCreateEvents(any(Shipment.class));
        verify(packageLogService, times(1)).createPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

    @Test
    void createBulk_ShipmentDomainMappedToAnEntity_ShouldReturnSuccessResultList() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("SHP-1");
        Shipment shipmentDomain = new Shipment();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId("orderID");
        shipmentEntity.setOrder(orderEntity);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Milestone milestoneDomain = new Milestone();
        milestoneDomain.setMilestoneCode(OM_BOOKED);
        milestoneDomain.setMilestoneName("Shipment Created");
        shipmentDomain.setMilestone(milestoneDomain);
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        Order order = new Order();
        order.setId("orderID");
        shipmentDomain.setOrder(order);

        when(createShipmentHelper.createShipmentEntity(any(), any())).thenReturn(shipmentEntity);
        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);
        when(milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(any(), any(MilestoneCode.class))).thenReturn(new Milestone());
        when(shipmentFetchService.findShipmentByTrackingId(any())).thenReturn(Optional.ofNullable(shipmentEntity));
        when(shipmentJourneyService.create(any(), any())).thenReturn(shipmentEntity.getShipmentJourney());
        when(shipmentRepository.isShipmentWithTrackingIdAndOrgIdExist(any(), any())).thenReturn(false);

        List<ShipmentResult> shipmentResultList = shipmentService.createBulk(List.of(shipmentDomain));

        assertThat(shipmentResultList).hasSize(1);
        assertThat(shipmentResultList.get(0).isSuccess()).isTrue();

        verify(shipmentRepository, times(1)).save(any(ShipmentEntity.class));
        verify(createShipmentHelper, times(1)).createShipmentEntity(any(), any());
        verify(milestoneService, times(1)).createOMTriggeredMilestoneAndSendMilestoneMessage(any(Shipment.class), any(MilestoneCode.class));

        //call was transferred outside
        verify(shipmentPostProcessService, never()).sendUpdateToQship(any(Shipment.class));

        verify(shipmentPostProcessService, times(1)).publishQloggerCreateEvents(any());
    }

    @Test
    void createBulk_ExceptionThrown_ShouldReturnFailResultList() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        Order order = new Order();
        shipmentDomain.setOrder(order);
        shipmentDomain.setShipmentJourney(new ShipmentJourney());

        when(shipmentFetchService.findShipmentByTrackingId(any())).thenReturn(Optional.ofNullable(shipmentEntity));
        when(shipmentJourneyService.create(any(), any())).thenReturn(shipmentEntity.getShipmentJourney());
        when(shipmentRepository.isShipmentWithTrackingIdAndOrgIdExist(any(), any())).thenReturn(false);

        List<ShipmentResult> shipmentResultList = shipmentService.createBulk(List.of(shipmentDomain));

        assertThat(shipmentResultList).hasSize(1);
        assertThat(shipmentResultList.get(0).isSuccess()).isFalse();

        verify(shipmentRepository, atMostOnce()).save(any());
        verify(createShipmentHelper, times(1)).createShipmentEntity(any(), any());
        verify(messageApi, never()).sendShipmentToQShip(any());
        verify(qLoggerAPI, never()).publishShipmentCreatedEvent(any(), any());
        verify(qLoggerAPI, never()).publishShipmentJourneyCreatedEvent(any(), any(), any());
    }

    @Test
    void findByIdAndCheckLocationPermission_EntityFound_ShouldReturnDomain() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("SHP-ID1");
        shipmentEntity.setUserId("USR1");
        String orderId = "order-id-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        shipmentEntity.setOrder(orderEntity);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByIdWithFetchOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);

        Shipment shipmentDomainResult = shipmentService.findByIdAndCheckLocationPermission("ID0");

        assertThat(shipmentDomainResult.getId()).isEqualTo(shipmentDomain.getId());
        assertThat(shipmentDomainResult.getUserId()).isEqualTo(shipmentDomain.getUserId());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentPackageJourneySegmentsWithInstructions(any());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentJourneyAndSegmentWithAlert(any());
        verify(packageJourneySegmentService, times(1)).enrichSegmentsWithOrderInstructions(eq(orderId), anyList());
    }

    @Test
    void findByIdAndCheckLocationPermission_EntityNotFound_ShouldThrowException() {
        String shipmentId = "ID0";
        String errorMessage = "Shipment Id %s not found.";
        String expectedErrorMsg = String.format(errorMessage, shipmentId);
        when(shipmentFetchService.findByIdWithFetchOrThrowException(anyString())).thenThrow(new ShipmentNotFoundException(String.format(errorMessage, shipmentId)));

        assertThatThrownBy(() -> shipmentService.findByIdAndCheckLocationPermission(shipmentId))
                .isInstanceOfSatisfying(ShipmentNotFoundException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));
    }

    @Test
    void findByIdAndCheckLocationPermission_allSegmentLocationsNotPermitted_ShouldThrowException() {
        String shipmentId = "SHP-ID1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        shipmentEntity.setUserId("USR1");
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setCountry(new LocationEntity());
        locationHierarchyEntity.setState(new LocationEntity());
        locationHierarchyEntity.setCity(new LocationEntity());
        locationHierarchyEntity.setFacility(new LocationEntity());
        segmentEntity.setStartLocationHierarchy(locationHierarchyEntity);
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByIdWithFetchOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(false);

        String expectedErrorMsg = String.format(ShipmentService.ERR_READ_NOT_ALLOWED, shipmentId);
        assertThatThrownBy(() -> shipmentService.findByIdAndCheckLocationPermission(shipmentId))
                .isInstanceOfSatisfying(SegmentLocationNotAllowedException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));
        verify(shipmentEnrichmentService, times(1)).enrichShipmentPackageJourneySegmentsWithInstructions(any());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentJourneyAndSegmentWithAlert(any());
    }

    @Test
    void findByIdAndCheckLocationPermission_SingleSegmentWithNullFacility_ShouldReturnDomain() {
        String shipmentId = "SHP-ID1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        shipmentEntity.setUserId("USR1");
        String orderId = "order-id-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        shipmentEntity.setOrder(orderEntity);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByIdWithFetchOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(false);

        String.format(ShipmentService.ERR_READ_NOT_ALLOWED, shipmentId);
        shipmentService.findByIdAndCheckLocationPermission(shipmentId);
        verify(shipmentEnrichmentService, times(1)).enrichShipmentPackageJourneySegmentsWithInstructions(any());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentJourneyAndSegmentWithAlert(any());
        verify(packageJourneySegmentService, times(1)).enrichSegmentsWithOrderInstructions(eq(orderId), anyList());

    }

    @Test
    void findById_EntityFound_ShouldReturnDomain() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("SHP-ID1");
        shipmentEntity.setUserId("USR1");
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(updateShipmentHelper.getShipmentById(anyString())).thenReturn(shipmentDomain);

        Shipment shipmentDomainResult = updateShipmentHelper.getShipmentById("ID0");

        assertThat(shipmentDomainResult.getId()).isEqualTo(shipmentDomain.getId());
        assertThat(shipmentDomainResult.getUserId()).isEqualTo(shipmentDomain.getUserId());
    }

    @Test
    void update_InvalidShipmentTrackingId_ShouldNotUpdateEntityDomain() {
        Shipment shipment = new Shipment();
        shipment.setId("ID");
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.GROUND);
        List<PackageJourneySegment> packageJourneySegmentList = new ArrayList<>();
        packageJourneySegmentList.add(packageJourneySegment);
        shipmentJourney.setPackageJourneySegments(packageJourneySegmentList);
        shipment.setShipmentJourney(shipmentJourney);

        Organization organization = new Organization();
        organization.setId("TEST-ORG-ID");
        organization.setName("TEST-ORG-NAME");
        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenThrow(mock(ShipmentNotFoundException.class));
        assertThatThrownBy(() -> shipmentService.update(shipment, true)).isInstanceOf(ShipmentNotFoundException.class);
        verify(shipmentRepository, never()).save(any(ShipmentEntity.class));
        verify(messageApi, never()).sendShipmentToQShip(any(Shipment.class));
        verify(flightStatsEventService, never()).subscribeFlight(anyList());
    }

    @Test
    void update_allSegmentLocationsNotPermitted_ShouldThrowException() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";

        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.GROUND);
        List<PackageJourneySegment> packageJourneySegmentList = new ArrayList<>();
        packageJourneySegmentList.add(packageJourneySegment);
        shipmentJourney.setJourneyId("journey-1");
        shipmentJourney.setPackageJourneySegments(packageJourneySegmentList);
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(false);

        String expectedErrorMsg = String.format(ShipmentService.ERR_UPDATE_NOT_ALLOWED, shipmentDomain.getId());
        assertThatThrownBy(() -> shipmentService.update(shipmentDomain, true))
                .isInstanceOfSatisfying(SegmentLocationNotAllowedException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));

        verify(updateShipmentHelper, never()).updateShipmentEntityFromDomain(any(Shipment.class), any(ShipmentEntity.class));
        verify(shipmentRepository, never()).save(any(ShipmentEntity.class));
        verify(messageApi, never()).sendShipmentToQShip(any(Shipment.class));
        verify(flightStatsEventService, never()).subscribeFlight(anyList());
        verify(qLoggerAPI, never()).publishShipmentUpdatedEvent(any(), any());
        verify(qLoggerAPI, never()).publishShipmentJourneyUpdatedEvent(any(), any(), any(), any());
    }

    @Test
    void update_validShipment_ShouldNotThrowException() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";

        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.GROUND);
        List<PackageJourneySegment> packageJourneySegmentList = new ArrayList<>();
        packageJourneySegmentList.add(packageJourneySegment);
        shipmentJourney.setJourneyId("journey-1");
        shipmentJourney.setPackageJourneySegments(packageJourneySegmentList);
        shipmentDomain.setShipmentJourney(shipmentJourney);
        shipmentDomain.setSegmentsUpdatedFromSource(true);
        shipmentDomain.setNotes("New Shipment");

        Organization organization = new Organization();
        organization.setId("ORG-ID");
        organization.setName("ORG-NAME");
        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(shipmentFetchService.findShipmentByTrackingId(anyString())).thenReturn(Optional.of(shipmentEntity));
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(shipmentJourneyService.update(any(), any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());
        when(shipmentJourneyService.save(any())).thenAnswer(i -> i.getArgument(0));
        when(shipmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(shipmentService.update(shipmentDomain, true)).isNotNull();

        verify(updateShipmentHelper, times(1)).updateShipmentEntityFromDomain(any(Shipment.class), any(ShipmentEntity.class));
        verify(shipmentRepository, times(1)).save(any(ShipmentEntity.class));
        verify(shipmentPostProcessService, times(1)).publishQLoggerUpdateEvents(any(), any(), any());
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    void createOrUpdate_newShipmentFromOm_ShouldReturnShipmentDomain() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("SHP-1");
        Shipment shipmentDomain = new Shipment();
        OrderEntity order = new OrderEntity();
        order.setId("orderId");
        shipmentEntity.setOrder(order);
        AddressEntity origin = new AddressEntity();
        origin.setId("address-1");
        origin.setExternalId(UUID.randomUUID().toString());
        shipmentEntity.setOrigin(origin);
        AddressEntity destination = new AddressEntity();
        destination.setId("address-2");
        destination.setExternalId(UUID.randomUUID().toString());
        shipmentEntity.setDestination(destination);
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId("journey-1");
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segment-1");
        segmentEntity.setRefId("1");
        segmentEntity.setTransportType(TransportType.GROUND);
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setId("package-1");
        shipmentEntity.setShipmentPackage(packageEntity);
        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        serviceTypeEntity.setId("svc-type-1");
        shipmentEntity.setServiceType(serviceTypeEntity);
        Milestone milestoneDomain = new Milestone();
        milestoneDomain.setMilestoneCode(OM_BOOKED);
        milestoneDomain.setMilestoneName("Shipment Created");
        shipmentDomain.setMilestone(milestoneDomain);
        shipmentDomain.setOrder(new Order());
        shipmentDomain.getOrder().setId("");
        shipmentDomain.setShipmentTrackingId("");
        shipmentDomain.setOrigin(new Address());
        shipmentDomain.setDestination(new Address());
        shipmentDomain.setShipmentPackage(new Package());
        shipmentDomain.setServiceType(new ServiceType());
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        OrderShipmentMetadata orderShipmentMetadata =
                new OrderShipmentMetadata(new OrganizationEntity(), order, new CustomerEntity(), new AddressEntity(), new AddressEntity(), new ServiceTypeEntity(), journeyEntity);

        when(createShipmentHelper.createShipmentEntity(any(), any())).thenReturn(shipmentEntity);
        when(shipmentRepository.save(shipmentEntity)).thenReturn(shipmentEntity);
        when(milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(any(Shipment.class), any(MilestoneCode.class))).thenReturn(new Milestone());
        when(orderShipmentMetadataService.createOrderShipmentMetaData(any(), any())).thenReturn(orderShipmentMetadata);
        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain), true)).isNotNull();

        verify(createShipmentHelper, times(1)).createShipmentEntity(any(), any());
        verify(shipmentRepository, times(1)).save(any());
        verify(milestoneService, times(1)).createOMTriggeredMilestoneAndSendMilestoneMessage(any(Shipment.class), any(MilestoneCode.class));

        //call was transferred outside
        verify(shipmentPostProcessService, never()).sendUpdateToQship(any(Shipment.class));
        verify(shipmentPostProcessService, times(1)).publishQloggerCreateEvents(any());
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    void givenShipmentDomainWithDuplicateTrackingId_whenCreateShipment_ShouldThrowQuincusValidationException() {

        Shipment shipmentDomain = new Shipment();
        String id = "test tracking";
        shipmentDomain.setShipmentTrackingId(id);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentTrackingId(id);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId("orderId");
        shipmentEntity.setOrder(orderEntity);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId("journeyId");
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("123");
        when(shipmentRepository.isShipmentWithTrackingIdAndOrgIdExist(
                shipmentDomain.getShipmentTrackingId(),
                userDetailsProvider.getCurrentOrganizationId())).thenReturn(true);

        assertThatThrownBy(() -> shipmentService.create(shipmentDomain, shipmentJourneyEntity))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessage("Shipment with Tracking Id: test tracking already exist");

    }

    @Test
    void createOrUpdate_existingShipmentFromOm_ShouldReturnShipmentDomain() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";

        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        shipmentDomain.setSegmentsUpdatedFromSource(true);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);
        when(shipmentRepository.findAllByOrderId(anyString(), anyString())).thenReturn(List.of(shipmentEntity));
        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain), true)).isNotNull();

        verify(updateShipmentHelper, times(1)).updateShipmentEntityFromDomain(any(Shipment.class), any(ShipmentEntity.class));
        verify(shipmentRepository, times(1)).save(any(ShipmentEntity.class));
        verify(shipmentPostProcessService, times(1)).publishQLoggerUpdateEvents(any(), any(), any());
        verify(orderShipmentMetadataService, never()).createOrderShipmentMetaData(any(), any());
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    void createOrUpdate_existingShipmentFromOm_triggerPackageDimensionUpdateMilestone() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";
        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        OrderShipmentMetadata orderShipmentMetadata =
                new OrderShipmentMetadata(new OrganizationEntity(), new OrderEntity(), new CustomerEntity(), new AddressEntity(), new AddressEntity(), new ServiceTypeEntity(), shipmentEntity.getShipmentJourney());

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);
        when(packageDimensionService.isPackageDimensionUpdated(any(PackageDimensionEntity.class), any(PackageDimension.class)))
                .thenReturn(true);
        when(milestoneService.createPackageDimensionUpdateMilestone(any(Shipment.class)))
                .thenReturn(new Milestone());
        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);
        when(shipmentRepository.findAllByOrderId(anyString(), anyString())).thenReturn(List.of(shipmentEntity));

        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain), true)).isNotNull();

        verify(messageApi, times(1)).sendMilestoneMessage(any(Shipment.class), any(TriggeredFrom.class));
        verify(orderShipmentMetadataService, never()).createOrderShipmentMetaData(any(), any());
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    void createOrUpdate_existingShipmentFromOm_triggerOMCancelledMilestone() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";
        Organization organization = new Organization();
        organization.setId(orgId);
        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        shipmentEntity.getOrder().setStatus(Root.STATUS_CANCELLED);
        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        shipmentDomain.getOrder().setStatus(Root.STATUS_CANCELLED);
        shipmentDomain.setDeleted(true);

        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);
        when(milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(any(Shipment.class), any(MilestoneCode.class))).thenReturn(new Milestone());
        when(shipmentRepository.findAllByOrderId(anyString(), anyString())).thenReturn(List.of(shipmentEntity));
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain), true)).isNotNull();

        verify(orderShipmentMetadataService, never()).createOrderShipmentMetaData(any(), any());
        verify(qLoggerAPI, times(1)).publishShipmentCancelledEvent(any(), any());
        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(ShipmentJourney.class), any(SegmentDispatchType.class), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, never()).sendShipmentToQShip(any(Shipment.class));
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    void multipleShipmentsInOneOrder_orderStatusCancelledFromOM_cancelAllShipmentsRelatedToOrder() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";
        Organization organization = new Organization();
        organization.setId(orgId);
        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        shipmentEntity.getOrder().setStatus(Root.STATUS_CANCELLED);
        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        shipmentDomain.getOrder().setStatus(Root.STATUS_CANCELLED);
        shipmentDomain.setDeleted(true);

        when(milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(any(Shipment.class), any(MilestoneCode.class)))
                .thenReturn(new Milestone());
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);
        when(shipmentRepository.findAllByOrderId(anyString(), anyString())).thenReturn(List.of(shipmentEntity, shipmentEntity, shipmentEntity));
        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain), true)).isNotNull();

        verify(orderShipmentMetadataService, never()).createOrderShipmentMetaData(any(), any());
        verify(qLoggerAPI, times(3)).publishShipmentCancelledEvent(anyString(), any(Shipment.class));
        verify(messageApi, times(3)).sendSegmentDispatch(anyList(), any(ShipmentJourney.class), any(SegmentDispatchType.class), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, never()).sendShipmentToQShip(any(Shipment.class));
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    void deleteById_EntityFound_ShouldDeleteEntity() {
        String shipmentId = "ID0";

        doNothing().when(shipmentRepository).deleteById(shipmentId);

        shipmentService.deleteById(shipmentId);

        verify(shipmentRepository, times(1)).deleteById(shipmentId);
    }

    @Test
    void deleteById_EntityNotFound_ShouldThrowException() {
        String shipmentId = "ID0";
        String errorMessage = "Shipment Id %s not found.";
        String expectedErrorMsg = String.format(errorMessage, shipmentId);
        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenThrow(new ShipmentNotFoundException(String.format(expectedErrorMsg, shipmentId)));

        verify(shipmentRepository, never()).deleteById(any());

        assertThatThrownBy(() -> shipmentService.deleteById(shipmentId))
                .isInstanceOfSatisfying(ShipmentNotFoundException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));
    }

    @Test
    void findById_InvalidShipmentTrackingId_ShouldThrowException() {
        when(shipmentFetchService.findByIdWithFetchOrThrowException(anyString())).thenThrow(mock(ShipmentNotFoundException.class));
        assertThatThrownBy(() -> shipmentService.findByIdAndCheckLocationPermission("111"))
                .isInstanceOf(ShipmentNotFoundException.class);
    }

    @Test
    void cancelById_ShipmentAlreadyCanceled_ShouldThrowShipmentInvalidStatusException() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId("-journey-1-");
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("-segment-1-");
        segmentEntity.setRefId("1");
        segmentEntity.setSequence("1");
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);
        shipmentEntity.setStatus(ShipmentStatus.CANCELLED);

        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);

        assertThatThrownBy(() -> shipmentService.cancelById("an-id", TriggeredFrom.SHP)).isInstanceOf(ShipmentInvalidStatusException.class);
    }

    @Test
    void cancelById_allSegmentLocationsNotPermitted_shouldThrowSegmentLocationNotAllowedException() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("-shp-1-");
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId("-journey-1-");
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("-segment-1-");
        segmentEntity.setRefId("1");
        segmentEntity.setSequence("1");
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);

        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(false);

        String expectedErrorMsg = String.format(ShipmentService.ERR_CANCEL_NOT_ALLOWED, shipmentEntity.getId());
        assertThatThrownBy(() -> shipmentService.cancelById("an-id", TriggeredFrom.SHP))
                .isInstanceOfSatisfying(SegmentLocationNotAllowedException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));
    }

    @Test
    void cancelById_ShipmentIsNotYetCancelled_ShouldNotThrowException() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setStatus(ShipmentStatus.CREATED);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId("-journey-1-");
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("-segment-1-");
        segmentEntity.setRefId("1");
        segmentEntity.setSequence("1");
        segmentEntity.setStatus(SegmentStatus.COMPLETED);
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        PackageJourneySegmentEntity segmentEntity2 = new PackageJourneySegmentEntity();
        segmentEntity2.setId("-segment-2-");
        segmentEntity2.setRefId("2");
        segmentEntity2.setSequence("2");

        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity2);

        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        shipmentEntity.setOrder(new OrderEntity());

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(shipmentRepository.save(any(ShipmentEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(shipmentRepository.countShipmentJourneyIdAndIdNotAndStatusNot(shipmentJourneyEntity.getId(), shipmentEntity.getId(), ShipmentStatus.CANCELLED))
                .thenReturn(0);
        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenReturn(shipmentEntity);

        Shipment cancelledShipment = shipmentService.cancelById("an-id", TriggeredFrom.SHP);
        assertThat(cancelledShipment).isNotNull();

        assertThat(cancelledShipment.getStatus()).withFailMessage("Shipment Status not cancelled.").isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(cancelledShipment.getShipmentJourney().getStatus()).withFailMessage("Shipment Journey Status not cancelled.").isEqualTo(JourneyStatus.CANCELLED);
        assertThat(cancelledShipment.getShipmentJourney().getPackageJourneySegments().get(0).getStatus()).withFailMessage("Shipment Journey Segment 1 Status Complete should not be cancelled .").isNotEqualTo(SegmentStatus.CANCELLED);
        assertThat(cancelledShipment.getShipmentJourney().getPackageJourneySegments().get(1).getStatus()).withFailMessage("Shipment Journey Segment 2 Status Complete should not be cancelled .").isNotEqualTo(SegmentStatus.CANCELLED);
        verify(shipmentRepository, times(1)).save(any(ShipmentEntity.class));
        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
    }

    @Test
    void cancelById_ShipmentIsNotYetCancelled_ShouldNotCancelJourneyWhenOtherShipmentsAreConnectedToJourneyAndAreNotYetCancelled() {
        //GIVEN:
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("123");
        shipmentEntity.setStatus(ShipmentStatus.CREATED);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId("-journey-1-");
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("-segment-1-");
        segmentEntity.setRefId("1");
        segmentEntity.setSequence("1");
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        shipmentEntity.setOrder(new OrderEntity());

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(shipmentRepository.save(any(ShipmentEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(shipmentRepository.countShipmentJourneyIdAndIdNotAndStatusNot(shipmentJourneyEntity.getId(), shipmentEntity.getId(), ShipmentStatus.CANCELLED))
                .thenReturn(1);
        when(shipmentFetchService.findByIdOrThrowException(anyString())).thenReturn(shipmentEntity);

        //WHEN:
        Shipment cancelledShipment = shipmentService.cancelById("an-id", TriggeredFrom.SHP);

        //THEN:
        assertThat(cancelledShipment).isNotNull();
        assertThat(cancelledShipment.getStatus()).withFailMessage("Shipment Status not cancelled.").isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(cancelledShipment.getShipmentJourney().getStatus()).withFailMessage("Shipment Journey Status should not be cancelled.").isNotEqualTo(JourneyStatus.CANCELLED);
        assertThat(cancelledShipment.getShipmentJourney().getPackageJourneySegments().get(0).getStatus()).withFailMessage("Shipment Journey Segment Status should not be cancelled.").isNotEqualTo(SegmentStatus.CANCELLED);
        verify(shipmentRepository, times(1)).save(any(ShipmentEntity.class));
        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
    }

    @Test
    void findAll_ValidParam_ShouldReturnFilterResult() {

        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";

        ShipmentFilter shipmentFilter = new ShipmentFilter();
        shipmentFilter.setPageNumber(1);
        shipmentFilter.setSize(1);

        Map<LocationType, List<String>> locationTypeMap = new HashMap<>();
        locationTypeMap.put(LocationType.STATE, Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

        ShipmentCriteria shipmentCriteria = new ShipmentCriteria();
        shipmentCriteria.setPage(1);
        shipmentCriteria.setPerPage(1);
        shipmentCriteria.setUserLocationCoverageIdsByType(locationTypeMap);

        when(shipmentCriteriaMapper.mapFilterToCriteria(shipmentFilter, objectMapper, shipmentLocationCoveragePredicates)).thenReturn(shipmentCriteria);
        List<ShipmentEntity> page = List.of(dummyShipmentEntity(orderId, trackingId, orgId));
        when(shipmentRepository.count(any(ShipmentSpecification.class))).thenReturn(1L);
        when(listingPage.findAllWithPagination(any(ShipmentSpecification.class), any(Pageable.class))).thenReturn(page);
        ShipmentFilterResult shipmentFilterResult = shipmentService.findAll(shipmentFilter);

        assertThat(shipmentFilterResult).extracting(ShipmentFilterResult::totalElements).isEqualTo(1L);
        Shipment refShipment = shipmentFilterResult.getResult().get(0);
        assertThat(refShipment).isEqualTo(dummyShipment(dummyShipmentEntity(orderId, trackingId, orgId)));
        assertThat(refShipment.getMilestone()).isEqualTo(refShipment.getMilestoneEvents().get(0));
        verify(locationCoverageCriteriaEnricher, times(1)).enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
        verify(userPartnerCriteriaEnricher, times(1)).enrichCriteriaByPartners(shipmentCriteria);
    }

    @Test
    void findAll_NoUserCoverage_ShouldReturnEmptyResult() {

        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";

        ShipmentFilter shipmentFilter = new ShipmentFilter();
        shipmentFilter.setPageNumber(1);
        shipmentFilter.setSize(1);

        ShipmentCriteria shipmentCriteria = new ShipmentCriteria();
        shipmentCriteria.setPage(1);
        shipmentCriteria.setPerPage(1);

        when(shipmentCriteriaMapper.mapFilterToCriteria(shipmentFilter, objectMapper, shipmentLocationCoveragePredicates)).thenReturn(shipmentCriteria);
        List<ShipmentEntity> page = List.of(dummyShipmentEntity(orderId, trackingId, orgId));
        ShipmentFilterResult shipmentFilterResult = shipmentService.findAll(shipmentFilter);

        assertThat(shipmentFilterResult).extracting(ShipmentFilterResult::totalElements).isEqualTo(0L);
        assertThat(shipmentFilterResult.getResult()).isEmpty();
        verify(locationCoverageCriteriaEnricher, times(1)).enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
    }

    @Test
    void findActiveShipmentsWithAirSegment_noParam_shouldReturnShipmentEntityList() {
        String journeyId1 = "journey1";
        String journeyId2 = "journey2";
        List<String> refJourneyIdList = List.of(journeyId1, journeyId2);
        List<Tuple> shpObjDummy = createDummyPartialShipmentList(refJourneyIdList);
        List<PackageJourneySegment> segmentsDummy = createDummySegmentList(refJourneyIdList);

        when(shipmentRepository.findActiveShipmentsPartialFieldsWithAirSegmentAndSegmentUncachedAndSegmentNoAlert())
                .thenReturn(shpObjDummy);
        when(packageJourneySegmentService.getAllSegmentsFromShipments(anyList())).thenReturn(segmentsDummy);

        List<Shipment> resultList = shipmentService.findActiveShipmentsWithAirSegment();

        assertThat(resultList).hasSize(refJourneyIdList.size());
        assertThat(resultList.get(0).getShipmentJourney()).isNotNull();
        assertThat(resultList.get(0).getShipmentJourney().getJourneyId()).isEqualTo(journeyId1);
        assertThat(resultList.get(0).getShipmentJourney().getPackageJourneySegments()).hasSize(1);
        assertThat(resultList.get(0).getShipmentJourney().getPackageJourneySegments().get(0).getJourneyId())
                .isEqualTo(journeyId1);
        assertThat(resultList.get(1).getShipmentJourney()).isNotNull();
        assertThat(resultList.get(1).getShipmentJourney().getJourneyId()).isEqualTo(journeyId2);
        assertThat(resultList.get(1).getShipmentJourney().getPackageJourneySegments()).hasSize(1);
        assertThat(resultList.get(1).getShipmentJourney().getPackageJourneySegments().get(0).getJourneyId())
                .isEqualTo(journeyId2);
    }

    @Test
    @DisplayName("given retryable to dispatch then return true")
    void returnTrueWhenRetryableToDispatch() {

        when(milestoneService.isRetryableToDispatch(anyString()))
                .thenReturn(true);

        assertThat(shipmentService.isRetryableToDispatch("001")).isTrue();

        verify(shipmentRepository, times(0)).findById(any());
        verify(alertService, times(0)).createPickupDeliveryFailedAlert(any(ShipmentJourneyEntity.class));
    }

    @Test
    @DisplayName("given Set of Milestone Events, when Retrieved, then should return sorted Set based on CreateTime in descending order")
    void testReturnSortedSetOfMilestoneEvents() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        Set<MilestoneEntity> milestoneEvents = new HashSet<>();
        milestoneEvents.add(createMilestone(OM_BOOKED, OM_BOOKED.name(), Instant.parse("2023-03-10T14:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(DSP_ON_ROUTE_TO_PICKUP, DSP_ON_ROUTE_TO_PICKUP.name(), Instant.parse("2023-03-25T16:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(DSP_PICKUP_SUCCESSFUL, DSP_PICKUP_SUCCESSFUL.name(), Instant.parse("2023-03-15T14:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(DSP_DISPATCH_SCHEDULED, DSP_DISPATCH_SCHEDULED.name(), Instant.parse("2023-03-18T14:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(SHP_ARRIVED_AT_HUB, SHP_ARRIVED_AT_HUB.name(), Instant.parse("2023-03-25T18:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(SHP_SORTED_IN_HUB, SHP_SORTED_IN_HUB.name(), Instant.parse("2023-03-17T14:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(SHP_FLIGHT_DEPARTED, SHP_FLIGHT_DEPARTED.name(), Instant.parse("2023-03-22T15:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(SHP_FLIGHT_ARRIVED, SHP_FLIGHT_ARRIVED.name(), Instant.parse("2023-03-19T14:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION, DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION.name(), Instant.parse("2023-03-08T18:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(DSP_DELIVERY_ON_ROUTE, DSP_DELIVERY_ON_ROUTE.name(), Instant.parse("2023-03-04T14:47:56.202331500Z")));
        milestoneEvents.add(createMilestone(DSP_DELIVERY_SUCCESSFUL, DSP_DELIVERY_SUCCESSFUL.name(), Instant.parse("2023-03-12T14:47:56.202331500Z")));
        shipmentEntity.setMilestoneEvents(milestoneEvents);

        List<MilestoneEntity> result = shipmentEntity.getMilestoneEvents().stream().toList();
        assertThat(milestoneEvents).hasSameSizeAs(result);
        assertThat(Instant.parse("2023-03-25T18:47:56.202331500Z")).isEqualTo(result.get(0).getMilestoneTime());
        assertThat(Instant.parse("2023-03-25T16:47:56.202331500Z")).isEqualTo(result.get(1).getMilestoneTime());
        assertThat(Instant.parse("2023-03-22T15:47:56.202331500Z")).isEqualTo(result.get(2).getMilestoneTime());
        assertThat(Instant.parse("2023-03-19T14:47:56.202331500Z")).isEqualTo(result.get(3).getMilestoneTime());
        assertThat(Instant.parse("2023-03-18T14:47:56.202331500Z")).isEqualTo(result.get(4).getMilestoneTime());
        assertThat(Instant.parse("2023-03-17T14:47:56.202331500Z")).isEqualTo(result.get(5).getMilestoneTime());
        assertThat(Instant.parse("2023-03-15T14:47:56.202331500Z")).isEqualTo(result.get(6).getMilestoneTime());
        assertThat(Instant.parse("2023-03-12T14:47:56.202331500Z")).isEqualTo(result.get(7).getMilestoneTime());
        assertThat(Instant.parse("2023-03-10T14:47:56.202331500Z")).isEqualTo(result.get(8).getMilestoneTime());
        assertThat(Instant.parse("2023-03-08T18:47:56.202331500Z")).isEqualTo(result.get(9).getMilestoneTime());
        assertThat(Instant.parse("2023-03-04T14:47:56.202331500Z")).isEqualTo(result.get(10).getMilestoneTime());
    }

    @Test
    @DisplayName("given shipment with no hard constraints when create or update then send hasHardConstraints:false to messageApi")
    void sendFalseWhenShipmentHasNoHardConstraints() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";
        Organization organization = new Organization();
        organization.setId(orgId);

        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setType(AlertType.WARNING);
        alertEntity.setShortMessage("This is a soft constraint");
        shipmentEntity.getShipmentJourney()
                .setAlerts(List.of(alertEntity));

        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        shipmentDomain.getShipmentJourney()
                .setAlerts(List.of(new Alert("This is a soft constraint", AlertType.WARNING)));

        OrderShipmentMetadata orderShipmentMetadata =
                new OrderShipmentMetadata(new OrganizationEntity(), new OrderEntity(), new CustomerEntity(), new AddressEntity(), new AddressEntity(), new ServiceTypeEntity(), shipmentEntity.getShipmentJourney());

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(createShipmentHelper.createShipmentEntity(any(), any())).thenReturn(shipmentEntity);

        Package shipmentPackage = new Package();
        PackageDimension dims = new PackageDimension();
        shipmentPackage.setDimension(dims);
        shipmentDomain.setShipmentPackage(shipmentPackage);
        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);

        when(orderShipmentMetadataService.createOrderShipmentMetaData(any(), any())).thenReturn(orderShipmentMetadata);
        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain), true)).isNotNull();

        //call was transferred outside
        verify(shipmentPostProcessService, never()).sendUpdateToQship(any(Shipment.class));
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    @DisplayName("given shipment with hard constraints on journey when create or update then send hasHardConstraints:true to messageApi")
    void sendTrueWhenShipmentHasHardConstraintsOnJourney() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";
        Organization organization = new Organization();
        organization.setId(orgId);

        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setType(AlertType.ERROR);
        alertEntity.setShortMessage("This is a hard constraint");
        shipmentEntity.getShipmentJourney().setAlerts(List.of(alertEntity));
        OrderShipmentMetadata orderShipmentMetadata =
                new OrderShipmentMetadata(new OrganizationEntity(), new OrderEntity(), new CustomerEntity(), new AddressEntity(), new AddressEntity(), new ServiceTypeEntity(), shipmentEntity.getShipmentJourney());

        Shipment shipmentDomain = dummyShipment(shipmentEntity);
        shipmentDomain.getShipmentJourney().setAlerts(List.of(new Alert("This is a hard constraint", AlertType.ERROR)));

        when(orderShipmentMetadataService.createOrderShipmentMetaData(any(), any())).thenReturn(orderShipmentMetadata);
        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);
        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain), true)).isNotNull();

        //call was transferred outside
        verify(shipmentPostProcessService, never()).sendUpdateToQship(any(Shipment.class));
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    @Test
    @DisplayName("given shipment with hard constraints on segment when create or update then send hasHardConstraints:true to messageApi")
    void sendTrueWhenShipmentHasHardConstraintsOnSegment() {
        String orderId = "orderId";
        String trackingId = "tracking-id";
        String orgId = "org-id";
        Organization organization = new Organization();
        organization.setId(orgId);

        ShipmentEntity shipmentEntity = dummyShipmentEntity(orderId, trackingId, orgId);
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setType(AlertType.ERROR);
        alertEntity.setShortMessage("This is a hard constraint");
        shipmentEntity.getShipmentJourney().getPackageJourneySegments().get(0)
                .setAlerts(List.of(alertEntity));

        Shipment shipmentDomain1 = dummyShipment(shipmentEntity);
        shipmentDomain1.getShipmentJourney().getPackageJourneySegments().get(0)
                .setAlerts(List.of(new Alert("This is a hard constraint", AlertType.ERROR)));
        Shipment shipmentDomain2 = dummyShipment(shipmentEntity);
        shipmentDomain2.setShipmentTrackingId("shipment2");
        shipmentDomain2.setId("shipment2");
        OrderShipmentMetadata orderShipmentMetadata =
                new OrderShipmentMetadata(new OrganizationEntity(), new OrderEntity(), new CustomerEntity(), new AddressEntity(), new AddressEntity(), new ServiceTypeEntity(), shipmentEntity.getShipmentJourney());

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(organization);
        when(createShipmentHelper.createShipmentEntity(any(), any())).thenReturn(shipmentEntity);
        when(shipmentRepository.save(any())).thenReturn(shipmentEntity);

        when(orderShipmentMetadataService.createOrderShipmentMetaData(any(), any())).thenReturn(orderShipmentMetadata);
        when(shipmentJourneyService.createShipmentJourneyEntity(any(), any(), anyBoolean())).thenReturn(shipmentEntity.getShipmentJourney());

        assertThat(shipmentService.createOrUpdate(List.of(shipmentDomain1, shipmentDomain2), true)).isNotNull();

        //call was transferred outside
        verify(shipmentPostProcessService, never()).sendUpdateToQship(any(Shipment.class));
        verify(flightStatsEventService, times(1)).subscribeFlight(anyList());
    }

    private Shipment dummyShipment(ShipmentEntity shipmentEntity) {
        return dummyShipment(
                shipmentEntity.getOrder().getId(),
                shipmentEntity.getShipmentTrackingId(),
                shipmentEntity.getOrganization().getId(),
                shipmentEntity.getId()
        );
    }

    @Test
    void shouldUpdateShipmentMilestoneAdditionalInfo() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String userName = "JohnDoe";
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        infoRequest.setMilestoneTime("2023-01-01T12:00:00Z");
        List<HostedFile> requestAttachments = new ArrayList<>();
        HostedFile genericAttachment = new HostedFile(randomUUID().toString(), "file1.pdf", "url.com", "url.com", 1234L, null);
        requestAttachments.add(genericAttachment);
        HostedFile fileAttachment = new HostedFile(randomUUID().toString(), "file1.jpg", "url.com", "url.com", 1234L, null);
        requestAttachments.add(fileAttachment);
        infoRequest.setAttachments(requestAttachments);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId("1");
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", "url.com", 3245L, null);
        existingAttachments.add(existingAttachment);
        shipmentEntity.setShipmentAttachments(existingAttachments);

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);

        Milestone newMilestone = new Milestone();
        newMilestone.setMilestoneCode(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        newMilestone.setId(UUID.randomUUID().toString());
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        newMilestone.setUserName(userName);
        MilestoneAdditionalInfo milestoneAdditionalInfo = new MilestoneAdditionalInfo();
        milestoneAdditionalInfo.setRemarks(infoRequest.getNotes());
        milestoneAdditionalInfo.setAttachments(infoRequest.getAttachments());
        newMilestone.setAdditionalInfo(milestoneAdditionalInfo);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(milestoneService.getLatestMilestone(anySet())).thenReturn(previousMilestone);
        when(milestoneService.createMilestoneFromOpsUpdate(any(), any(), any())).thenReturn(newMilestone);
        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);

        ShipmentMilestoneOpsUpdateResponse response = shipmentService.updateShipmentFromOpsUpdate(infoRequest);

        assertThat(response.shipmentId()).isEqualTo(shipmentEntity.getId());
        assertThat(response.previousMilestoneName()).isEqualTo(previousMilestone.getMilestoneName());
        assertThat(response.previousMilestoneCode()).isEqualTo(previousMilestone.getMilestoneCode().toString());
        assertThat(response.currentMilestoneId()).isEqualTo(newMilestone.getId());
        assertThat(response.currentMilestoneName()).isEqualTo(newMilestone.getMilestoneName());
        assertThat(response.currentMilestoneCode()).isEqualTo(newMilestone.getMilestoneCode().toString());
        assertThat(response.milestoneTime()).isNotNull();
        assertThat(response.notes()).isEqualTo(newMilestone.getAdditionalInfo().getRemarks());
        assertThat(response.shipmentTrackingId()).isEqualTo(shipmentEntity.getShipmentTrackingId());
        assertThat(response.organizationId()).isEqualTo(shipmentEntity.getOrganization().getId());
        assertThat(response.attachments()).isEqualTo(newMilestone.getAdditionalInfo().getAttachments());
        assertThat(response.attachments()).hasSize(2);
        assertThat(response.attachments()).contains(genericAttachment).contains(fileAttachment);
        assertThat(response.attachments().get(0).getFileTimestamp()).isEqualTo(newMilestone.getAdditionalInfo().getAttachments().get(0).getFileTimestamp());
        assertThat(response.updatedBy()).isEqualTo(userName);
        assertThat(response.milestoneTime()).isNotNull();

        verify(milestoneService, times(1)).isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any());
        verify(shipmentPostProcessService, times(1)).publishShipmentUpdatedEvent(any());

        infoRequest.setAttachments(List.of(genericAttachment));
        ShipmentMilestoneOpsUpdateResponse responseWithShipmentEntityAttachmentsNull = shipmentService.updateShipmentFromOpsUpdate(infoRequest);

        assertThat(responseWithShipmentEntityAttachmentsNull.attachments()).hasSize(2);
        assertThat(responseWithShipmentEntityAttachmentsNull.attachments()).contains(genericAttachment).contains(fileAttachment);

    }

    @Test
    void shouldUpdateShipmentMilestoneAdditionalInfo_AndCreateAlert_ForFailedMilestoneCodes() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String userName = "JohnDoe";
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Pick Up Failed");
        infoRequest.setMilestoneCode("1502");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        infoRequest.setMilestoneTime("2023-01-01T12:00:00Z");

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId("1");
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);

        Milestone newMilestone = new Milestone();
        newMilestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_FAILED);
        newMilestone.setId(UUID.randomUUID().toString());
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        newMilestone.setUserName(userName);
        MilestoneAdditionalInfo milestoneAdditionalInfo = new MilestoneAdditionalInfo();
        milestoneAdditionalInfo.setRemarks(infoRequest.getNotes());
        milestoneAdditionalInfo.setAttachments(infoRequest.getAttachments());
        newMilestone.setAdditionalInfo(milestoneAdditionalInfo);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(milestoneService.getLatestMilestone(anySet())).thenReturn(previousMilestone);
        when(milestoneService.createMilestoneFromOpsUpdate(any(), any(), any())).thenReturn(newMilestone);
        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(), any())).thenReturn(true);
        when(milestoneService.isFailedStatusCode(any())).thenReturn(true);

        ShipmentMilestoneOpsUpdateResponse response = shipmentService.updateShipmentFromOpsUpdate(infoRequest);

        assertThat(response.shipmentId()).isEqualTo(shipmentEntity.getId());
        assertThat(response.previousMilestoneName()).isEqualTo(previousMilestone.getMilestoneName());
        assertThat(response.previousMilestoneCode()).isEqualTo(previousMilestone.getMilestoneCode().toString());
        assertThat(response.currentMilestoneId()).isEqualTo(newMilestone.getId());
        assertThat(response.currentMilestoneName()).isEqualTo(newMilestone.getMilestoneName());
        assertThat(response.currentMilestoneCode()).isEqualTo(newMilestone.getMilestoneCode().toString());
        assertThat(response.milestoneTime()).isNotNull();
        assertThat(response.notes()).isEqualTo(newMilestone.getAdditionalInfo().getRemarks());
        assertThat(response.shipmentTrackingId()).isEqualTo(shipmentEntity.getShipmentTrackingId());
        assertThat(response.organizationId()).isEqualTo(shipmentEntity.getOrganization().getId());
        assertThat(response.updatedBy()).isEqualTo(userName);
        assertThat(response.milestoneTime()).isNotNull();

        verify(milestoneService, times(1)).isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any());
        verify(shipmentPostProcessService, times(1)).publishShipmentUpdatedEvent(any());
        verify(alertService, times(1)).createPickupDeliveryFailedAlert(any(ShipmentJourneyEntity.class));
    }

    @Test
    void updateShipmentMilestoneAdditionalInfo_segmentLocationNotCovered_shouldThrowSegmentLocationNotAllowedException() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        infoRequest.setMilestoneTime("2023-01-01T12:00:00Z");
        List<HostedFile> requestAttachments = new ArrayList<>();
        HostedFile requestAttachment = new HostedFile(randomUUID().toString(), "file1", "url.com", "url.com", 1234L, null);
        requestAttachments.add(requestAttachment);
        infoRequest.setAttachments(requestAttachments);

        String shipmentId = "1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId(shipmentId);
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", "url.com", 3245L, null);
        existingAttachments.add(existingAttachment);
        shipmentEntity.setShipmentAttachments(existingAttachments);

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);

        Milestone newMilestone = new Milestone();
        newMilestone.setId(UUID.randomUUID().toString());
        newMilestone.setMilestoneCode(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(false);

        String expectedErrorMsg = String.format(ShipmentService.ERR_UPDATE_MILESTONE_OTHER_INFO_NOT_ALLOWED, shipmentId);
        assertThatThrownBy(() -> shipmentService.updateShipmentFromOpsUpdate(infoRequest))
                .isInstanceOfSatisfying(SegmentLocationNotAllowedException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));

        verify(qLoggerAPI, never()).publishShipmentUpdatedEvent(any(), any());
    }

    @Test
    void updateShipmentMilestoneAdditionalInfo_notAllShipmentsReceivedMilestone_shouldNotUpdateSegment() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        infoRequest.setMilestoneTime("2023-01-01T12:00:00Z");
        List<HostedFile> requestAttachments = new ArrayList<>();
        HostedFile requestAttachment = new HostedFile(randomUUID().toString(), "file1", "url.com", "url.com", 1234L, null);
        requestAttachments.add(requestAttachment);
        infoRequest.setAttachments(requestAttachments);

        String shipmentId = "1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId(shipmentId);
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", "url.com", 3245L, null);
        existingAttachments.add(existingAttachment);
        shipmentEntity.setShipmentAttachments(existingAttachments);

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());
        previousMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).minusHours(4));

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        segmentEntity.setStatus(SegmentStatus.PLANNED);
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);

        Milestone newMilestone = new Milestone();
        newMilestone.setId(UUID.randomUUID().toString());
        newMilestone.setMilestoneCode(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(milestoneService.getLatestMilestone(any())).thenReturn(previousMilestone);
        when(milestoneService.createMilestoneFromOpsUpdate(any(), any(), any())).thenReturn(newMilestone);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(Milestone.class))).thenReturn(false);

        assertThatNoException().isThrownBy(() -> shipmentService.updateShipmentFromOpsUpdate(infoRequest));

        verify(milestoneService, times(1)).isNewMilestoneAfterMostRecentMilestone(any(), any());
        verify(packageJourneySegmentService, never()).updateSegmentByMilestone(any(), anyString(), anyBoolean());
        verify(packageJourneySegmentService, never()).refreshJourneyWithUpdatedSegments(any());
        verify(messageApi, never()).sendUpdatedSegmentFromShipment(any(Shipment.class), anyString());
        verify(messageApi, times(1)).sendMilestoneMessage(any(Shipment.class), eq(TriggeredFrom.SHP));
        verify(shipmentPostProcessService, never()).sendUpdatedSegmentToDispatch(any(), any());
        verify(shipmentPostProcessService, never()).sendSingleSegmentToQship(any(), anyString());
        verify(shipmentPostProcessService, times(1)).publishShipmentUpdatedEvent(any());
        verify(notificationApi, times(1)).sendNotification(any());
    }

    @Test
    void updateShipmentMilestoneAdditionalInfo_allShipmentsReceivedMilestone_shouldUpdateSegment() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        infoRequest.setMilestoneTime("2023-01-01T12:00:00Z");
        List<HostedFile> requestAttachments = new ArrayList<>();
        HostedFile requestAttachment = new HostedFile(randomUUID().toString(), "file1", "url.com", "url.com", 1234L, null);
        requestAttachments.add(requestAttachment);
        infoRequest.setAttachments(requestAttachments);

        String shipmentId = "1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId(shipmentId);
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", "url.com", 3245L, null);
        existingAttachments.add(existingAttachment);
        shipmentEntity.setShipmentAttachments(existingAttachments);

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());
        previousMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).minusHours(4));

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        segmentEntity.setStatus(SegmentStatus.PLANNED);
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);

        Milestone newMilestone = new Milestone();
        newMilestone.setId(UUID.randomUUID().toString());
        newMilestone.setMilestoneCode(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        newMilestone.setSegmentUpdatedFromMilestone(true);

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(milestoneService.getLatestMilestone(any())).thenReturn(previousMilestone);
        when(milestoneService.createMilestoneFromOpsUpdate(any(), any(), any())).thenReturn(newMilestone);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any(Milestone.class))).thenReturn(true);

        assertThatNoException().isThrownBy(() -> shipmentService.updateShipmentFromOpsUpdate(infoRequest));

        verify(milestoneService, times(1)).updateMilestoneAndPackageJourneySegment(any(), any(), any());
        verify(packageJourneySegmentService, times(1)).refreshJourneyWithUpdatedSegments(any());
        verify(messageApi, times(1)).sendMilestoneMessage(any(Shipment.class), eq(TriggeredFrom.SHP));
        verify(shipmentPostProcessService, times(1)).sendUpdatedSegmentToDispatch(any(), any());
        verify(shipmentPostProcessService, times(1)).sendSingleSegmentToQship(any(), anyString());
        verify(shipmentPostProcessService, times(1)).publishShipmentUpdatedEvent(any());
        verify(milestonePostProcessService, times(1)).createAndSendAPIGWebhooks(any(Milestone.class), any(Shipment.class));
        verify(notificationApi, times(1)).sendNotification(any());
    }

    @Test
    void shouldUpdateShipmentMilestoneAdditionalInfo_When_GivenNonExistingShipmentTrackingId() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        infoRequest.setAttachments(List.of(new HostedFile(UUID.randomUUID().toString(), "aboitiz", "url.com", "url.com", 1234L, null)));

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenThrow(new ShipmentNotFoundException(String.format(WARN_SHIPMENT_NOT_FOUND_BY_TRACKING_ID_AND_ORGANIZATION_ID, shipmentTrackingId, organizationId)));

        assertThatThrownBy(() -> shipmentService.updateShipmentFromOpsUpdate(infoRequest))
                .isInstanceOfSatisfying(ShipmentNotFoundException.class, exception ->
                        assertThat(exception.getMessage()).contains("Shipment not found with shipment tracking id:"));
    }

    @Test
    void isShipmentAnySegmentLocationAllowed_withAllowedSegments_shouldReturnTrue() {
        ShipmentEntity shipment = new ShipmentEntity();
        String journeyId = "JOURNEY-1";
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        journey.setId(journeyId);
        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setId("SEGMENT-1");
        segment.setShipmentJourneyId(journeyId);
        segment.setRefId("1");
        segment.setSequence("1");
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class)))
                .thenReturn(true);

        assertThat(shipmentService.isShipmentAnySegmentLocationAllowed(shipment)).isTrue();
    }

    @Test
    void isShipmentAnySegmentLocationAllowed_noAllowedSegments_shouldReturnFalse() {
        ShipmentEntity shipment = new ShipmentEntity();
        String journeyId = "JOURNEY-1";
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        journey.setId(journeyId);
        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setId("SEGMENT-1");
        segment.setShipmentJourneyId(journeyId);
        segment.setRefId("1");
        segment.setSequence("1");
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class)))
                .thenReturn(false);

        assertThat(shipmentService.isShipmentAnySegmentLocationAllowed(shipment)).isFalse();
    }

    @Test
    void isShipmentAnySegmentLocationAllowed_noSegments_shouldReturnFalse() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentJourney(new ShipmentJourneyEntity());
        shipmentEntity.getShipmentJourney().addAllPackageJourneySegments(Collections.emptyList());

        assertThat(shipmentService.isShipmentAnySegmentLocationAllowed(shipmentEntity)).isFalse();
    }

    private MilestoneEntity createMilestone(MilestoneCode code, String name, Instant milestoneTime) {
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        milestoneEntity.setId(randomUUID().toString());
        milestoneEntity.setMilestoneCode(code);
        milestoneEntity.setMilestoneName(name);
        milestoneEntity.setCreateTime(Instant.now());
        milestoneEntity.setMilestoneTime(milestoneTime.toString());
        return milestoneEntity;
    }

    private List<Tuple> createDummyPartialShipmentList(List<String> journeyIds) {
        List<Tuple> partialShpList = new ArrayList<>();
        String organizationId = "ORG-001";

        for (String journeyId : journeyIds) {
            String shipmentId = UUID.randomUUID().toString();
            String shipmentTrackingId = "QC-SHP-001";
            String orderId = "ORDER-001";

            Tuple tuple = TupleDataFactory.ofShipmentFromFlightDelay(shipmentId, shipmentTrackingId, organizationId, orderId,
                    journeyId);
            partialShpList.add(tuple);
        }
        return partialShpList;
    }

    private List<PackageJourneySegment> createDummySegmentList(List<String> journeyIds) {
        List<PackageJourneySegment> segmentList = new ArrayList<>();

        for (String journeyId : journeyIds) {
            PackageJourneySegment segment = new PackageJourneySegment();
            segment.setSegmentId(UUID.randomUUID().toString());
            segment.setJourneyId(journeyId);
            segmentList.add(segment);
        }
        return segmentList;
    }

    private Shipment dummyShipment(String orderId, String trackingId, String orgId, String shipmentId) {
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentId);
        shipmentDomain.setOrder(new Order());
        shipmentDomain.getOrder().setId(orderId);
        shipmentDomain.setShipmentTrackingId(trackingId);
        shipmentDomain.setOrganization(new Organization());
        shipmentDomain.getOrganization().setId(orgId);

        shipmentDomain.setOrigin(new Address());
        shipmentDomain.setDestination(new Address());
        shipmentDomain.setServiceType(new ServiceType());
        shipmentDomain.setShipmentPackage(new Package());

        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.GROUND);
        List<PackageJourneySegment> packageJourneySegmentList = new ArrayList<>();
        packageJourneySegmentList.add(packageJourneySegment);
        shipmentJourney.setPackageJourneySegments(packageJourneySegmentList);

        shipmentDomain.setShipmentJourney(shipmentJourney);

        return shipmentDomain;
    }

    private ShipmentEntity dummyShipmentEntity(String orderId, String trackingId, String orgId) {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("an-id");
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setShipmentTrackingId(trackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(orgId);
        AddressEntity origin = new AddressEntity();
        origin.setExternalId(UUID.randomUUID().toString());
        shipmentEntity.setOrigin(origin);
        AddressEntity destination = new AddressEntity();
        destination.setExternalId(UUID.randomUUID().toString());
        shipmentEntity.setDestination(destination);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity packageJourneySegment = new PackageJourneySegmentEntity();
        packageJourneySegment.setId("segment1");
        packageJourneySegment.setTransportType(TransportType.GROUND);
        packageJourneySegment.setRefId("1");
        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegment);
        shipmentJourneyEntity.setId("shipmentJourneyEntity-id");
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        PackageEntity packageEntity = new PackageEntity();
        PackageDimensionEntity dims = new PackageDimensionEntity();
        packageEntity.setDimension(dims);
        shipmentEntity.setShipmentPackage(packageEntity);
        shipmentEntity.setServiceType(new ServiceTypeEntity());
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        milestoneEntity.setMilestoneCode(OM_BOOKED);
        milestoneEntity.setCreateTime(Instant.now());
        shipmentEntity.setMilestoneEvents(Set.of(milestoneEntity));
        return shipmentEntity;
    }

    @Test
    void testReceiveMilestoneMessageFromDispatch_Success() {
        //GIVEN:
        String dspPayload = "mockPayload";
        String uuid = "mockUUID";

        Milestone milestone = new Milestone();
        milestone.setShipmentId("402861228832ba41018832c6ea210051");
        milestone.setMilestoneTime(OffsetDateTime.now().plusWeeks(2));
        when(milestoneService.convertAndValidateMilestoneFromDispatch(dspPayload, uuid)).thenReturn(milestone);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId("402861228832ba41018832c6ea210051")).thenReturn(OffsetDateTime.now());
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)).thenReturn(true);
        //WHEN:
        Milestone result = shipmentService.receiveMilestoneMessageFromDispatch(dspPayload, uuid);

        //THEN:
        verify(milestoneService, times(1)).convertAndValidateMilestoneFromDispatch(dspPayload, uuid);
        verify(milestoneService, times(1)).getMostRecentMilestoneTimeByShipmentId(anyString());
        verify(milestoneService, times(1))
                .updateMilestoneAndPackageJourneySegment(any(Milestone.class), anyString(), any(OffsetDateTime.class));

        // Verify that messageApi.sendDispatchMilestoneError() was not called
        verify(messageApi, never()).sendDispatchMilestoneError(any(MilestoneError.class));
        // Verify the returned milestone
        assertThat(result).isEqualTo(milestone);
    }

    @Test
    void testReceiveMilestoneMessageFromDispatch_ConstraintViolationException() {
        String dspPayload = "mockPayload";
        String uuid = "mockUUID";

        // Mocking the ConstraintViolationException
        ConstraintViolationException mockException = mock(ConstraintViolationException.class);
        when(mockException.getConstraintViolations()).thenReturn(Set.of(mock(ConstraintViolation.class)));
        when(milestoneService.getDispatchMessageJson(dspPayload)).thenReturn(mock(JsonNode.class));

        // Mocking the milestone returned by milestoneService.saveMilestoneFromDispatch()
        when(milestoneService.convertAndValidateMilestoneFromDispatch(dspPayload, uuid)).thenThrow(mockException);

        // Invoke the method
        Milestone result = shipmentService.receiveMilestoneMessageFromDispatch(dspPayload, uuid);

        // Verify that milestoneService.saveMilestoneFromDispatch() was called
        verify(milestoneService, times(1)).convertAndValidateMilestoneFromDispatch(dspPayload, uuid);

        // Verify that the packageJourneySegmentService methods were not called
        verify(milestoneService, never()).updateMilestoneAndPackageJourneySegment(any(), anyString(), any());
        verify(packageJourneySegmentService, never()).updateSegmentDriverAndVehicleFromMilestone(any(), any());
        verify(milestoneHubLocationHandler, never()).enrichMilestoneHubIdWithLocationIds(any());
        // Verify that messageApi.sendDispatchMilestoneError() was called with the expected error
        verify(messageApi, times(1)).sendDispatchMilestoneError(any());
        // Verify the returned result is null
        assertThat(result).isNull();
    }

    @Test
    void testReceiveMilestoneMessageFromDispatch_isNewMilestoneAfterMostRecentMilestone() {
        // Invoke the method
        String dspPayload = "mockPayload";
        String uuid = "mockUUID";
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setShipmentId("402861228832ba41018832c6ea210051");
        when(milestoneService.convertAndValidateMilestoneFromDispatch(dspPayload, uuid)).thenReturn(milestone);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId("402861228832ba41018832c6ea210051")).thenReturn(OffsetDateTime.now().minusMonths(2));
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)).thenReturn(true);

        // Invoke the method
        Milestone result = shipmentService.receiveMilestoneMessageFromDispatch(dspPayload, uuid);

        // Verify that milestoneService.createMilestone() was called
        verify(milestoneService, times(1)).createMilestone(milestone);

        // should call getMostRecentMilestoneTimeByShipmentId before createMilestone or else it will get the milestone time of the new milestone
        InOrder inOrder = inOrder(milestoneService);
        inOrder.verify(milestoneService).getMostRecentMilestoneTimeByShipmentId("402861228832ba41018832c6ea210051");
        inOrder.verify(milestoneService).createMilestone(milestone);

        verify(milestoneHubLocationHandler, times(1)).enrichMilestoneHubIdWithLocationIds(milestone);
        verify(milestoneService, times(1)).getMostRecentMilestoneTimeByShipmentId(anyString());
        verify(milestoneService, times(1))
                .updateMilestoneAndPackageJourneySegment(any(Milestone.class), anyString(), any(OffsetDateTime.class));
        verify(milestoneTimezoneHelper, times(1)).supplyMilestoneTimezoneFromHubTimezone(any(Milestone.class));
        verify(milestoneTimezoneHelper, times(1)).supplyEtaAndProofOfDeliveryTimezoneFromHubTimezone(any(Milestone.class));

        assertThat(result).isNotNull();
    }

    @Test
    void testReceiveMilestoneMessageFromDispatch_isNewMilestoneBeforeMostRecentMilestone() {
        // Invoke the method
        String dspPayload = "mockPayload";
        String uuid = "mockUUID";
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setShipmentId("402861228832ba41018832c6ea210051");
        when(milestoneService.convertAndValidateMilestoneFromDispatch(dspPayload, uuid)).thenReturn(milestone);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId("402861228832ba41018832c6ea210051")).thenReturn(OffsetDateTime.now().plusMonths(2));
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)).thenReturn(true);

        // Invoke the method
        Milestone result = shipmentService.receiveMilestoneMessageFromDispatch(dspPayload, uuid);

        // Verify that milestoneService.createMilestone() was called
        verify(milestoneService, times(1)).createMilestone(milestone);

        // Verify that the packageJourneySegmentService methods were not called
        verify(packageJourneySegmentService, never()).updateSegmentStatusByMilestoneEvent(any(), anyString());

        verify(milestoneService, times(1)).getMostRecentMilestoneTimeByShipmentId(anyString());
        verify(milestoneService, times(1))
                .updateMilestoneAndPackageJourneySegment(any(Milestone.class), anyString(), any(OffsetDateTime.class));

        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdateMilestoneAndPjs_whenIsAllShipmentFromOrderHaveSameMilestoneIsTrue() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String userName = "JohnDoe";
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        List<HostedFile> requestAttachments = new ArrayList<>();
        HostedFile requestAttachment = new HostedFile(randomUUID().toString(), "file1", "url.com", "url.com", 1234L, null);
        requestAttachments.add(requestAttachment);
        infoRequest.setAttachments(requestAttachments);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId("1");
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", "url.com", 3245L, null);
        existingAttachments.add(existingAttachment);
        shipmentEntity.setShipmentAttachments(existingAttachments);

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());
        previousMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).minusHours(4));

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        segmentEntity.setStatus(SegmentStatus.IN_PROGRESS);
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);
        Milestone newMilestone = new Milestone();
        newMilestone.setMilestoneCode(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        newMilestone.setUserName(userName);
        newMilestone.setSegmentUpdatedFromMilestone(true);

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(milestoneService.getLatestMilestone(anySet())).thenReturn(previousMilestone);
        when(milestoneService.createMilestoneFromOpsUpdate(any(), any(), any())).thenReturn(newMilestone);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any())).thenReturn(true);

        //THEN:
        assertThat(shipmentService.updateShipmentFromOpsUpdate(infoRequest)).isNotNull();
        verify(milestoneService, times(1)).updateMilestoneAndPackageJourneySegment(eq(newMilestone), any(), eq(previousMilestone.getMilestoneTime()));
        verify(packageJourneySegmentService, times(1)).refreshJourneyWithUpdatedSegments(any());
        verify(shipmentPostProcessService, times(1)).sendUpdatedSegmentToDispatch(eq(newMilestone), any());
        verify(shipmentPostProcessService, times(1)).sendSingleSegmentToQship(any(), eq("segmentId"));
        verify(shipmentPostProcessService, times(1)).publishShipmentUpdatedEvent(any());
        verify(notificationApi, times(1)).sendNotification(any());
    }

    @Test
    void shouldNotUpdateMilestoneAndPjs_whenIsAllShipmentFromOrderHaveSameMilestoneIsFalse() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String userName = "JohnDoe";
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        List<HostedFile> requestAttachments = new ArrayList<>();
        HostedFile requestAttachment = new HostedFile(randomUUID().toString(), "file1", "url.com", "url.com", 1234L, null);
        requestAttachments.add(requestAttachment);
        infoRequest.setAttachments(requestAttachments);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId("1");
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", "url.com", 3245L, null);
        existingAttachments.add(existingAttachment);
        shipmentEntity.setShipmentAttachments(existingAttachments);

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());
        previousMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).minusHours(4));

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        segmentEntity.setStatus(SegmentStatus.IN_PROGRESS);
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);
        Milestone newMilestone = new Milestone();
        newMilestone.setMilestoneCode(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        newMilestone.setUserName(userName);
        newMilestone.setSegmentUpdatedFromMilestone(true);

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(milestoneService.getLatestMilestone(anySet())).thenReturn(previousMilestone);
        when(milestoneService.createMilestoneFromOpsUpdate(any(), any(), any())).thenReturn(newMilestone);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(any())).thenReturn(false);

        //THEN:
        assertThat(shipmentService.updateShipmentFromOpsUpdate(infoRequest)).isNotNull();
        verify(milestoneService, never()).updateMilestoneAndPackageJourneySegment(eq(newMilestone), any(), eq(previousMilestone.getMilestoneTime()));
        verify(packageJourneySegmentService, never()).refreshJourneyWithUpdatedSegments(any());
        verify(shipmentPostProcessService, never()).sendUpdatedSegmentToDispatch(eq(newMilestone), any());
        verify(shipmentPostProcessService, never()).sendSingleSegmentToQship(any(), eq("segmentId"));
        verify(shipmentPostProcessService, times(1)).publishShipmentUpdatedEvent(any());
        verify(notificationApi, times(1)).sendNotification(any());
    }

    @Test
    void shouldUpdateShipmentMilestoneAdditionalInfo_isNewMilestoneBeforeMostRecentMilestone() {
        String organizationId = UUID.randomUUID().toString();
        String shipmentTrackingId = UUID.randomUUID().toString();
        String userName = "JohnDoe";
        String orderId = UUID.randomUUID().toString();
        ShipmentMilestoneOpsUpdateRequest infoRequest = new ShipmentMilestoneOpsUpdateRequest();
        infoRequest.setMilestoneName("Arrived in Hub");
        infoRequest.setMilestoneCode("1509");
        infoRequest.setShipmentTrackingId(shipmentTrackingId);
        infoRequest.setNotes("This is a note");
        List<HostedFile> requestAttachments = new ArrayList<>();
        HostedFile requestAttachment = new HostedFile(randomUUID().toString(), "file1", "url.com", "url.com", 1234L, null);
        requestAttachments.add(requestAttachment);
        infoRequest.setAttachments(requestAttachments);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        shipmentEntity.setOrder(order);
        shipmentEntity.setId("1");
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        shipmentEntity.setOrganization(new OrganizationEntity());
        shipmentEntity.getOrganization().setId(organizationId);
        shipmentEntity.setNotes(infoRequest.getNotes());
        List<HostedFile> existingAttachments = new ArrayList<>();
        HostedFile existingAttachment = new HostedFile(randomUUID().toString(), "file2", "url.com", "url.com", 3245L, null);
        existingAttachments.add(existingAttachment);
        shipmentEntity.setShipmentAttachments(existingAttachments);

        MilestoneEntity previousMilestoneEntity = new MilestoneEntity();
        previousMilestoneEntity.setId("0001");
        previousMilestoneEntity.setMilestoneCode(OM_BOOKED);
        previousMilestoneEntity.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(previousMilestoneEntity));

        Milestone previousMilestone = new Milestone();
        previousMilestone.setId(previousMilestoneEntity.getId());
        previousMilestone.setMilestoneCode(previousMilestoneEntity.getMilestoneCode());
        previousMilestone.setMilestoneName(previousMilestoneEntity.getMilestoneName());

        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segmentId");
        segmentEntity.setStatus(SegmentStatus.IN_PROGRESS);
        journeyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(journeyEntity);
        Milestone newMilestone = new Milestone();
        newMilestone.setMilestoneCode(DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION);
        newMilestone.setMilestoneName("MILESTONE-DUMMY");
        newMilestone.setShipmentId(shipmentEntity.getId());
        newMilestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        newMilestone.setUserName(userName);

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);
        when(milestoneService.getLatestMilestone(anySet())).thenReturn(previousMilestone);
        when(milestoneService.createMilestoneFromOpsUpdate(any(), any(), any())).thenReturn(newMilestone);

        assertThat(shipmentService.updateShipmentFromOpsUpdate(infoRequest)).isNotNull();

        verify(packageJourneySegmentService, never()).updateSegmentStatusByMilestoneEvent(newMilestone, null);
    }

    @Test
    void findByShipmentTrackingIdAndCheckLocationPermission_EntityFound_ShouldReturnDomain() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("SHP-ID1");
        shipmentEntity.setUserId("USR1");
        String orderId = "order-id-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        shipmentEntity.setOrder(orderEntity);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(true);

        Shipment shipmentDomainResult = shipmentService.findByShipmentTrackingIdAndCheckLocationPermission("ShipmentTrackingId1");

        assertThat(shipmentDomainResult.getId()).isEqualTo(shipmentDomain.getId());
        assertThat(shipmentDomainResult.getUserId()).isEqualTo(shipmentDomain.getUserId());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentPackageJourneySegmentsWithInstructions(any());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentJourneyAndSegmentWithAlert(any());
        verify(packageJourneySegmentService, times(1)).enrichSegmentsWithOrderInstructions(eq(orderId), anyList());
    }

    @Test
    void findByShipmentTrackingIdAndCheckLocationPermission_EntityNotFound_ShouldThrowException() {
        String shipmentTrackingId = "ShipmentTrackingId1";
        String errorMessage = "Shipment Id %s not found.";
        String expectedErrorMsg = String.format(errorMessage, shipmentTrackingId);
        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString()))
                .thenThrow(new ShipmentNotFoundException(String.format(errorMessage, shipmentTrackingId)));

        assertThatThrownBy(() -> shipmentService.findByShipmentTrackingIdAndCheckLocationPermission(shipmentTrackingId))
                .isInstanceOfSatisfying(ShipmentNotFoundException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));
    }

    @Test
    void findByShipmentTrackingIdAndCheckLocationPermission_allSegmentLocationsNotPermitted_ShouldThrowException() {
        String shipmentTrackingId = "ShipmentTrackingId1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentTrackingId);
        shipmentEntity.setUserId("USR1");
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setCountry(new LocationEntity());
        locationHierarchyEntity.setState(new LocationEntity());
        locationHierarchyEntity.setCity(new LocationEntity());
        locationHierarchyEntity.setFacility(new LocationEntity());
        segmentEntity.setStartLocationHierarchy(locationHierarchyEntity);
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(false);

        String expectedErrorMsg = String.format(ShipmentService.ERR_READ_NOT_ALLOWED, shipmentTrackingId);
        assertThatThrownBy(() -> shipmentService.findByShipmentTrackingIdAndCheckLocationPermission(shipmentTrackingId))
                .isInstanceOfSatisfying(SegmentLocationNotAllowedException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));
        verify(shipmentEnrichmentService, times(1)).enrichShipmentPackageJourneySegmentsWithInstructions(any());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentJourneyAndSegmentWithAlert(any());
    }

    @Test
    void findByShipmentTrackingIdAndCheckLocationPermission_SingleSegmentWithNullFacility_ShouldReturnDomain() {
        String shipmentTrackingId = "ShipmentTrackingId1";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentTrackingId);
        shipmentEntity.setUserId("USR1");
        String orderId = "order-id-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        shipmentEntity.setOrder(orderEntity);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipmentDomain.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByShipmentTrackingIdOrThrowException(anyString())).thenReturn(shipmentEntity);
        when(packageJourneySegmentService.isSegmentAllFacilitiesAllowed(any(PackageJourneySegmentEntity.class))).thenReturn(false);

        shipmentService.findByShipmentTrackingIdAndCheckLocationPermission(shipmentTrackingId);
        verify(shipmentEnrichmentService, times(1)).enrichShipmentPackageJourneySegmentsWithInstructions(any());
        verify(shipmentEnrichmentService, times(1)).enrichShipmentJourneyAndSegmentWithAlert(any());
        verify(packageJourneySegmentService, times(1)).enrichSegmentsWithOrderInstructions(eq(orderId), anyList());
    }

    @Test
    void receiveMilestoneMessageFromDispatch_someShipmentReceivedMilestone_shouldNotUpdateSegment() {
        String dspPayload = "mockPayload";
        String uuid = "mockUUID";

        Milestone milestone = new Milestone();
        milestone.setShipmentId("shipment1");
        milestone.setMilestoneTime(OffsetDateTime.now().plusWeeks(2));
        when(milestoneService.convertAndValidateMilestoneFromDispatch(dspPayload, uuid)).thenReturn(milestone);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)).thenReturn(false);

        Milestone result = shipmentService.receiveMilestoneMessageFromDispatch(dspPayload, uuid);

        assertThat(result).isNotNull();
        assertThat(result.isSegmentUpdatedFromMilestone()).isFalse();

        verify(milestoneService, never())
                .updateMilestoneAndPackageJourneySegment(any(Milestone.class), anyString(), any(OffsetDateTime.class));
    }
}