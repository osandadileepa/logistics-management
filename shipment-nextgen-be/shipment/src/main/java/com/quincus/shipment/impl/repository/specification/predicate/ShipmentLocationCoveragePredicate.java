package com.quincus.shipment.impl.repository.specification.predicate;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;

public interface ShipmentLocationCoveragePredicate {

    Predicate constructPredicate(Join<PackageJourneySegmentEntity, LocationHierarchyEntity> locationHierarchyJoin, CriteriaBuilder criteriaBuilder, Map<LocationType, List<String>> userLocationCoverageByType);
}
