package com.quincus.shipment.impl.helper.journey;

import com.quincus.ext.DateTimeUtil;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Component
public class PackageJourneySegmentPickUpDropOffTimeCalculator {

    public void computeAndAssignPickUpAndDropOffTime(List<PackageJourneySegment> segmentList, Root omMessage) {
        if (CollectionUtils.isEmpty(segmentList) || Objects.isNull(omMessage)
                || StringUtils.isBlank(omMessage.getPickupStartTime())) {
            return;
        }
        IntStream.range(0, segmentList.size()).forEach(index -> {
            PackageJourneySegment currentSegment = segmentList.get(index);
            currentSegment.setPickUpTime(identifyPickupTime(index, segmentList, omMessage));
            currentSegment.setDropOffTime(calculateDropOffTime(currentSegment));
        });
    }

    private String calculateDropOffTime(PackageJourneySegment segment) {
        ZonedDateTime zonedDatePickupTime = DateTimeUtil.parseZonedDateTime(segment.getPickUpTime());
        if (segment.getDuration() == null || zonedDatePickupTime == null) {
            return null;
        }
        return DateTimeUtil.toIsoDateTimeFormat(zonedDatePickupTime.plus(segment.getDuration().longValue(),
                convertUnitOfMeasureToTemporal(segment.getDurationUnit())).toString());

    }

    private String identifyPickupTime(int currentLoopIndex, List<PackageJourneySegment> segmentList, Root omMessage) {
        if (currentLoopIndex == 0) {
            return DateTimeUtil.toIsoDateTimeFormat(omMessage.getPickupStartTime());
        }
        return segmentList.get(currentLoopIndex - 1).getDropOffTime();
    }

    private ChronoUnit convertUnitOfMeasureToTemporal(UnitOfMeasure unitOfMeasure) {
        if (UnitOfMeasure.HOUR.equals(unitOfMeasure)) {
            return ChronoUnit.HOURS;
        }
        return ChronoUnit.MINUTES;
    }
}
