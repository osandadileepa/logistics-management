package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ShipmentAsyncService {
    private static final String CREATE_OR_UPDATE_ERR_MSG = "Error occurred while creating or updating shipment with order id label `{}`";
    private final ShipmentService shipmentBatchService;
    private final ShipmentPostProcessService shipmentPostProcessService;

    @Async("threadPoolTaskExecutor")
    @Transactional
    public void processAndDispatchShipments(final List<Shipment> shipmentList, final boolean areSegmentsUpdated) {
        try {
            List<Shipment> processedShipments = new ArrayList<>(shipmentBatchService.createOrUpdate(shipmentList, areSegmentsUpdated));
            SegmentDispatchType dispatchType;
            if (processedShipments.stream().anyMatch(Shipment::isSegmentsUpdatedFromSource)) {
                dispatchType = SegmentDispatchType.JOURNEY_UPDATED;
            } else if (processedShipments.stream().anyMatch(Shipment::isUpdated)) {
                dispatchType = SegmentDispatchType.SHIPMENT_UPDATED;
            } else {
                dispatchType = SegmentDispatchType.SHIPMENT_CREATED;
            }
            Shipment refShipment = processedShipments.get(0);
            if (isOrderNotCancelled(refShipment.getOrder())) {
                shipmentPostProcessService.sendJourneyToDispatch(processedShipments, refShipment.getShipmentJourney(), dispatchType);
            }
            shipmentPostProcessService.sendUpdateToQship(refShipment);
        } catch (Exception e) {
            log.error(CREATE_OR_UPDATE_ERR_MSG, shipmentList.get(0).getOrder().getId(), e);
        }
    }

    private boolean isOrderNotCancelled(final Order order) {
        return !ShipmentStatus.CANCELLED.name().equalsIgnoreCase(order.getStatus());
    }
}