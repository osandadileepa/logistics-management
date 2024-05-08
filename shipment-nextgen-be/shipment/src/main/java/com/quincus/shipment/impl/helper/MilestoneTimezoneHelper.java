package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.quincus.ext.DateTimeUtil.getOffset;

@Component
@AllArgsConstructor
@Slf4j
public class MilestoneTimezoneHelper {
    private static final String UTC_TIMEZONE = "UTC+00:00";

    /**
     * Supplies the  timezone for eta and proof of delivery base from hub timezone
     *
     * @param milestone - Milestone
     */
    public void supplyEtaAndProofOfDeliveryTimezoneFromHubTimezone(Milestone milestone) {
        if (milestone == null || StringUtils.isBlank(milestone.getHubTimeZone())) {
            return;
        }
        if (milestone.getEta() != null) {
            milestone.setEtaTimezone(getOffset(milestone.getEta().toString(), milestone.getHubTimeZone()));
        }
        if (milestone.getProofOfDeliveryTime() != null) {
            milestone.setProofOfDeliveryTimezone(getOffset(milestone.getProofOfDeliveryTime().toString(), milestone.getHubTimeZone()));
        }
    }

    /**
     * Supplies the  timezone for eta and proof of delivery if not null from destination facility timezone
     *
     * @param milestone             - Milestone
     * @param packageJourneySegment - PackageJourneySegment
     */
    public void supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(Milestone milestone, PackageJourneySegment packageJourneySegment) {
        if (packageJourneySegment == null) {
            return;
        }

        if (milestone.getEta() != null) {
            milestone.setEtaTimezone(getOffset(milestone.getEta().toString(), packageJourneySegment.getEndFacility().getTimezone()));
        }
        if (milestone.getProofOfDeliveryTime() != null) {
            milestone.setProofOfDeliveryTimezone(getOffset(milestone.getProofOfDeliveryTime().toString(), packageJourneySegment.getEndFacility().getTimezone()));
        }
    }

    /**
     * Supplies the timezone for eta and proof of delivery if not null from destination facility timezone
     *
     * @param milestone                   - Milestone
     * @param packageJourneySegmentEntity - PackageJourneySegmentEntity
     */
    public void supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(Milestone milestone, PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (packageJourneySegmentEntity == null || packageJourneySegmentEntity.getEndLocationHierarchy() == null) {
            return;
        }

        String endFacilityTimezone = Optional.ofNullable(packageJourneySegmentEntity.getEndLocationHierarchy().getFacility())
                .map(LocationEntity::getTimezone).orElseGet(() -> packageJourneySegmentEntity.getEndLocationHierarchy().getCity().getTimezone());
        if (milestone.getEta() != null) {
            milestone.setEtaTimezone(getOffset(milestone.getEta().toString(), endFacilityTimezone));
        }
        if (milestone.getProofOfDeliveryTime() != null) {
            milestone.setProofOfDeliveryTimezone(getOffset(milestone.getProofOfDeliveryTime().toString(), endFacilityTimezone));
        }
    }

    /**
     * This will use the milestone hubTimezone that is set base on user facility timezone to be set to milestone time timezone field
     *
     * @param milestone milestone
     */
    public void supplyMilestoneTimezoneFromHubTimezone(Milestone milestone) {
        if (StringUtils.isBlank(milestone.getHubTimeZone()) || milestone.getMilestoneTime() == null) {
            milestone.setMilestoneTimezone(UTC_TIMEZONE);
            return;
        }
        milestone.setMilestoneTimezone(getOffset(milestone.getMilestoneTime().toString(), milestone.getHubTimeZone()));
    }
}
