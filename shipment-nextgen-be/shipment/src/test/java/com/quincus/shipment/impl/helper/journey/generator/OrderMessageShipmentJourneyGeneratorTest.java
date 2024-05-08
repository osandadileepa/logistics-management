package com.quincus.shipment.impl.helper.journey.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.order.api.domain.Root;
import com.quincus.order.api.domain.SegmentsPayload;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.config.ShipmentJourneyCreationProperties;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentTypeAssigner;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMessageShipmentJourneyGeneratorTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private OrderMessageShipmentJourneyGenerator orderMessageJourneyGenerator;
    @Mock
    private PackageJourneySegmentTypeAssigner packageJourneySegmentTypeAssigner;
    @Mock
    private ShipmentJourneyCreationProperties shipmentJourneyCreationProperties;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    List<String> ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION = List.of("UPSAPAC");

    @Test
    void mapOrderMessageToJourney_orderWithMultipleSegments_shouldMapFacilityDetailsToJourney() throws JsonProcessingException {
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("SHPV2");
        currentOrganization.setId("SHPV2_ID");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);

        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(data.toString(), data.get("is_segment").toString());
        Root root = testUtil.createRootFromOM(data.toString());
        root.setSegmentsPayloads(segments);

        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(segmentDomainList).hasSize(5);
        Facility facilityS1 = segmentDomainList.get(0).getStartFacility();
        assertThat(facilityS1).isNotNull();
        assertThat(facilityS1.getExternalId()).isEqualTo(root.getOrigin().getId());
        assertThat(facilityS1.getName()).isEqualTo(Shipment.ORIGIN_PROPERTY_NAME);
        assertThat(facilityS1.getCode()).isEqualTo(Shipment.ORIGIN_PROPERTY_NAME);
        Facility facilityE1 = segmentDomainList.get(0).getEndFacility();
        assertThat(facilityE1).isNotNull();
        assertThat(facilityE1.getExternalId()).isEqualTo(segments.get(0).getDropOffFacilityId());
        assertThat(facilityE1.getName()).isEqualTo(segments.get(0).getDropOffFacilityId());
        assertThat(facilityE1.getCode()).isEqualTo(segments.get(0).getDropOffFacilityId());
        Facility facilityS2 = segmentDomainList.get(1).getStartFacility();
        assertThat(facilityS2).isNotNull();
        assertThat(facilityS2.getExternalId()).isEqualTo(segments.get(1).getPickUpFacilityId());
        assertThat(facilityS2.getName()).isEqualTo(segments.get(1).getPickUpFacilityId());
        Facility facilityE2 = segmentDomainList.get(1).getEndFacility();
        assertThat(facilityE2).isNotNull();
        assertThat(facilityE2.getExternalId()).isEqualTo(segments.get(1).getDropOffFacilityId());
        assertThat(facilityE2.getName()).isEqualTo(segments.get(1).getDropOffFacilityId());
        Facility facilityS5 = segmentDomainList.get(4).getStartFacility();
        assertThat(facilityS5).isNotNull();
        assertThat(facilityS5.getExternalId()).isEqualTo(segments.get(4).getPickUpFacilityId());
        assertThat(facilityS5.getName()).isEqualTo(segments.get(4).getPickUpFacilityId());
        assertThat(facilityS5.getCode()).isEqualTo(segments.get(4).getPickUpFacilityId());
        Facility facilityE5 = segmentDomainList.get(4).getEndFacility();
        assertThat(facilityE5).isNotNull();
        assertThat(facilityE5.getExternalId()).isEqualTo(root.getDestination().getId());
        assertThat(facilityE5.getName()).isEqualTo(Shipment.DESTINATION_PROPERTY_NAME);
        assertThat(facilityE5.getCode()).isEqualTo(Shipment.DESTINATION_PROPERTY_NAME);
    }

    @Test
    void mapOrderMessageToJourney_blankFacilitiesForOriginAndDestination_shouldMapFacilityDetailsToJourney() throws JsonProcessingException {
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("SHPV2");
        currentOrganization.setId("SHPV2_ID");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);

        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(data.toString(), data.get("is_segment").toString());
        segments.get(0).setPickUpFacilityId("");
        segments.get(segments.size() - 1).setDropOffFacilityId("");
        Root root = testUtil.createRootFromOM(data.toString());
        root.setSegmentsPayloads(segments);

        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(segmentDomainList).hasSize(5);
        Facility facilityS1 = segmentDomainList.get(0).getStartFacility();
        assertThat(facilityS1).isNotNull();
        assertThat(facilityS1.getExternalId()).isEqualTo(root.getOrigin().getId());
        assertThat(facilityS1.getName()).isEqualTo(Shipment.ORIGIN_PROPERTY_NAME);
        assertThat(facilityS1.getCode()).isEqualTo(Shipment.ORIGIN_PROPERTY_NAME);
        Facility facilityE1 = segmentDomainList.get(0).getEndFacility();
        assertThat(facilityE1).isNotNull();
        assertThat(facilityE1.getExternalId()).isEqualTo(segments.get(0).getDropOffFacilityId());
        assertThat(facilityE1.getName()).isEqualTo(segments.get(0).getDropOffFacilityId());
        assertThat(facilityE1.getCode()).isEqualTo(segments.get(0).getDropOffFacilityId());
        Facility facilityS2 = segmentDomainList.get(1).getStartFacility();
        assertThat(facilityS2).isNotNull();
        assertThat(facilityS2.getExternalId()).isEqualTo(segments.get(1).getPickUpFacilityId());
        assertThat(facilityS2.getName()).isEqualTo(segments.get(1).getPickUpFacilityId());
        Facility facilityE2 = segmentDomainList.get(1).getEndFacility();
        assertThat(facilityE2).isNotNull();
        assertThat(facilityE2.getExternalId()).isEqualTo(segments.get(1).getDropOffFacilityId());
        assertThat(facilityE2.getName()).isEqualTo(segments.get(1).getDropOffFacilityId());
        Facility facilityS5 = segmentDomainList.get(4).getStartFacility();
        assertThat(facilityS5).isNotNull();
        assertThat(facilityS5.getExternalId()).isEqualTo(segments.get(4).getPickUpFacilityId());
        assertThat(facilityS5.getName()).isEqualTo(segments.get(4).getPickUpFacilityId());
        assertThat(facilityS5.getCode()).isEqualTo(segments.get(4).getPickUpFacilityId());
        Facility facilityE5 = segmentDomainList.get(4).getEndFacility();
        assertThat(facilityE5).isNotNull();
        assertThat(facilityE5.getExternalId()).isEqualTo(root.getDestination().getId());
        assertThat(facilityE5.getName()).isEqualTo(Shipment.DESTINATION_PROPERTY_NAME);
        assertThat(facilityE5.getCode()).isEqualTo(Shipment.DESTINATION_PROPERTY_NAME);
    }

    @Test
    void givenSamplePayload1SegmentData_whenGenerateJourney_properlyMapFromPayloadToJourney() throws JsonProcessingException {
        //GIVE:
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("SHPV2");
        currentOrganization.setId("SHPV2_ID");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);

        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-1-segment.json");
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(data.toString(), data.get("is_segment").toString());
        Root root = testUtil.createRootFromOM(data.toString());
        root.setSegmentsPayloads(segments);
        //WHEN:
        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        //THEN:
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();
        assertThat(segmentDomainList).hasSize(1);

        PackageJourneySegment segmentDomain = segmentDomainList.get(0);
        assertThat(segmentDomain.getSequence()).isEqualTo("0");
        assertThat(segmentDomain.getPickUpTime()).isEqualTo("2022-12-10T16:27:02+07:00");
        assertThat(segmentDomain.getDropOffTime()).isEqualTo("2022-12-11T14:27:02+07:00");
        assertThat(segmentDomain.getCalculatedMileage()).isEqualTo(root.getSegmentsPayloads().get(0).getCalculatedMileage());
        assertThat(segmentDomain.getDuration()).isEqualTo(root.getSegmentsPayloads().get(0).getDuration());
        assertThat(segmentDomain.getDurationUnit()).isEqualTo(UnitOfMeasure.MINUTE);
        assertThat(segmentDomain.getHubId()).isEqualTo(root.getSegmentsPayloads().get(0).getHandleFacilityId());
        verify(packageJourneySegmentTypeAssigner, times(1)).assignSegmentTypes(segmentDomainList);
    }

    @Test
    void givenNoSegmentInPayload_whenGenerateJourney_shouldReturnNullJourney() throws JsonProcessingException {
        //GIVE:
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-1-segment.json");
        Root root = testUtil.createRootFromOM(data.toString());
        //WHEN:
        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        //THEN:
        assertThat(journeyDomain).isNull();
        verifyNoInteractions(packageJourneySegmentTypeAssigner);
    }

    @Test
    void convertCalculatedMileageUomStringToUom_imperialArgument_shouldReturnMile() throws JsonProcessingException {
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("SHPV2");
        currentOrganization.setId("SHPV2_ID");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);

        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        ObjectNode dataObj = (ObjectNode) data;
        dataObj.put("distance_uom", Root.DISTANCE_UOM_IMPERIAL);
        Root root = testUtil.createRootFromOM(data.toString());
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(data.toString(), data.get("is_segment").toString());
        root.setSegmentsPayloads(segments);

        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);

        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(segmentDomainList).hasSize(5);
        assertThat(segmentDomainList.get(0).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(1).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(2).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(3).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(4).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
    }

    @Test
    void convertCalculatedMileageUomStringToUom_metricArgument_shouldReturnKm() throws JsonProcessingException {
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("SHPV2");
        currentOrganization.setId("SHPV2_ID");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        ObjectNode dataObj = (ObjectNode) data;
        dataObj.put("distance_uom", Root.DISTANCE_UOM_METRIC);
        Root root = testUtil.createRootFromOM(data.toString());
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(dataObj.toString(), data.get("is_segment").toString());
        root.setSegmentsPayloads(segments);

        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(segmentDomainList).hasSize(5);
        assertThat(segmentDomainList.get(0).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(1).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(2).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(3).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(4).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
    }

    @Test
    void givenCurrentOrganizationInConfigToNotCreateSegmentFromPayload_whenGenerateShipmentJourney_ThenShouldReturnNull() throws JsonProcessingException {
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("UPSAPAC");
        currentOrganization.setId("UPSAPAC");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        ObjectNode dataObj = (ObjectNode) data;
        dataObj.put("distance_uom", Root.DISTANCE_UOM_METRIC);
        Root root = testUtil.createRootFromOM(data.toString());
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(dataObj.toString(), data.get("is_segment").toString());
        root.setSegmentsPayloads(segments);

        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        assertThat(journeyDomain).isNull();
    }

    @Test
    void givenCurrentOrganizationInConfigToNotCreateSegmentFromPayloadButSegmentForUpdate_whenGenerateShipmentJourney_ThenShouldGenerateJourneyFromPayload() throws JsonProcessingException {
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("UPSAPAC");
        currentOrganization.setId("UPSAPAC");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        ObjectNode dataObj = (ObjectNode) data;
        dataObj.put("distance_uom", Root.DISTANCE_UOM_METRIC);
        Root root = testUtil.createRootFromOM(data.toString());
        root.setSegmentsUpdated(true);
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(dataObj.toString(), data.get("is_segment").toString());
        root.setSegmentsPayloads(segments);

        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        assertThat(journeyDomain).isNotNull();
    }

    @Test
    void mapOrderMessageToJourney_orderWithSingleSegmentWithoutFacilityIds_shouldMapFacilityDetailsToJourney() throws JsonProcessingException {
        when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(ORG_CODE_SKIP_SEGMENT_PAYLOAD_CREATION);

        Organization currentOrganization = new Organization();
        currentOrganization.setCode("SHPV2");
        currentOrganization.setId("SHPV2_ID");

        when(userDetailsProvider.getCurrentOrganization()).thenReturn(currentOrganization);

        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-1-segments-without-facilityIds.json");
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(data.toString(), data.get("is_segment").toString());
        Root root = testUtil.createRootFromOM(data.toString());
        root.setSegmentsPayloads(segments);

        ShipmentJourney journeyDomain = orderMessageJourneyGenerator.generateShipmentJourney(root);
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(segmentDomainList).hasSize(1);
        Facility facilityStart = segmentDomainList.get(0).getStartFacility();
        assertThat(facilityStart).isNotNull();

        Facility facilityEnd = segmentDomainList.get(0).getEndFacility();
        assertThat(facilityEnd).isNotNull();
    }
}
