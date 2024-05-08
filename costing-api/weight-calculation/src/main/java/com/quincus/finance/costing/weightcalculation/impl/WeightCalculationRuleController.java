package com.quincus.finance.costing.weightcalculation.impl;

import com.quincus.finance.costing.common.exception.CostingApiError;
import com.quincus.finance.costing.common.web.model.filter.CostingApiFilterResult;
import com.quincus.finance.costing.weightcalculation.api.filter.WeightCalculationRuleFilter;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import com.quincus.finance.costing.weightcalculation.impl.validator.SpecialVolumeWeightTemplateConstraint;
import com.quincus.finance.costing.weightcalculation.impl.validator.WeightCalculationRuleConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Weight Calculation Rule API", description = "These endpoints allows the client to run CRUD functionalities and search for Weight Calculation Rules")
@Validated
@RequestMapping("/weight-calculation-rules")
public interface WeightCalculationRuleController {


    @Operation(summary = "Create Weight Calculation Rule", description = "Create Weight Calculation Rule", tags = "Weight Calculation Rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Created Weight Calculation Rule",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WeightCalculationRule.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Weight Calculation Rule data")
    @Parameter(description = "MultipartFile file")
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity<WeightCalculationRule> create(
            @RequestPart("data")
            @Validated @WeightCalculationRuleConstraint WeightCalculationRule data,
            @RequestPart(value = "file", required = false)
            @Validated @SpecialVolumeWeightTemplateConstraint MultipartFile file
    );

    @Operation(summary = "Update Weight Calculation Rule", description = "Update Weight Calculation Rule", tags = "Weight Calculation Rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Updated Weight Calculation Rule",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WeightCalculationRule.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Weight Calculation Rule Id")
    @Parameter(description = "Weight Calculation Rule data")
    @Parameter(description = "MultipartFile file")
    @PutMapping(path = {"/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity<WeightCalculationRule> update(
            @PathVariable("id") String id,
            @RequestPart("data")
            @Validated @WeightCalculationRuleConstraint WeightCalculationRule data,
            @RequestPart(value = "file", required = false)
            @Validated @SpecialVolumeWeightTemplateConstraint MultipartFile file
    );

    @Operation(summary = "Get Weight Calculation Rule by ID", description = "Get Weight Calculation Rule by ID", tags = "Weight Calculation Rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Weight Calculation Rule",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WeightCalculationRule.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Weight Calculation Rule Id")
    @GetMapping(path = {"/{id}"})
    ResponseEntity<WeightCalculationRule> get(@PathVariable("id") String id);


    @Operation(summary = "Search Weight Calculation Rule", description = "Search Weight Calculation Rule", tags = "Weight Calculation Rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Weight Calculation Rules",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiFilterResult.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Weight Calculation Rule Filter")
    @GetMapping(path = {"/search"})
    ResponseEntity<CostingApiFilterResult<WeightCalculationRule>> search(
            WeightCalculationRuleFilter filter,
            @PageableDefault(sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable
    );


    @Operation(summary = "Delete Weight Calculation Rule", description = "Delete Weight Calculation Rule", tags = "Weight Calculation Rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Weight Calculation Rule Filter")
    @DeleteMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    void delete(@PathVariable("id") String id);


    @Operation(summary = "Get the default Weight Calculation Rule by organizationId", description = "Get the default Weight Calculation Rule by organizationId", tags = "Weight Calculation Rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Returns Weight Calculation Rules",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiFilterResult.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class))),
            @ApiResponse(responseCode = "500",
                    description = "Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CostingApiError.class)))
    })
    @Parameter(description = "Weight Calculation Rule Filter")
    @GetMapping(path = {"/default/{organizationId}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<WeightCalculationRule> getDefaultRuleByOrganizationId(@PathVariable("organizationId") String organizationId);
}
