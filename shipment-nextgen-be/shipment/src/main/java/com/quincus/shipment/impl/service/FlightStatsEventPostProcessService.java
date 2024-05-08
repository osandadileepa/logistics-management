package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.NotificationApi;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightStatusResult;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper;
import com.quincus.shipment.impl.mapper.ShipmentMessageDtoMapper;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class FlightStatsEventPostProcessService {
    private final MessageApi messageApi;
    private final MilestoneService milestoneService;
    private final NotificationApi notificationApi;
    private final ShipmentMessageDtoMapper shipmentMessageDtoMapper;
    private final AlertService alertService;

    @Async("threadPoolTaskExecutor")
    public void processAlerts(FlightStatusResult flightStatusResult) {
        alertService.saveAll(flightStatusResult.getAlerts());
    }

    public void sendUpdatedSegmentToQShip(PackageJourneySegmentEntity updatedSegment, List<ShipmentEntity> relatedShipments) {
        PackageJourneySegment packageJourneySegment = PackageJourneySegmentMapper.mapEntityToDomain(updatedSegment);
        sendUpdatedSegmentMessages(relatedShipments, packageJourneySegment);
    }

    public void processFlightStats(List<ShipmentEntity> shipmentEntities, PackageJourneySegmentEntity segmentEntity, Flight flight) {
        PackageJourneySegment packageJourneySegment = PackageJourneySegmentMapper.mapEntityToDomain(segmentEntity);
        for (ShipmentEntity shipmentEntity : shipmentEntities) {
            processSingleFlightStat(shipmentEntity, packageJourneySegment, flight);
        }
    }

    private boolean flightEventOccurred(Flight flight) {
        return flight.getFlightStatus().getEventName() == FlightEventName.FLIGHT_DEPARTED ||
                flight.getFlightStatus().getEventName() == FlightEventName.FLIGHT_LANDED;
    }

    private void processSingleFlightStat(ShipmentEntity shipmentEntity, PackageJourneySegment packageJourneySegment, Flight flight) {
        ShipmentMessageDto shipmentMessageDto = toShipmentMessageDto(shipmentEntity);
        sendUpdatedSegmentMessage(shipmentMessageDto, packageJourneySegment);
        if (flightEventOccurred(flight)) {
            sendMilestoneAndNotify(shipmentMessageDto, packageJourneySegment, flight);
        }
    }

    private ShipmentMessageDto toShipmentMessageDto(ShipmentEntity shipmentEntity) {
        return shipmentMessageDtoMapper.mapToDto(shipmentEntity);
    }

    private void sendUpdatedSegmentMessage(ShipmentMessageDto shipmentDto, PackageJourneySegment segment) {
        messageApi.sendUpdatedSegmentFromShipment(shipmentDto, segment);
    }

    private void sendMilestoneAndNotify(ShipmentMessageDto shipmentDto, PackageJourneySegment segment, Flight flight) {
        Milestone createdMilestone = milestoneService.createMilestoneFromFlightEvent(shipmentDto, segment, flight);
        messageApi.sendFlightMilestoneMessage(createdMilestone, shipmentDto, segment, TriggeredFrom.APIG);
        notificationApi.sendNotification(NotificationRequest.ofFlightNotification(shipmentMessageDtoMapper.mapToShipment(shipmentDto), createdMilestone, flight));
    }

    private void sendUpdatedSegmentMessages(List<ShipmentEntity> relatedShipments, PackageJourneySegment packageJourneySegment) {
        shipmentMessageDtoMapper.mapAllToDto(relatedShipments).forEach(shipmentQShipDto ->
                sendUpdatedSegmentMessage(shipmentQShipDto, packageJourneySegment)
        );
    }

}
