package com.quincus.finance.costing.weightcalculation.impl;

import com.quincus.finance.costing.common.exception.CostingApiError;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Weight Calculation API", description = "This endpoints allows the client to calculate weight, given a weight calculation input")
@Validated
@RequestMapping("/weight-calculation")
public interface WeightCalculationController {
    
    @Operation(summary = "Calculate Weight", description = "Calculate Weight", tags = "Weight Calculation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Calculated Weight Calculation",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WeightCalculationOutput.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Weight Calculation Input")
    @PostMapping
    ResponseEntity<WeightCalculationOutput> calculate(@Valid @RequestBody WeightCalculationInput request);

}
