package com.quincus.shipment.impl.web;

import com.quincus.shipment.PackageJourneyAirSegmentController;
import com.quincus.shipment.api.PackageJourneyAirSegmentApi;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilter;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilterResult;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PackageJourneyAirSegmentControllerImpl implements PackageJourneyAirSegmentController {
    private final PackageJourneyAirSegmentApi filterApi;

    @Override
    @LogExecutionTime
    public Response<PackageJourneyAirSegmentFilterResult> findAirlines(final int perPage,
                                                                       final int page) {
        PackageJourneyAirSegmentFilter filter = new PackageJourneyAirSegmentFilter();
        filter.setPage(page);
        filter.setPerPage(perPage);
        return new Response<>(filterApi.findAirlinesByOrganizationId(filter));
    }

    @Override
    @LogExecutionTime
    public Response<PackageJourneyAirSegmentFilterResult> findFlightNumbersByAirline(final String airline,
                                                                                     final int perPage,
                                                                                     final int page) {
        PackageJourneyAirSegmentFilter filter = new PackageJourneyAirSegmentFilter();
        filter.setAirline(airline);
        filter.setPage(page);
        filter.setPerPage(perPage);
        return new Response<>(filterApi.findFlightNumbersByAirlineAndOrganizationId(filter));
    }

    @Override
    @LogExecutionTime
    public Response<PackageJourneyAirSegmentFilterResult> findAirlineOrFlightNumberByKeyword(final String key,
                                                                                             final int perPage,
                                                                                             final int page,
                                                                                             final int level) {
        PackageJourneyAirSegmentFilter filter = new PackageJourneyAirSegmentFilter();
        filter.setKey(key);
        filter.setLevel(level);
        filter.setPage(page);
        filter.setPerPage(perPage);
        return new Response<>(filterApi.findAirlineOrFlightNumberByKeywordAndOrganizationId(filter));
    }
}
