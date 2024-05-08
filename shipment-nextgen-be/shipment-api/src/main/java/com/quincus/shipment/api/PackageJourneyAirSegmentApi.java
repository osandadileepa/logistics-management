package com.quincus.shipment.api;

import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilter;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilterResult;

public interface PackageJourneyAirSegmentApi {

    PackageJourneyAirSegmentFilterResult findAirlinesByOrganizationId(PackageJourneyAirSegmentFilter filter);

    PackageJourneyAirSegmentFilterResult findFlightNumbersByAirlineAndOrganizationId(PackageJourneyAirSegmentFilter filter);

    PackageJourneyAirSegmentFilterResult findAirlineOrFlightNumberByKeywordAndOrganizationId(PackageJourneyAirSegmentFilter filter);
}
