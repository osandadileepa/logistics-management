package com.quincus.shipment;

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

@Tag(name = "service-types", description = "This endpoint manage service types.")
@Validated
public interface ServiceTypeController {

    @GetMapping("/filter/service_types")
    @Operation(summary = "Find Service Types", description = "Return a list of service types based on request.", tags = "service-types")
    Response<FilterResult> findServiceTypes(@Min(1) @Max(100) @RequestParam("per_page") final int perPage,
                                            @Min(1) @Max(Integer.MAX_VALUE) @RequestParam final int page,
                                            @Size(max = 100) @RequestParam(required = false) final String key);

    @GetMapping("/filter/network-lane/service_types")
    @Operation(summary = "Find Network Lane Service Types", description = "Return a list of service types based on request.", tags = "service-types")
    Response<FilterResult> findServiceTypesForNetworkLane(@Min(1) @Max(100) @RequestParam("per_page") final int perPage,
                                            @Min(1) @Max(Integer.MAX_VALUE) @RequestParam final int page,
                                            @Size(max = 100) @RequestParam(required = false) final String key);
}
