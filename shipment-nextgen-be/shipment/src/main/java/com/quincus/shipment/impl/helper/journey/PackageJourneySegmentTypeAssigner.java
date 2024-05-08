package com.quincus.shipment.impl.helper.journey;

import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PackageJourneySegmentTypeAssigner {

    public void assignSegmentTypes(List<PackageJourneySegment> segmentList) {
        if (CollectionUtils.isEmpty(segmentList)) {
            return;
        }
        segmentList.get(0).setType(SegmentType.FIRST_MILE);
        segmentList.get(segmentList.size() - 1).setType(SegmentType.LAST_MILE);
        if (segmentList.size() > 2) {
            segmentList.stream().filter(s -> s.getType() == null).forEach(s -> s.setType(SegmentType.MIDDLE_MILE));
        }
    }
}
