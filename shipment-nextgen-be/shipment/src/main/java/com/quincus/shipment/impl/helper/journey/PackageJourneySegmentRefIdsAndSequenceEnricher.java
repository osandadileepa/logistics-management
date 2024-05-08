package com.quincus.shipment.impl.helper.journey;

import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class PackageJourneySegmentRefIdsAndSequenceEnricher {

    /**
     * Method to enrich the segments with type, refIds and sequence.
     * <p>
     * This method generates or validates the refId of each segment in the list,
     * sets the sequence number as per the segment's position in the list,
     * and assigns the type of segment based on its position in the journey.
     * <p>
     * Scenarios for ref_id generation
     * <p>
     * Scenario 1: Input ["1", null, "2"], generates a new refId for the null value ["1", "3", "2"]
     * Scenario 2: Input of ["1", "2", null], generates a new refId for the null values ["1", "2", "3"]
     * Scenario 3: Input of [null, null, null], generates new refIds for all null values ["0", "1", "2"]
     * Scenario 4: Input of ["a", null, "b"], generates a new refId for the null values ["a", "0", "b"]
     * Scenario 5: Input of ["a", "b", null], generates a new refId for the null values["a", "b", "0"]
     *
     * @param segments - a list of PackageJourneySegment that will be enriched.
     */
    public void enrichSegmentsWithTypesRefIdsAndSequence(List<PackageJourneySegment> segments) {
        if (CollectionUtils.isEmpty(segments)) {
            return;
        }
        int maxRefId = calculateMaxRefId(segments);
        for (int i = 0; i < segments.size(); i++) {
            PackageJourneySegment segment = segments.get(i);
            String refId = segment.getRefId();
            if (StringUtils.isBlank(refId)) {
                segment.setRefId(String.valueOf(++maxRefId));
                segment.setNewlyCreated(true);
            } else if (NumberUtils.isCreatable(refId)) {
                maxRefId = Math.max(maxRefId, Integer.parseInt(refId));
            }
            segment.setSequence(String.valueOf(i));
            segment.setType(getSegmentType(i, segments.size()));
        }
    }

    private int calculateMaxRefId(List<PackageJourneySegment> segments) {
        return segments.stream()
                .map(PackageJourneySegment::getRefId)
                .filter(StringUtils::isNotBlank)
                .filter(NumberUtils::isCreatable)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(-1);
    }

    private SegmentType getSegmentType(int index, int size) {
        if (size == 1 || index == size - 1) return SegmentType.LAST_MILE;
        if (size > 1 && index == 0) return SegmentType.FIRST_MILE;
        return SegmentType.MIDDLE_MILE;
    }
}
