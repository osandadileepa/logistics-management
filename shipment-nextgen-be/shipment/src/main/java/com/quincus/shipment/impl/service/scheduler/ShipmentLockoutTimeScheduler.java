package com.quincus.shipment.impl.service.scheduler;

import com.quincus.ext.ListUtil;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.service.ShipmentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShipmentLockoutTimeScheduler {

    private static final String METHOD_NAME_CHECK_LOCKOUT_TIME_MISSED = "checkLockoutTimeMissed";
    private static final String LOG_START_SCHEDULED = "Started scheduled call: {}.";
    private static final String LOG_NO_SHIPMENT_PROCESSED = "Scheduled call {} ended. Reason: No qualifying shipment available.";
    private static final String LOG_SHIPMENT_COUNT = "shipment entries to check: {}.";
    private final PackageJourneySegmentService segmentService;
    private final ShipmentService shipmentService;
    private final MessageApi messageApi;

    @Scheduled(fixedRateString = "${shipment.async.lockoutCheckInterval}")
    public void checkLockoutTimeMissed() {
        log.debug(LOG_START_SCHEDULED, METHOD_NAME_CHECK_LOCKOUT_TIME_MISSED);
        List<Shipment> activeShipments = shipmentService.findActiveShipmentsWithAirSegment();
        if (CollectionUtils.isEmpty(activeShipments)) {
            log.info(LOG_NO_SHIPMENT_PROCESSED, METHOD_NAME_CHECK_LOCKOUT_TIME_MISSED);
            return;
        }
        log.debug(LOG_SHIPMENT_COUNT, activeShipments.size());

        int cachedSegments = 0;
        for (Shipment activeShipment : activeShipments) {
            PackageJourneySegment airSegment = getAirSegmentLockoutTimeMissed(activeShipment);
            if (airSegment == null) {
                continue;
            }

            ++cachedSegments;
            messageApi.sendDispatchCanceledFlight(activeShipment, airSegment.getSegmentId(), "flight canceled");
            segmentService.cacheLockoutTimePassedSegment(airSegment);
        }
        log.debug("total segments cached: {}", cachedSegments);
    }

    public PackageJourneySegment getAirSegmentLockoutTimeMissed(Shipment shipment) {
        List<PackageJourneySegment> segments = shipment.getShipmentJourney().getPackageJourneySegments();
        int plannedAirSegmentPos = ListUtil.findIndex(segments, this::isAirSegmentPlanned);
        if (plannedAirSegmentPos < 0) {
            return null;
        }

        PackageJourneySegment previousSegment = (plannedAirSegmentPos > 0) ? segments.get(plannedAirSegmentPos - 1) : null;
        if (previousSegment != null
                && (SegmentStatus.IN_PROGRESS != previousSegment.getStatus()
                && SegmentStatus.COMPLETED != previousSegment.getStatus())) {
            return null;
        }

        PackageJourneySegment airSegment = segments.get(plannedAirSegmentPos);
        if (!segmentService.isSegmentLockoutTimeMissed(airSegment)) {
            return null;
        }

        return airSegment;
    }

    private boolean isAirSegmentPlanned(PackageJourneySegment entity) {
        return TransportType.AIR == entity.getTransportType() && SegmentStatus.PLANNED == entity.getStatus();
    }
}
