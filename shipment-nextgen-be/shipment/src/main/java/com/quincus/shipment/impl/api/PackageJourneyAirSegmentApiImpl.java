package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.PackageJourneyAirSegmentApi;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilter;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilterResult;
import com.quincus.shipment.impl.service.PackageJourneyAirSegmentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PackageJourneyAirSegmentApiImpl implements PackageJourneyAirSegmentApi {

    private final PackageJourneyAirSegmentService packageJourneyAirSegmentService;

    @Override
    public PackageJourneyAirSegmentFilterResult findAirlinesByOrganizationId(PackageJourneyAirSegmentFilter filter) {
        return packageJourneyAirSegmentService.findAirlines(filter);
    }

    @Override
    public PackageJourneyAirSegmentFilterResult findFlightNumbersByAirlineAndOrganizationId(PackageJourneyAirSegmentFilter filter) {
        return packageJourneyAirSegmentService.findFlightNumbers(filter);
    }

    @Override
    public PackageJourneyAirSegmentFilterResult findAirlineOrFlightNumberByKeywordAndOrganizationId(PackageJourneyAirSegmentFilter filter) {
        return packageJourneyAirSegmentService.findAirlinesOrFlightNumbers(filter);
    }
}
