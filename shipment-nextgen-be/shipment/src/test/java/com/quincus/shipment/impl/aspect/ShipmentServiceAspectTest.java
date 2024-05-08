package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.resolver.LocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserGroupPermissionChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceAspectTest {

    @Mock
    private LocationPermissionChecker locationPermissionChecker;
    @Mock
    private UserGroupPermissionChecker userGroupPermissionChecker;
    @InjectMocks
    private ShipmentServiceAspect shipmentServiceAspect;

    @Test
    void testCheckShipmentsOnFindAllByIds() {
        Shipment shipment1 = createShipment("ExternalID1", "Country1", "State1", "City1");
        Shipment shipment2 = createShipment("ExternalID2", "Country2", "State2", "City2");
        List<Shipment> shipments = new ArrayList<>();
        shipments.add(shipment1);
        shipments.add(shipment2);

        shipmentServiceAspect.checkShipmentsOnFindAllByIds(shipments);

        verify(locationPermissionChecker, times(2)).checkLocationPermissions(Set.of(
                "ExternalID1", "Country1", "State1", "City1",
                "ExternalID2", "Country2", "State2", "City2"
        ));
    }

    private Shipment createShipment(String externalId, String countryId, String stateId, String cityId) {
        Shipment shipment = new Shipment();
        shipment.setShipmentJourney(createShipmentJourney(externalId, countryId, stateId, cityId));
        return shipment;
    }

    private ShipmentJourney createShipmentJourney(String externalId, String countryId, String stateId, String cityId) {
        Facility startFacility = createFacility("123", externalId, countryId, stateId, cityId);
        Facility endFacility = createFacility("456", externalId, countryId, stateId, cityId);

        PackageJourneySegment packageJourneySegment = createPackageJourneySegment(startFacility, endFacility);

        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(packageJourneySegment));
        return shipmentJourney;
    }

    private PackageJourneySegment createPackageJourneySegment(Facility startFacility, Facility endFacility) {
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setStartFacility(startFacility);
        packageJourneySegment.setEndFacility(endFacility);
        return packageJourneySegment;
    }

    private Facility createFacility(String id, String externalId, String countryId, String stateId, String cityId) {
        Facility facility = new Facility();
        facility.setId(id);
        facility.setExternalId(externalId);

        Address location = new Address();
        location.setCountryId(countryId);
        location.setStateId(stateId);
        location.setCityId(cityId);

        facility.setLocation(location);

        return facility;
    }
}
