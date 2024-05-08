package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ShipmentProcessingService {
    private final ShipmentRepository shipmentRepository;
    private final PackageJourneySegmentService packageJourneySegmentService;

    @Transactional(readOnly = true)
    public List<Shipment> findShipmentsFromSegmentList(List<String> segmentIdList) {
        List<Tuple> shpObj = shipmentRepository.findShipmentsPartialFieldsFromSegmentIdList(segmentIdList);
        if (CollectionUtils.isEmpty(shpObj)) {
            return Collections.emptyList();
        }

        List<Shipment> shipments = new ArrayList<>();
        shpObj.forEach(o -> shipments.add(ShipmentUtil.convertObjectArrayToShipmentLimited(o)));

        List<PackageJourneySegment> segments = packageJourneySegmentService.getAllSegmentsFromShipments(shipments);
        ShipmentUtil.addSegmentsToShipments(shipments, segments);

        return shipments;
    }

}
