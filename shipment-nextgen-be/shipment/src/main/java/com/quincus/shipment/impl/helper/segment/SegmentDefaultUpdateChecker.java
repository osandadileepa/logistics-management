package com.quincus.shipment.impl.helper.segment;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@AllArgsConstructor
public class SegmentDefaultUpdateChecker implements SegmentUpdateChecker {

    public boolean isSegmentMatch(PackageJourneySegment segment, PackageJourneySegmentEntity existingSegment) {
        return isLocationMatch(segment.getStartFacility(), existingSegment.getStartLocationHierarchy())
                && isLocationMatch(segment.getEndFacility(), existingSegment.getEndLocationHierarchy())
                && isPartnerMatch(segment.getPartner(), existingSegment.getPartner());
    }

    public Optional<PackageJourneySegmentEntity> findSegmentForUpdate(PackageJourneySegment segment,
                                                                      List<PackageJourneySegmentEntity> existingSegments) {
        if (CollectionUtils.isEmpty(existingSegments)) {
            return Optional.empty();
        }
        return existingSegments.stream()
                .filter(Predicate.not(PackageJourneySegmentEntity::isDeleted))
                .filter(segmentEntity -> isSegmentMatch(segment, segmentEntity))
                .findFirst();
    }

    private boolean isLocationMatch(Facility location, LocationHierarchyEntity existingLocation) {
        if (location == null && existingLocation == null) {
            return true;
        }

        if (location == null || existingLocation == null) {
            return false;
        }

        return StringUtils.equalsIgnoreCase(location.getExternalId(), existingLocation.getExternalId());
    }

    private boolean isPartnerMatch(Partner partner, PartnerEntity existingPartner) {
        if (partner == null && existingPartner == null) {
            return true;
        }

        if (partner == null || existingPartner == null) {
            return false;
        }

        return StringUtils.equalsIgnoreCase(partner.getId(), existingPartner.getExternalId());
    }
}
