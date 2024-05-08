package com.quincus.shipment.impl.helper.segment;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;

import java.util.List;
import java.util.Optional;

public interface SegmentUpdateChecker {

    boolean isSegmentMatch(PackageJourneySegment segment, PackageJourneySegmentEntity existingSegment);

    Optional<PackageJourneySegmentEntity> findSegmentForUpdate(PackageJourneySegment segment, List<PackageJourneySegmentEntity> existingSegments);
}
