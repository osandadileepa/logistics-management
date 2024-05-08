package com.quincus.finance.costing.ratecard.impl;

import com.quincus.finance.costing.common.exception.CostingApiError;
import com.quincus.finance.costing.ratecard.api.model.RateCard;
import com.quincus.finance.costing.ratecard.impl.validator.RateCardConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Rate Card API", description = "These endpoints allows the client to run CRUD functionalities for Rate Cards")
@Validated
@RequestMapping("/rate-cards")
public interface RateCardController {


    @Operation(summary = "Create Rate Card", description = "Create Rate Card", tags = "Rate Cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Created Rate Card",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = RateCard.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Rate Card Request")
    @PostMapping
    ResponseEntity<RateCard> create(@Validated @RateCardConstraint @RequestBody RateCard request);


    @Operation(summary = "Update Rate Card", description = "Update Rate Card", tags = "Rate Cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Updated Rate Card",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = RateCard.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Rate Card Id")
    @Parameter(description = "Rate Card Request")
    @PutMapping(path = {"/{id}"})
    ResponseEntity<RateCard> update(
            @PathVariable("id") String id,
            @Validated @RateCardConstraint @RequestBody RateCard request
    );

    @Operation(summary = "Find Rate Card", description = "Find Rate Card", tags = "Rate Cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Rate Card",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = RateCard.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Rate Card Id")
    @GetMapping(path = {"/{id}"})
    ResponseEntity<RateCard> get(@PathVariable("id") String id);


    @Operation(summary = "Delete Rate Card", description = "Find Rate Card", tags = "Rate Cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Rate Card Id")
    @DeleteMapping(path = {"/{id}"})
    void delete(@PathVariable("id") String id);
}

