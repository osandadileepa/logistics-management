package com.quincus.shipment.impl.helper;

import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import lombok.Data;

import java.util.Map;

@Data
public class SegmentReferenceHolder {
    private Map<String, PartnerEntity> partnerBySegmentId;
    private Map<String, LocationHierarchyEntity> locationHierarchyByFacilityExtId;
}
