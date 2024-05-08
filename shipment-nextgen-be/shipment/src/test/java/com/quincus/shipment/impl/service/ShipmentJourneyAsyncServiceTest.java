package com.quincus.shipment.impl.service;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentJourneyContext;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentJourneyAsyncServiceTest {

    private static final String GIVEN_SHIPMENT_ID = "SHP-ID";
    private static final String GIVEN_JOURNEY_ID = "SHP-JOURNEY-ID";
    private static final String GIVEN_ORG_ID = "ORG-ID";
    private static final String GIVEN_ORDER_ID = "ORDER-ID";

    @InjectMocks
    private ShipmentJourneyAsyncService shipmentJourneyAsyncService;
    @Mock
    private QLoggerAPI qLoggerAPI;
    @Mock
    private MessageApi messageApi;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private ApiGatewayApi apiGatewayApi;

    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private ShipmentPostProcessService shipmentPostProcessService;

    @Test
    void givenShipmentJourneyUpdates_thenSendUpdatesToThirdparty() {

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        PackageJourneySegmentEntity segmentEntity = TestDataFactory.createPackageJourneySegmentEntity("1", "1");
        ShipmentJourneyEntity shipmentJourneyEntity = TestDataFactory.createShipmentJourneyEntity(GIVEN_JOURNEY_ID, segmentEntity);

        ShipmentEntity shipmentEntity1 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, UUID.randomUUID().toString());
        ShipmentEntity shipmentEntity2 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, UUID.randomUUID().toString());
        ShipmentJourney previousShipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);
        ShipmentJourney updatedShipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);

        //WHEN:
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(GIVEN_ORG_ID);
        when(shipmentRepository.findByIdAfterJourneyUpdate(shipmentEntity1.getId(), GIVEN_ORG_ID)).thenReturn(Optional.of(shipmentEntity1));
        when(shipmentRepository.findByIdAfterJourneyUpdate(shipmentEntity2.getId(), GIVEN_ORG_ID)).thenReturn(Optional.of(shipmentEntity2));

        shipmentJourneyAsyncService.sendShipmentJourneyUpdates(List.of(shipmentEntity1.getId(), shipmentEntity2.getId()),
                previousShipmentJourney, updatedShipmentJourney);

        //sending data to thirdParty would send all shipment that are related to the updated shipmentJourney
        verify(qLoggerAPI, times(2)).publishShipmentJourneyUpdatedEventWithRetry(any(), any(), any(), any());
        verify(shipmentPostProcessService, times(1)).sendJourneyUpdateToDispatch(anyList(), any());
        verify(shipmentPostProcessService, times(1)).sendUpdateToQship(any(), any());
    }

    @Test
    void sendShipmentJourneyUpdates_segmentContextArgument_thenSendUpdatesToThirdparty() {

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(GIVEN_ORG_ID);

        PackageJourneySegmentEntity segmentEntity = TestDataFactory.createPackageJourneySegmentEntity("1", "1");
        ShipmentJourneyEntity shipmentJourneyEntity = TestDataFactory.createShipmentJourneyEntity(GIVEN_JOURNEY_ID, segmentEntity);

        ShipmentEntity shipmentEntity1 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, UUID.randomUUID().toString());
        ShipmentEntity shipmentEntity2 = TestDataFactory.createShipmentEntity(shipmentJourneyEntity, organizationEntity, UUID.randomUUID().toString());
        ShipmentJourney previousShipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);
        ShipmentJourney updatedShipmentJourney = TestDataFactory.createShipmentJourney(GIVEN_SHIPMENT_ID, GIVEN_JOURNEY_ID, GIVEN_ORDER_ID, JourneyStatus.PLANNED);

        PackageJourneySegment addedSegment = new PackageJourneySegment();
        addedSegment.setSegmentId("segment-A");
        addedSegment.setJourneyId(updatedShipmentJourney.getJourneyId());
        addedSegment.setRefId("2");
        addedSegment.setSequence("1");
        addedSegment.setNewlyCreated(true);
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        updatedSegment.setSegmentId("segment-B");
        updatedSegment.setJourneyId(updatedShipmentJourney.getJourneyId());
        updatedSegment.setRefId("0");
        updatedSegment.setSequence("0");
        PackageJourneySegment deletedSegment = new PackageJourneySegment();
        deletedSegment.setSegmentId("segment-C");
        deletedSegment.setJourneyId(updatedShipmentJourney.getJourneyId());
        deletedSegment.setRefId("1");
        deletedSegment.setSequence("1");
        deletedSegment.setDeleted(true);
        List<PackageJourneySegment> segments = new ArrayList<>(List.of(addedSegment, updatedSegment, deletedSegment));
        updatedShipmentJourney.setPackageJourneySegments(segments);

        List<String> shipmentIdList = List.of(shipmentEntity1.getId(), shipmentEntity2.getId());
        List<String> shipmentTrackingIdList = List.of(shipmentEntity1.getShipmentTrackingId(), shipmentEntity2.getShipmentTrackingId());
        ShipmentJourneyContext shipmentJourneyContext = new ShipmentJourneyContext()
                .previousShipmentJourney(previousShipmentJourney)
                .updatedShipmentJourney(updatedShipmentJourney)
                .shipmentIds(shipmentIdList)
                .shipmentTrackingIds(shipmentTrackingIdList);

        //WHEN:
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(GIVEN_ORG_ID);
        when(shipmentRepository.findByIdAfterJourneyUpdate(shipmentEntity1.getId(), GIVEN_ORG_ID)).thenReturn(Optional.of(shipmentEntity1));
        when(shipmentRepository.findByIdAfterJourneyUpdate(shipmentEntity2.getId(), GIVEN_ORG_ID)).thenReturn(Optional.of(shipmentEntity2));

        shipmentJourneyAsyncService.sendShipmentJourneyUpdates(shipmentJourneyContext);

        //sending data to thirdParty would send all shipment that are related to the updated shipmentJourney
        verify(qLoggerAPI, times(2)).publishShipmentJourneyUpdatedEventWithRetry(any(), any(), any(), any());
        verify(shipmentPostProcessService, times(1)).sendJourneyUpdateToDispatch(anyList(), any());
        verify(shipmentPostProcessService, times(3)).sendSingleSegmentToQship(any(), any(PackageJourneySegment.class));
    }

    @Test
    void sendUpdateToApiGIfPartnerHasChanged_whenPartnerHasChanged_shouldCallApiG() {
        ShipmentJourney previousJourney = new ShipmentJourney();
        Partner previousPartner = new Partner();
        previousPartner.setId("PREV-PARTNER-ID");
        PackageJourneySegment previousSegment = new PackageJourneySegment();
        previousSegment.setRefId("REF-ID");
        previousSegment.setPartner(previousPartner);
        previousJourney.setPackageJourneySegments(List.of(previousSegment));

        ShipmentJourney updatedJourney = new ShipmentJourney();
        Partner currentPartner = new Partner();
        currentPartner.setId("CURRENT-PARTNER-ID");
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        updatedSegment.setRefId("REF-ID");
        updatedSegment.setPartner(currentPartner);
        updatedJourney.setPackageJourneySegments(List.of(updatedSegment));
        updatedJourney.setShipmentId(GIVEN_SHIPMENT_ID);

        Shipment shipment = new Shipment();

        shipmentJourneyAsyncService.sendUpdateToApiGIfPartnerHasChanged(shipment, previousJourney, updatedJourney);

        verify(apiGatewayApi, times(1)).sendAssignVendorDetailsWithRetry(shipment, updatedSegment);
    }

    @Test
    void sendUpdateToApiGIfPartnerHasChanged_whenPartnerHasNotChanged_shouldNotCallApiG() {
        ShipmentJourney previousJourney = new ShipmentJourney();
        Partner previousPartner = new Partner();
        previousPartner.setId("CURRENT-PARTNER-ID");
        PackageJourneySegment previousSegment = new PackageJourneySegment();
        previousSegment.setRefId("REF-ID");
        previousSegment.setPartner(previousPartner);
        previousJourney.setPackageJourneySegments(List.of(previousSegment));

        ShipmentJourney updatedJourney = new ShipmentJourney();
        Partner currentPartner = new Partner();
        currentPartner.setId("CURRENT-PARTNER-ID");
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        updatedSegment.setRefId("REF-ID");
        updatedSegment.setPartner(currentPartner);
        updatedJourney.setPackageJourneySegments(List.of(updatedSegment));
        updatedJourney.setShipmentId(GIVEN_SHIPMENT_ID);

        Shipment shipment = new Shipment();

        shipmentJourneyAsyncService.sendUpdateToApiGIfPartnerHasChanged(shipment, previousJourney, updatedJourney);

        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(shipment, updatedSegment);
    }

    @Test
    void sendUpdateToApiGIfPartnerHasChanged_whenNewPartnerFromNone_shouldCallApiG() {
        ShipmentJourney previousJourney = new ShipmentJourney();
        previousJourney.setPackageJourneySegments(Collections.emptyList());

        ShipmentJourney updatedJourney = new ShipmentJourney();
        Partner currentPartner = new Partner();
        currentPartner.setId("CURRENT-PARTNER-ID");
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        updatedSegment.setRefId("REF-ID");
        updatedSegment.setPartner(currentPartner);
        updatedJourney.setPackageJourneySegments(List.of(updatedSegment));
        updatedJourney.setShipmentId(GIVEN_SHIPMENT_ID);

        Shipment shipment = new Shipment();

        shipmentJourneyAsyncService.sendUpdateToApiGIfPartnerHasChanged(shipment, previousJourney, updatedJourney);

        verify(apiGatewayApi, times(1)).sendAssignVendorDetailsWithRetry(shipment, updatedSegment);
    }

    @Test
    void sendUpdateToApiGIfPartnerHasChanged_whenNewPartnerFromNoPartner_shouldCallApiG() {
        ShipmentJourney previousJourney = new ShipmentJourney();
        PackageJourneySegment previousSegment = new PackageJourneySegment();
        previousSegment.setRefId("REF-ID");
        previousJourney.setPackageJourneySegments(List.of(previousSegment));

        ShipmentJourney updatedJourney = new ShipmentJourney();
        Partner currentPartner = new Partner();
        currentPartner.setId("CURRENT-PARTNER-ID");
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        updatedSegment.setRefId("REF-ID");
        updatedSegment.setPartner(currentPartner);
        updatedJourney.setPackageJourneySegments(List.of(updatedSegment));
        updatedJourney.setShipmentId(GIVEN_SHIPMENT_ID);

        Shipment shipment = new Shipment();

        shipmentJourneyAsyncService.sendUpdateToApiGIfPartnerHasChanged(shipment, previousJourney, updatedJourney);

        verify(apiGatewayApi, times(1)).sendAssignVendorDetailsWithRetry(shipment, updatedSegment);
    }

    @Test
    void sendUpdateToApiGIfPartnerHasChanged_whenPartnerRemoved_invalidShouldNotCallApiG() {
        ShipmentJourney previousJourney = new ShipmentJourney();
        Partner previousPartner = new Partner();
        previousPartner.setId("PREV-PARTNER-ID");
        PackageJourneySegment previousSegment = new PackageJourneySegment();
        previousSegment.setRefId("REF-ID");
        previousSegment.setPartner(previousPartner);
        previousJourney.setPackageJourneySegments(List.of(previousSegment));

        ShipmentJourney updatedJourney = new ShipmentJourney();
        PackageJourneySegment updatedSegment = new PackageJourneySegment();
        updatedSegment.setRefId("REF-ID");
        updatedJourney.setPackageJourneySegments(List.of(updatedSegment));
        updatedJourney.setShipmentId(GIVEN_SHIPMENT_ID);

        Shipment shipment = new Shipment();

        shipmentJourneyAsyncService.sendUpdateToApiGIfPartnerHasChanged(shipment, previousJourney, updatedJourney);

        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(shipment, updatedSegment);
    }
}
