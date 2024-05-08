package com.quincus.shipment;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Tag(name = "locations", description = "This endpoint manage locations.")
@Validated
public interface LocationController {

    @GetMapping("/filter/locations")
    @Operation(summary = "Find Locations API", description = "Return a list of locations based on request.", tags = "locations")
    Response<FilterResult> findLocations(@Valid @RequestParam final LocationType type,
                                         @Min(1) @Max(100) @RequestParam("per_page") final int perPage,
                                         @Min(1) @Max(Integer.MAX_VALUE) @RequestParam final int page,
                                         @Size(max = 100) @RequestParam(required = false) final String key,
                                         @RequestParam(name = "sort_by", required = false, defaultValue = "name") final String sortBy);

}
