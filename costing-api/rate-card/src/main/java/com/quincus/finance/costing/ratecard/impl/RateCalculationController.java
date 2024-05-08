package com.quincus.finance.costing.ratecard.impl;

import com.quincus.finance.costing.common.exception.CostingApiError;
import com.quincus.finance.costing.ratecard.api.model.RateCalculationInput;
import com.quincus.finance.costing.ratecard.api.model.RateCalculationOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping("/rate-calculation")
@Tag(name = "Rate Calculation API", description = "This endpoint allows the client to calculate rate, given a rate calculation input")
public interface RateCalculationController {

    @Operation(summary = "Calculate Rate", description = "Calculate Rate", tags = "Rate Calculation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Calculated Rate",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = RateCalculationOutput.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Rate Calculation Input")
    @PostMapping
    ResponseEntity<RateCalculationOutput> calculate(@Valid @RequestBody RateCalculationInput request);
}
