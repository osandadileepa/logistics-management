package com.quincus.shipment.impl.repository.specification.predicate;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
@Component
public class FacilityLocationCoveragePredicate implements ShipmentLocationCoveragePredicate {
    @Override
    public Predicate constructPredicate(Join<PackageJourneySegmentEntity, LocationHierarchyEntity> locationHierarchyJoin, CriteriaBuilder criteriaBuilder, Map<LocationType, List<String>> userLocationCoverageByType) {
        List<String> userFacilityLocationCoverage = userLocationCoverageByType.get(LocationType.FACILITY);
        if (CollectionUtils.isEmpty(userFacilityLocationCoverage)) {
            return null;
        }
        return locationHierarchyJoin.join(LocationHierarchyEntity_.facility).get(BaseEntity_.id).in(userFacilityLocationCoverage);

    }
}
