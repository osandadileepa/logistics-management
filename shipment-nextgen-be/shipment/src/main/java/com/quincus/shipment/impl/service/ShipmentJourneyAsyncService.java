package com.quincus.shipment.impl.service;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentJourneyContext;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ShipmentJourneyAsyncService {
    private static final String QLOGGER_UPDATE_EVENT_SOURCE = "ShipmentJourneyService#updateShipmentJourney";
    private static final String EXTERNAL_API_CALL_FAILED = "External API call failed. Error message: %s";
    private final QLoggerAPI qLoggerAPI;
    private final ApiGatewayApi apiGatewayApi;
    private final ShipmentRepository shipmentRepository;
    private final UserDetailsProvider userDetailsProvider;
    private final ShipmentPostProcessService shipmentPostProcessService;

    @Async("threadPoolTaskExecutor")
    public void sendShipmentJourneyUpdates(
            final List<String> shipmentIds,
            final ShipmentJourney previousShipmentJourney,
            final ShipmentJourney updatedShipmentJourney) {

        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        List<Shipment> updatedShipments = new ArrayList<>();
        shipmentIds.stream()
                .map(id -> shipmentRepository.findByIdAfterJourneyUpdate(id, organizationId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(shp -> {
                    final Shipment previousShipment = ShipmentMapper.toShipmentForShipmentJourneyUpdate(shp);
                    previousShipment.setShipmentJourney(previousShipmentJourney);
                    final Shipment updatedShipment = ShipmentMapper.toShipmentForShipmentJourneyUpdate(shp);
                    updatedShipment.setShipmentJourney(updatedShipmentJourney);
                    sendUpdateShipmentJourneyToOtherProductsAndQLogger(previousShipment, updatedShipment);
                    updatedShipments.add(updatedShipment);
                });
        Shipment refShipment = updatedShipments.get(0);
        if (userDetailsProvider.isFromAllowedSource()) {
            shipmentPostProcessService.sendUpdateToQship(refShipment, previousShipmentJourney);
        }
        shipmentPostProcessService.sendUpdateToQship(refShipment, updatedShipmentJourney);
        shipmentPostProcessService.sendJourneyUpdateToDispatch(updatedShipments, updatedShipmentJourney);
    }

    @Async("threadPoolTaskExecutor")
    public void sendShipmentJourneyUpdates(ShipmentJourneyContext shipmentJourneyContext) {
        ShipmentJourney previousShipmentJourney = shipmentJourneyContext.previousShipmentJourney();
        ShipmentJourney updatedShipmentJourney = shipmentJourneyContext.updatedShipmentJourney();
        List<PackageJourneySegment> segments = updatedShipmentJourney.getPackageJourneySegments();
        List<String> shipmentIds = shipmentJourneyContext.shipmentIds();

        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        List<Shipment> updatedShipments = new ArrayList<>();
        shipmentIds.stream()
                .map(id -> shipmentRepository.findByIdAfterJourneyUpdate(id, organizationId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(shipmentEntity -> {
                    final Shipment previousShipment = ShipmentMapper.toShipmentForShipmentJourneyUpdate(shipmentEntity);
                    previousShipment.setShipmentJourney(previousShipmentJourney);
                    final Shipment updatedShipment = ShipmentMapper.toShipmentForShipmentJourneyUpdate(shipmentEntity);
                    updatedShipment.setShipmentJourney(updatedShipmentJourney);
                    sendUpdateShipmentJourneyToOtherProductsAndQLogger(previousShipment, updatedShipment);
                    updatedShipments.add(updatedShipment);
                });

        Shipment refShipment = updatedShipments.get(0);
        segments.forEach(segment -> shipmentPostProcessService.sendSingleSegmentToQship(refShipment, segment));
        shipmentPostProcessService.sendJourneyUpdateToDispatch(updatedShipments, updatedShipmentJourney);
    }

    private void sendUpdateShipmentJourneyToOtherProductsAndQLogger(Shipment previousShipment, Shipment updatedShipment) {
        ShipmentJourney previousShipmentJourney = previousShipment.getShipmentJourney();
        ShipmentJourney updatedShipmentJourney = updatedShipment.getShipmentJourney();
        sendUpdateToApiGIfPartnerHasChanged(updatedShipment, previousShipmentJourney, updatedShipmentJourney);
        try {
            qLoggerAPI.publishShipmentJourneyUpdatedEventWithRetry(QLOGGER_UPDATE_EVENT_SOURCE, previousShipmentJourney, updatedShipmentJourney, updatedShipment);
        } catch (Exception e) {
            log.error(String.format(EXTERNAL_API_CALL_FAILED, e.getMessage()));
        }
    }

    public void sendUpdateToApiGIfPartnerHasChanged(Shipment shipment, ShipmentJourney previousShipmentJourney, ShipmentJourney updatedShipmentJourney) {
        updatedShipmentJourney.getPackageJourneySegments().forEach(segment -> {
            Optional<PackageJourneySegment> previousSegmentOpt = previousShipmentJourney.getPackageJourneySegments().stream()
                    .filter(e -> StringUtils.equalsIgnoreCase(e.getRefId(), segment.getRefId()))
                    .findFirst();

            if (segment.getPartner() != null
                    && (previousSegmentOpt.isEmpty() || (previousSegmentOpt.get().getPartner() == null
                    || !StringUtils.equals(previousSegmentOpt.get().getPartner().getId(), segment.getPartner().getId())))) {
                apiGatewayApi.sendAssignVendorDetailsWithRetry(shipment, segment);
            }
        });
    }


}
