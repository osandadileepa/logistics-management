package com.quincus.shipment.impl.service;

import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ShipmentPostProcessService {
    static final String QLOGGER_UPDATE_EVENT_SOURCE = "ShipmentService#update";
    static final String QLOGGER_CREATE_EVENT_SOURCE = "ShipmentService#create";
    private MessageApi messageApi;
    private QLoggerAPI qLoggerAPI;

    public void sendUpdateToQship(Shipment shipment) {
        messageApi.sendShipmentToQShip(shipment);
    }

    public void sendUpdateToQship(Shipment shipment, ShipmentJourney journey) {
        journey.getPackageJourneySegments().forEach(segment -> segment.setJourneyId(journey.getJourneyId()));
        messageApi.sendShipmentWithJourneyToQShip(shipment, journey);
    }

    public void sendSingleSegmentToQship(Shipment shipment, String segmentId) {
        messageApi.sendUpdatedSegmentFromShipment(shipment, segmentId);
    }

    public void sendSingleSegmentToQship(Shipment shipment, PackageJourneySegment segment) {
        messageApi.sendUpdatedSegmentFromShipment(shipment, segment);
    }

    public void publishQloggerCreateEvents(Shipment createdOrUpdatedShipment) {
        qLoggerAPI.publishShipmentCreatedEvent(QLOGGER_CREATE_EVENT_SOURCE, createdOrUpdatedShipment);
        qLoggerAPI.publishShipmentJourneyCreatedEvent(QLOGGER_CREATE_EVENT_SOURCE, createdOrUpdatedShipment.getShipmentJourney(), createdOrUpdatedShipment);
    }

    public void publishQLoggerUpdateEvents(Shipment shipment, Shipment createdOrUpdatedShipment, Shipment previousShipment) {
        qLoggerAPI.publishShipmentUpdatedEvent(QLOGGER_UPDATE_EVENT_SOURCE, createdOrUpdatedShipment);
        if (shipment.getShipmentJourney() != null) {
            qLoggerAPI.publishShipmentJourneyUpdatedEvent(QLOGGER_UPDATE_EVENT_SOURCE, previousShipment.getShipmentJourney(), createdOrUpdatedShipment.getShipmentJourney(), createdOrUpdatedShipment);
        }
    }

    public void publishShipmentUpdatedEvent(Shipment updatedShipment) {
        qLoggerAPI.publishShipmentUpdatedEvent(QLOGGER_UPDATE_EVENT_SOURCE, updatedShipment);
    }

    public void sendJourneyToDispatch(List<Shipment> shipments, ShipmentJourney journey, SegmentDispatchType type) {
        messageApi.sendSegmentDispatch(shipments, journey, type, DspSegmentMsgUpdateSource.CLIENT);
    }

    public void sendUpdatedSegmentToDispatch(Milestone milestone, Shipment shipment) {
        if (!milestone.isSegmentUpdatedFromMilestone()) {
            return;
        }
        messageApi.sendSegmentDispatch(shipment, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    public void sendJourneyUpdateToDispatch(List<Shipment> shipments, ShipmentJourney journey) {
        messageApi.sendSegmentDispatch(shipments, journey, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }
}
