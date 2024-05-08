package com.quincus.shipment;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Tag(name = "location-hierarchy", description = "This endpoint manage location hierarchy.")
@Validated
public interface LocationHierarchyController {

    @GetMapping("/filter/location_hierarchies")
    @Operation(summary = "Find Location Hierarchies API", description = "Return a location hierarchies based on request parameters.", tags = "location-hierarchy")
    Response<FilterResult> findLocationHierarchies(@UUID(required = false) @RequestParam(name = "country_id", required = false) final String countryId,
                                                   @UUID(required = false) @RequestParam(name = "state_id", required = false) final String stateId,
                                                   @UUID(required = false) @RequestParam(name = "city_id", required = false) final String cityId,
                                                   @UUID(required = false) @RequestParam(name = "facility_id", required = false) final String facilityId,
                                                   @Min(1) @Max(Integer.MAX_VALUE) @RequestParam final int page,
                                                   @Min(1) @Max(100) @RequestParam final int per_page,
                                                   @Size(max = 2000) @RequestParam(required = false) final String key,
                                                   @Max(4) @RequestParam(required = false, defaultValue = "3") int level);

    @GetMapping("/filter/states")
    @Operation(summary = "Find All States by Country API", description = "Return a list of states based on request.", tags = "location-hierarchy")
    Response<FilterResult> findAllStatesByCountry(@UUID @RequestParam("country_id") final String countryId,
                                                  @Min(1) @Max(100) @RequestParam("per_page") final int perPage,
                                                  @Min(1) @Max(Integer.MAX_VALUE) @RequestParam final int page,
                                                  @Size(max = 20) @RequestParam(name = "sort_by", required = false, defaultValue = "name") final String sortBy);

    @GetMapping("/filter/cities")
    @Operation(summary = "Find All Cities by State and Country API", description = "Return a list of cities based on request.", tags = "location-hierarchy")
    Response<FilterResult> findAllCitiesByStateCountry(@UUID @RequestParam("country_id") final String countryId,
                                                       @UUID @RequestParam("state_id") final String stateId,
                                                       @Min(1) @Max(100) @RequestParam("per_page") final int perPage,
                                                       @Min(1) @Max(Integer.MAX_VALUE) @RequestParam final int page,
                                                       @Size(max = 20) @RequestParam(name = "sort_by", required = false, defaultValue = "name") final String sortBy);

    @GetMapping("/filter/facilities")
    @Operation(summary = "Find All Facilities by City State Country API", description = "Return a list of facilities based on request.", tags = "location-hierarchy")
    Response<FilterResult> findAllFacilitiesByCityStateCountry(@UUID @RequestParam("country_id") final String countryId,
                                                               @UUID @RequestParam("state_id") final String stateId,
                                                               @UUID @RequestParam("city_id") final String cityId,
                                                               @Min(1) @Max(100) @RequestParam("per_page") final int perPage,
                                                               @Min(1) @Max(Integer.MAX_VALUE) @RequestParam final int page,
                                                               @Size(max = 20) @RequestParam(name = "sort_by", required = false, defaultValue = "name") final String sortBy);

}