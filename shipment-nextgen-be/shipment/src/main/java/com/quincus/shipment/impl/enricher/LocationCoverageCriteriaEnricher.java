package com.quincus.shipment.impl.enricher;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.criteria.LocationCoverageCriteria;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.LocationService;
import com.quincus.web.common.multitenant.QuincusLocationCoverage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class LocationCoverageCriteriaEnricher {
    private static final String NO_USER_LOCATION_COVERAGE = "User {} has no location coverage set";
    private final LocationService locationService;
    private final UserDetailsProvider userDetailsProvider;

    public void enrichCriteriaWithUserLocationCoverage(LocationCoverageCriteria locationCoverageCriteria) {
        List<QuincusLocationCoverage> userLocationCoverageList = userDetailsProvider.getCurrentLocationCoverages();
        if (CollectionUtils.isEmpty(userLocationCoverageList)) {
            log.debug(NO_USER_LOCATION_COVERAGE, userDetailsProvider.getCurrentUserId());
            return;
        }
        List<String> userLocCoverageExternalId = userLocationCoverageList.stream()
                .filter(Objects::nonNull).map(QuincusLocationCoverage::getId).toList();
        List<LocationEntity> userLocationCoverageEntities =
                locationService.findByOrganizationIdAndExternalIds(userLocCoverageExternalId);

        locationCoverageCriteria.setUserLocationCoverageIdsByType(locationsToLocationsByType(userLocationCoverageEntities));
    }

    private Map<LocationType, List<String>> locationsToLocationsByType(List<LocationEntity> userCoverageLocations) {
        return userCoverageLocations.stream().filter(l -> l.getType() != null)
                .collect(Collectors.groupingBy(LocationEntity::getType,
                        Collectors.mapping(LocationEntity::getId, Collectors.toList())));
    }
}