package com.quincus.finance.costing.weightcalculation.impl;

import com.quincus.finance.costing.common.exception.CostingApiError;
import com.quincus.finance.costing.weightcalculation.api.model.CheckRoundingLogicInput;
import com.quincus.finance.costing.weightcalculation.api.model.CheckRoundingLogicOutput;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Rounding Logic API", description = "This endpoint allows to calculate rounding of given parameters")
@Validated
@RequestMapping("/rounding-logic")
public interface RoundingLogicController {


    @Operation(summary = "Check Rounding Logic", description = "Check Rounding Logic", tags = "Weight Calculation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Calculated Rounded Result",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CheckRoundingLogicOutput.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Check RoundingLogic Input Input")
    @PostMapping
    ResponseEntity<CheckRoundingLogicOutput> calculate(@Validated @RequestBody CheckRoundingLogicInput request);

}
