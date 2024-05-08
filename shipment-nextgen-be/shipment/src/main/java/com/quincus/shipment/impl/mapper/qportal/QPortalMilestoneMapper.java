package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.shipment.api.domain.MilestoneLookup;
import com.quincus.shipment.api.dto.MilestoneResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalMilestoneMapper {
    MilestoneLookup toMilestoneLookup(QPortalMilestone qPortalMilestone);

    MilestoneResponse toMilestoneCodeResponse(QPortalMilestone qPortalMilestone);
}
