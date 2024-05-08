package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.impl.repository.AlertRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.web.common.multitenant.QuincusLocationCoverage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
@AllArgsConstructor
public class FacilityLocationPermissionChecker {

    private final UserDetailsProvider userDetailsProvider;
    private final ShipmentRepository shipmentRepository;
    private final AlertRepository alertRepository;

    public boolean isShipmentIdAnySegmentLocationCovered(String shipmentId) {
        return isIdRelatedToAnySegmentLocationCovered(shipmentId, IdType.SHIPMENT_ID);
    }

    public boolean isShipmentTrackingIdAnySegmentLocationCovered(String shipmentTrackingId) {
        return isIdRelatedToAnySegmentLocationCovered(shipmentTrackingId, IdType.SHIPMENT_TRACKING_ID);
    }

    public boolean isAlertIdAnySegmentLocationCovered(String alertId) {
        return isIdRelatedToAnySegmentLocationCovered(alertId, IdType.ALERT_ID);
    }

    public boolean isFacilityLocationCovered(Facility facility) {
        if (facility == null) {
            return false;
        }

        List<QuincusLocationCoverage> locationCoverages = userDetailsProvider.getCurrentLocationCoverages();
        if (CollectionUtils.isEmpty(locationCoverages)) {
            return false;
        }

        return isFacilityCovered(facility, userDetailsProvider.getFacilitySpecificCoverage())
                || isCityCovered(facility, userDetailsProvider.getCityWideCoverage())
                || isStateCovered(facility, userDetailsProvider.getStateWideCoverage())
                || isCountryCovered(facility, userDetailsProvider.getCountryWideCoverage());
    }

    private boolean isIdRelatedToAnySegmentLocationCovered(String id, IdType idType) {
        if (userDetailsProvider.isFromPreAuthenticatedSource()) return true;

        if (StringUtils.isEmpty(id)) {
            return false;
        }

        List<QuincusLocationCoverage> locationCoverages = userDetailsProvider.getCurrentLocationCoverages();
        if (CollectionUtils.isEmpty(locationCoverages)) {
            return false;
        }

        if (IdType.SHIPMENT_TRACKING_ID == idType) {
            return shipmentRepository.isShipmentTrackingIdAnySegmentLocationCovered(id,
                    userDetailsProvider.getFacilityIdCoverages(), userDetailsProvider.getCityIdCoverages(),
                    userDetailsProvider.getStateIdCoverages(), userDetailsProvider.getCountryIdCoverages());
        }

        if (IdType.ALERT_ID == idType) {
            return alertRepository.isAlertFromSegmentLocationCovered(id, userDetailsProvider.getFacilityIdCoverages(),
                    userDetailsProvider.getCityIdCoverages(), userDetailsProvider.getStateIdCoverages(),
                    userDetailsProvider.getCountryIdCoverages());
        }

        return shipmentRepository.isShipmentIdAnySegmentLocationCovered(
                id,
                userDetailsProvider.getFacilityIdCoverages(),
                userDetailsProvider.getCityIdCoverages(),
                userDetailsProvider.getStateIdCoverages(),
                userDetailsProvider.getCountryIdCoverages());
    }

    private boolean isFacilityCovered(Facility facility, Set<QuincusLocationCoverage> facilityCoverages) {
        return isLocationCovered(facilityCoverages, facility.getExternalId());
    }

    private boolean isCityCovered(Facility facility, Set<QuincusLocationCoverage> cityCoverages) {
        if (facility.getLocation() == null) {
            return false;
        }
        return isLocationCovered(cityCoverages, facility.getLocation().getCityId());
    }

    private boolean isStateCovered(Facility facility, Set<QuincusLocationCoverage> stateCoverages) {
        if (facility.getLocation() == null) {
            return false;
        }
        return isLocationCovered(stateCoverages, facility.getLocation().getStateId());
    }

    private boolean isCountryCovered(Facility facility, Set<QuincusLocationCoverage> countryCoverages) {
        if (facility.getLocation() == null) {
            return false;
        }
        return isLocationCovered(countryCoverages, facility.getLocation().getCountryId());
    }

    private boolean isLocationCovered(Set<QuincusLocationCoverage> locationCoverages, String refLocationId) {
        if (CollectionUtils.isEmpty(locationCoverages)) {
            return false;
        }
        return locationCoverages.stream().
                anyMatch(locationCoverage -> locationCoverage.getId().equals(refLocationId));
    }

    private enum IdType {
        SHIPMENT_ID,
        SHIPMENT_TRACKING_ID,
        ALERT_ID
    }
}
