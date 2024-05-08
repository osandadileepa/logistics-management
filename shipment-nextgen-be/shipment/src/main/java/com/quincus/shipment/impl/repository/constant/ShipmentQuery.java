package com.quincus.shipment.impl.repository.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class ShipmentQuery {
    public static final String SHIPMENT_TO_SEGMENT_JOIN_FETCH = """
                  LEFT JOIN FETCH shp.serviceType serviceType
                  LEFT JOIN FETCH shp.origin origin
                  LEFT JOIN FETCH shp.organization organization
                  LEFT JOIN FETCH shp.order shipmentOrder
                  LEFT JOIN FETCH shp.customer customer
                  LEFT JOIN FETCH origin.locationHierarchy originLH
                  LEFT JOIN FETCH originLH.country originCountry
                  LEFT JOIN FETCH originLH.state originState
                  LEFT JOIN FETCH originLH.city originCity
                  LEFT JOIN FETCH originLH.facility originFacility
                  LEFT JOIN FETCH shp.destination destination
                  LEFT JOIN FETCH destination.locationHierarchy destinationLH
                  LEFT JOIN FETCH destinationLH.country destinationCountry
                  LEFT JOIN FETCH destinationLH.state destinationState
                  LEFT JOIN FETCH destinationLH.city destinationCity
                  LEFT JOIN FETCH destinationLH.facility destinationFacility
                  LEFT JOIN FETCH shp.shipmentPackage package
                  LEFT JOIN FETCH package.dimension
                  LEFT JOIN FETCH shp.shipmentJourney shipmentJourney
                  LEFT JOIN FETCH shipmentJourney.packageJourneySegments segment
                  LEFT JOIN FETCH segment.partner segmentPartner
                  LEFT JOIN FETCH segment.startLocationHierarchy startLH
                  LEFT JOIN FETCH startLH.country startCountry
                  LEFT JOIN FETCH startLH.state startState
                  LEFT JOIN FETCH startLH.city startCity
                  LEFT JOIN FETCH startLH.facility startFacility
                  LEFT JOIN FETCH segment.endLocationHierarchy endLH
                  LEFT JOIN FETCH endLH.country endCountry
                  LEFT JOIN FETCH endLH.state endState
                  LEFT JOIN FETCH endLH.city endCity
                  LEFT JOIN FETCH endLH.facility endFacility
            """;

    public static final String SHIPMENT_TO_COMMODITY_JOIN_FETCH = """
                  LEFT JOIN FETCH shp.serviceType serviceType
                  LEFT JOIN FETCH shp.origin origin
                  LEFT JOIN FETCH shp.organization organization
                  LEFT JOIN FETCH shp.order shipmentOrder
                  LEFT JOIN FETCH shp.customer customer
                  LEFT JOIN FETCH origin.locationHierarchy originLH
                  LEFT JOIN FETCH originLH.country originCountry
                  LEFT JOIN FETCH originLH.state originState
                  LEFT JOIN FETCH originLH.city originCity
                  LEFT JOIN FETCH originLH.facility originFacility
                  LEFT JOIN FETCH shp.destination destination
                  LEFT JOIN FETCH destination.locationHierarchy destinationLH
                  LEFT JOIN FETCH destinationLH.country destinationCountry
                  LEFT JOIN FETCH destinationLH.state destinationState
                  LEFT JOIN FETCH destinationLH.city destinationCity
                  LEFT JOIN FETCH destinationLH.facility destinationFacility
                  LEFT JOIN FETCH shp.shipmentPackage package
                  LEFT JOIN FETCH package.dimension
                  LEFT JOIN FETCH package.commodities commodity
            """;

    public static final String SHIPMENT_JOIN_FETCH = """
                  LEFT JOIN FETCH shp.serviceType serviceType
                  LEFT JOIN FETCH shp.origin origin
                  LEFT JOIN FETCH shp.organization organization
                  LEFT JOIN FETCH shp.order shipmentOrder
                  LEFT JOIN FETCH shp.customer customer
                  LEFT JOIN FETCH shp.shipmentJourney shipmentJourney
                  LEFT JOIN FETCH shp.shipmentPackage shipmentPackage
                  LEFT JOIN FETCH shipmentPackage.dimension dimension
                  LEFT JOIN FETCH origin.locationHierarchy originLH
                  LEFT JOIN FETCH originLH.country originCountry
                  LEFT JOIN FETCH originLH.state originState
                  LEFT JOIN FETCH originLH.city originCity
                  LEFT JOIN FETCH originLH.facility originFacility
                  LEFT JOIN FETCH shp.destination destination
                  LEFT JOIN FETCH destination.locationHierarchy destinationLH
                  LEFT JOIN FETCH destinationLH.country destinationCountry
                  LEFT JOIN FETCH destinationLH.state destinationState
                  LEFT JOIN FETCH destinationLH.city destinationCity
                  LEFT JOIN FETCH destinationLH.facility destinationFacility
                  LEFT JOIN FETCH shp.shipmentPackage package
                  LEFT JOIN FETCH package.dimension
            """;
}
