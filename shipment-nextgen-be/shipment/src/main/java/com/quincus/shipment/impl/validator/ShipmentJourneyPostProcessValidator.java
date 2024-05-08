package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.web.common.validator.PostProcessValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ShipmentJourneyPostProcessValidator implements PostProcessValidator<ShipmentJourney> {

    private PackageJourneySegmentPostProcessValidator segmentValidator;

    @Override
    public boolean isValid(ShipmentJourney journey) {
        if (journey == null || CollectionUtils.isEmpty(journey.getPackageJourneySegments())) {
            String errorMessage = "Shipment Journey and/or its Segments are not provided. Make sure these are not omitted in the query results.";
            log.warn(errorMessage);
            return false;
        }

        for (PackageJourneySegment segment : journey.getPackageJourneySegments()) {
            if (!segment.isDeleted() && !segmentValidator.isValid(segment)) {
                return false;
            }
        }
        return true;
    }
}
