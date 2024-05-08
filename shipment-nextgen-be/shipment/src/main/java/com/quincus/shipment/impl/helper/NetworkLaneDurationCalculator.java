package com.quincus.shipment.impl.helper;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@Slf4j
public class NetworkLaneDurationCalculator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public BigDecimal calculateNetworkLaneSegmentDuration(NetworkLaneSegment networkLaneSegment) {
        if (TransportType.GROUND == networkLaneSegment.getTransportType()) {
            return calculateNetworkLaneGroundSegmentDuration(networkLaneSegment);
        } else if (TransportType.AIR == networkLaneSegment.getTransportType()) {
            return calculateNetworkLaneAirSegmentDuration(networkLaneSegment);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateNetworkLaneAirSegmentDuration(NetworkLaneSegment networkLaneSegment) {
        List<OffsetDateTime> segmentDates = Stream.of(networkLaneSegment.getDropOffTime(), networkLaneSegment.getLockOutTime()
                        , networkLaneSegment.getArrivalTime(), networkLaneSegment.getRecoveryTime())
                .map(this::parseToOffsetDateTime).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(segmentDates)) {
            return BigDecimal.ZERO;
        }
        OffsetDateTime earliestDate = Collections.min(segmentDates);
        OffsetDateTime latestDate = Collections.max(segmentDates);
        Duration difference = Duration.between(earliestDate, latestDate);
        if (UnitOfMeasure.HOUR == networkLaneSegment.getDurationUnit()) {
            return BigDecimal.valueOf(difference.toHours());
        }

        return BigDecimal.valueOf(difference.toMinutes());
    }

    private BigDecimal calculateNetworkLaneGroundSegmentDuration(NetworkLaneSegment networkLaneSegment) {
        List<OffsetDateTime> segmentDates = Stream.of(networkLaneSegment.getPickUpTime(), networkLaneSegment.getDropOffTime())
                .map(this::parseToOffsetDateTime).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(segmentDates)) {
            return BigDecimal.ZERO;
        }
        OffsetDateTime earliestDate = Collections.min(segmentDates);
        OffsetDateTime latestDate = Collections.max(segmentDates);
        Duration difference = Duration.between(earliestDate, latestDate);
        if (UnitOfMeasure.HOUR == networkLaneSegment.getDurationUnit()) {
            return BigDecimal.valueOf(difference.toHours());
        }
        return BigDecimal.valueOf(difference.toMinutes());
    }

    private OffsetDateTime parseToOffsetDateTime(String strDateTime) {
        if (StringUtils.isBlank(strDateTime)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(strDateTime, formatter);
        } catch (Exception e) {
            log.warn("NetworkLaneDurationCalculator:: Error parsing datetime: {}", strDateTime, e);
            return null;
        }
    }
}
