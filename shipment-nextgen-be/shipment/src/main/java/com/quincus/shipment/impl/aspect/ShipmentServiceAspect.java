package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.resolver.LocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserGroupPermissionChecker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Aspect
@Component
@Slf4j
@AllArgsConstructor
public class ShipmentServiceAspect {

    private final LocationPermissionChecker locationPermissionChecker;
    private final UserGroupPermissionChecker userGroupPermissionChecker;

    @AfterReturning(
            value = "execution(* com.quincus.shipment.impl.service.ShipmentService.findAllByIds*(..))",
            returning = "shipments"
    )
    public void checkShipmentsOnFindAllByIds(List<Shipment> shipments) {
        Set<String> externalIds = new HashSet<>();
        shipments.forEach(shipment -> checkShipment(shipment, externalIds));
        shipments.forEach(userGroupPermissionChecker::checkUserGroupPermissions);
    }

    private void checkShipment(Shipment shipment, Set<String> externalIds) {
        List<PackageJourneySegment> packageJourneySegmentEntities = shipment.getShipmentJourney().getPackageJourneySegments();

        for (PackageJourneySegment packageJourneySegment : packageJourneySegmentEntities) {
            collectExternalIds(packageJourneySegment, externalIds);
        }
        locationPermissionChecker.checkLocationPermissions(externalIds);
    }

    private void collectExternalIds(PackageJourneySegment packageJourneySegment, Set<String> externalIds) {
        Facility startLocationHierarchy = packageJourneySegment.getStartFacility();
        Facility endLocationHierarchy = packageJourneySegment.getEndFacility();

        collectLocationExternalIds(startLocationHierarchy, externalIds);
        collectLocationExternalIds(endLocationHierarchy, externalIds);
    }

    private void collectLocationExternalIds(Facility facility, Set<String> externalIds) {
        if (facility != null) {
            externalIds.add(facility.getExternalId());
            externalIds.add(facility.getLocation().getCountryId());
            externalIds.add(facility.getLocation().getStateId());
            externalIds.add(facility.getLocation().getCityId());
        }
    }
}
