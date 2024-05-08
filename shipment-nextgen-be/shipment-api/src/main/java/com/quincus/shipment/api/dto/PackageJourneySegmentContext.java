package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.constant.PackageJourneySegmentState;
import com.quincus.shipment.api.domain.PackageJourneySegment;

public record PackageJourneySegmentContext(PackageJourneySegmentState state,
                                           PackageJourneySegment segment) {
}
