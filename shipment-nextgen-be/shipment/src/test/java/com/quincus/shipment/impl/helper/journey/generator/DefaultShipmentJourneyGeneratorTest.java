package com.quincus.shipment.impl.helper.journey.generator;

import com.quincus.order.api.domain.Destination;
import com.quincus.order.api.domain.Origin;
import com.quincus.order.api.domain.Root;
import com.quincus.order.api.domain.SegmentsPayload;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultShipmentJourneyGeneratorTest {

    private final DefaultShipmentJourneyGenerator defaultJourneyGenerator = new DefaultShipmentJourneyGenerator();

    @Test
    void givenRoot_whenGenerateJourney_mapOrderToJourneyAndSegments() {
        //GIVEN:
        Root orderMessage = new Root();
        orderMessage.setUserId("USERID_TEST");
        orderMessage.setId("ID_TEST");
        orderMessage.setOpsType("P2P");
        orderMessage.setStatus("STATUS_TEST");
        orderMessage.setOrderIdLabel("ORDER_ID_LABEL_TEST");
        orderMessage.setCustomerReferences(new ArrayList<>());
        orderMessage.setPickupStartTime("2023-02-14 08:00:00 GMT-05:00");
        orderMessage.setPickupCommitTime("2023-02-16 22:59:00 GMT-05:00");
        orderMessage.setPickupTimezone("GMT-05:00");
        orderMessage.setDeliveryStartTime("2023-03-28 10:00:00 GMT+10:00");
        orderMessage.setDeliveryCommitTime("2023-03-29 19:59:00 GMT+10:00");
        orderMessage.setDeliveryTimezone("GMT+10:00");
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setInternalOrderId("123");
        orderMessage.setExternalOrderId("456");
        orderMessage.setCustomerOrderId("789");
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);

        //WHEN:
        ShipmentJourney shipmentJourney = defaultJourneyGenerator.generateShipmentJourney(orderMessage);

        //THEN:
        assertThat(shipmentJourney).isNotNull();
        assertThat(shipmentJourney.getStatus()).isEqualTo(JourneyStatus.PLANNED);
        assertThat(shipmentJourney.getPackageJourneySegments()).isNotEmpty().hasSize(1);
        PackageJourneySegment segment = shipmentJourney.getPackageJourneySegments().get(0);
        assertThat(segment.getOpsType()).isEqualTo(orderMessage.getOpsType());
        assertThat(segment.getStatus()).isEqualTo(SegmentStatus.PLANNED);
        assertThat(segment.getRefId()).isEqualTo("0");
        assertThat(segment.getSequence()).isEqualTo("0");
        assertThat(segment.getPickUpTime()).isEqualTo("2023-02-14T08:00:00-05:00");
        assertThat(segment.getPickUpCommitTime()).isEqualTo("2023-02-16T22:59:00-05:00");
        assertThat(segment.getDropOffTime()).isEqualTo("2023-03-28T10:00:00+10:00");
        assertThat(segment.getDropOffCommitTime()).isEqualTo("2023-03-29T19:59:00+10:00");
        assertThat(segment.getTransportType()).isEqualTo(TransportType.GROUND);
        assertThat(segment.getType()).isEqualTo(SegmentType.LAST_MILE);
        Facility startFacility = segment.getStartFacility();
        assertThat(startFacility).isNotNull();
        assertThat(startFacility.getName()).isEqualTo(Shipment.ORIGIN_PROPERTY_NAME);
        Facility endFacility = segment.getEndFacility();
        assertThat(endFacility).isNotNull();
        assertThat(endFacility.getName()).isEqualTo(Shipment.DESTINATION_PROPERTY_NAME);
    }

    @Test
    void givenRoot_whenGivenSegments_mapFacilityLocations() {
        //GIVEN:
        Root orderMessage = new Root();
        orderMessage.setUserId("USERID_TEST");
        orderMessage.setId("ID_TEST");
        orderMessage.setOpsType("P2P");
        orderMessage.setStatus("STATUS_TEST");
        orderMessage.setOrderIdLabel("ORDER_ID_LABEL_TEST");
        orderMessage.setCustomerReferences(new ArrayList<>());
        orderMessage.setPickupStartTime("2023-02-14 08:00:00 GMT-05:00");
        orderMessage.setPickupCommitTime("2023-02-16 22:59:00 GMT-05:00");
        orderMessage.setPickupTimezone("GMT-05:00");
        orderMessage.setDeliveryStartTime("2023-03-28 10:00:00 GMT+10:00");
        orderMessage.setDeliveryCommitTime("2023-03-29 19:59:00 GMT+10:00");
        orderMessage.setDeliveryTimezone("GMT+10:00");
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setInternalOrderId("123");
        orderMessage.setExternalOrderId("456");
        orderMessage.setCustomerOrderId("789");
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);
        orderMessage.setIsSegment("true");

        SegmentsPayload omSegment1 = new SegmentsPayload();
        omSegment1.setRefId("1");
        omSegment1.setSequence("1");
        SegmentsPayload omSegment2 = new SegmentsPayload();
        omSegment2.setRefId("2");
        omSegment2.setSequence("2");
        SegmentsPayload omSegment3 = new SegmentsPayload();
        omSegment3.setRefId("3");
        omSegment3.setSequence("3");

        orderMessage.setSegmentsPayloads(List.of(omSegment1, omSegment2, omSegment3));

        //WHEN:
        ShipmentJourney shipmentJourney = defaultJourneyGenerator.generateShipmentJourney(orderMessage);

        //THEN:
        assertThat(shipmentJourney).isNotNull();
        assertThat(shipmentJourney.getStatus()).isEqualTo(JourneyStatus.PLANNED);
        assertThat(shipmentJourney.getPackageJourneySegments()).isNotEmpty().hasSize(1);
        PackageJourneySegment segment1 = shipmentJourney.getPackageJourneySegments().get(0);
        Facility startFacility1 = segment1.getStartFacility();
        assertThat(segment1.getOpsType()).isEqualTo(orderMessage.getOpsType());
        assertThat(segment1.getPickUpTime()).isEqualTo("2023-02-14T08:00:00-05:00");
        assertThat(segment1.getPickUpCommitTime()).isEqualTo("2023-02-16T22:59:00-05:00");
        assertThat(segment1.getDropOffTime()).isEqualTo("2023-03-28T10:00:00+10:00");
        assertThat(segment1.getDropOffCommitTime()).isEqualTo("2023-03-29T19:59:00+10:00");

        assertThat(startFacility1).isNotNull();
        assertThat(startFacility1.getName()).isEqualTo(Shipment.ORIGIN_PROPERTY_NAME);
        Facility endFacility1 = segment1.getEndFacility();
        assertThat(endFacility1).isNotNull();
        assertThat(endFacility1.getName()).isEqualTo(Shipment.DESTINATION_PROPERTY_NAME);
    }
}
