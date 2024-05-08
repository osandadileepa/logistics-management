package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.test_utils.TupleDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentProcessingServiceTest {

    @InjectMocks
    ShipmentProcessingService shipmentProcessingService;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;

    @Test
    void findShipmentsFromSegmentList_validParam_shouldReturnShipmentEntityList() {
        String segmentId1 = "segment1";
        String segmentId2 = "segment2";
        List<String> segmentIdList = List.of(segmentId1, segmentId2);
        String journeyId1 = "journey1";
        List<String> refJourneyIdList = List.of(journeyId1);
        List<Tuple> shpObjDummy = createDummyPartialShipment(journeyId1);
        List<PackageJourneySegment> segmentsDummy = createDummySegmentList(refJourneyIdList, segmentIdList.size());

        when(shipmentRepository.findShipmentsPartialFieldsFromSegmentIdList(anyList())).thenReturn(shpObjDummy);
        when(packageJourneySegmentService.getAllSegmentsFromShipments(anyList())).thenReturn(segmentsDummy);

        List<Shipment> resultList = shipmentProcessingService.findShipmentsFromSegmentList(segmentIdList);

        assertThat(resultList).hasSize(refJourneyIdList.size());
        assertThat(resultList.get(0).getShipmentJourney()).isNotNull();
        assertThat(resultList.get(0).getShipmentJourney().getJourneyId()).isEqualTo(journeyId1);
        assertThat(resultList.get(0).getShipmentJourney().getPackageJourneySegments()).hasSize(segmentIdList.size());
        assertThat(resultList.get(0).getShipmentJourney().getPackageJourneySegments().get(0).getJourneyId())
                .isEqualTo(journeyId1);
        assertThat(resultList.get(0).getShipmentJourney().getPackageJourneySegments().get(1).getJourneyId())
                .isEqualTo(journeyId1);
    }

    private List<Tuple> createDummyPartialShipment(String refJourneyId) {
        List<Tuple> partialShpList = new ArrayList<>();
        String shipmentId = UUID.randomUUID().toString();
        String organizationId = "ORG-001";

        String shipmentTrackingId = "QC-SHP-001";
        String orderId = "ORDER-001";

        Tuple tuple = TupleDataFactory.ofShipmentFromFlightDelay(shipmentId, shipmentTrackingId, organizationId, orderId,
                refJourneyId);
        partialShpList.add(tuple);

        return partialShpList;
    }

    private List<PackageJourneySegment> createDummySegmentList(List<String> journeyIds, int count) {
        List<PackageJourneySegment> segmentList = new ArrayList<>();

        for (String journeyId : journeyIds) {
            for (int i = 0; i < count; i++) {
                PackageJourneySegment segment = new PackageJourneySegment();
                segment.setSegmentId(UUID.randomUUID().toString());
                segment.setJourneyId(journeyId);
                segment.setRefId(String.format("%d", i));
                segment.setSequence(String.format("%d", i));
                segmentList.add(segment);
            }
        }
        return segmentList;
    }
}
