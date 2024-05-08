package com.quincus.shipment.impl.specification.predicate;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.repository.specification.predicate.StateLocationCoveragePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class StateLocationCoveragePredicateTest {

    private final ShipmentLocationCoveragePredicate predicate = new StateLocationCoveragePredicate();
    @Mock
    Map<LocationType, List<String>> userLocationCoverageByType;
    @Mock
    CriteriaBuilder criteriaBuilder;
    @Mock
    Join<PackageJourneySegmentEntity, LocationHierarchyEntity> locationHierarchyJoin;
    @Mock
    Join<LocationHierarchyEntity, LocationEntity> locationJoin;
    @Mock
    Predicate mockPredicate;
    @Mock
    Path<String> locationEntityPath;

    @Test
    @DisplayName("GIVEN UserLocationCoverage has no State assigned WHEN building predicates THEN return should be null")
    void givenNoUserCoverageForState_WhenConstructPredicate_ThenShouldReturnNull() {
        //WHEN:
        Predicate result = predicate.constructPredicate(locationHierarchyJoin, criteriaBuilder, userLocationCoverageByType);
        //THEN:
        assertThat(result).withFailMessage("Predicate should be null").isNull();
    }

    @Test
    @DisplayName("GIVEN UserLocationCoverage has  State assigned WHEN building predicates THEN return should be null")
    void givenUserHasCoverageForState_WhenConstructPredicate_ThenShouldReturnPredicate() {
        //GIVEN:
        when(userLocationCoverageByType.get(LocationType.STATE)).thenReturn(Arrays.asList("1", "2"));
        when(locationHierarchyJoin.join(LocationHierarchyEntity_.state)).thenReturn(locationJoin);
        when(locationJoin.get(BaseEntity_.id)).thenReturn(locationEntityPath);
        when(locationEntityPath.in(Arrays.asList("1", "2"))).thenReturn(mockPredicate);
        //WHEN:
        Predicate result = predicate.constructPredicate(locationHierarchyJoin, criteriaBuilder, userLocationCoverageByType);
        //THEN:
        assertThat(result).withFailMessage("Predicate should not be null").isNotNull();
        verify(locationEntityPath, times(1)).in(Arrays.asList("1", "2"));
    }
}
