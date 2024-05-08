package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@AllArgsConstructor
public class LocationPermissionChecker {

    private final UserDetailsProvider userDetailsProvider;

    public void checkLocationPermissions(Set<String> externalIds) {
        if (userDetailsProvider.isFromPreAuthenticatedSource()) return;
        
        if (userDetailsProvider.getCurrentLocationCoverageIds().stream().noneMatch(externalIds::contains)) {
            throw new SegmentLocationNotAllowedException("No access location coverages for the segment.", ShipmentErrorCode.ACCESS_LOCATION_NOT_ALLOWED);
        }
    }

    public void checkLocationPermissions(CostEntity costEntity) {
        checkLocationPermissions(costEntity.getLocationExternalIds());
    }
}
