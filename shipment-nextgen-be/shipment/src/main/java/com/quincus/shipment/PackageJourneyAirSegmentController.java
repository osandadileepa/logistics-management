package com.quincus.shipment;

import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilterResult;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/package-journey-air-segment")
@Tag(name = "package-journey-air-segment", description = "This endpoint manages package journey air segments.")
public interface PackageJourneyAirSegmentController {

    @GetMapping("/filter/airlines")
    @Operation(summary = "Find Airlines", description = "Returns a list of airlines associated with a specific organization ID.", tags = "package-journey-air-segment")
    Response<PackageJourneyAirSegmentFilterResult> findAirlines(@RequestParam(name = "per_page", required = false, defaultValue = "10") final int perPage,
                                                                @RequestParam(name = "page", required = false, defaultValue = "1") final int page);

    @GetMapping("/filter/airlines/{airline}")
    @Operation(summary = "Find Flight Numbers by Airline Name", description = "Returns a list of airlines based on a given airline name and organization ID.", tags = "package-journey-air-segment")
    Response<PackageJourneyAirSegmentFilterResult> findFlightNumbersByAirline(@PathVariable(name = "airline") final String airline,
                                                                              @RequestParam(name = "per_page", required = false, defaultValue = "10") final int perPage,
                                                                              @RequestParam(name = "page", required = false, defaultValue = "1") final int page);

    @GetMapping("/filter/airlines/hierarchies")
    @Operation(summary = "Find Airlines or with Flight Numbers by given criteria", description = "Returns a list of airlines or flight numbers based on a given keyword and organization ID.", tags = "package-journey-air-segment")
    Response<PackageJourneyAirSegmentFilterResult> findAirlineOrFlightNumberByKeyword(@RequestParam(name = "key", required = false) final String key,
                                                                                      @RequestParam(name = "per_page", required = false, defaultValue = "10") final int perPage,
                                                                                      @RequestParam(name = "page", required = false, defaultValue = "1") final int page,
                                                                                      @RequestParam(name = "level", required = false, defaultValue = "1") final int level);
}