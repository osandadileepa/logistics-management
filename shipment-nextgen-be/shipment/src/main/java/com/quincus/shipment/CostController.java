package com.quincus.shipment;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.CostFilterResult;
import com.quincus.shipment.api.filter.CostShipmentFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@RequestMapping("/costs")
@Tag(name = "costs", description = "This endpoint allows to manage cost related transactions.")
@Validated
public interface CostController {

    @GetMapping("/{id}")
    @Operation(summary = "Find Cost API", description = "Find an existing cost.", tags = "costs")
    Response<Cost> find(@UUID @PathVariable("id") final String id);

    @PostMapping
    @Operation(summary = "Create Cost API", description = "Create a new cost.", tags = "costs")
    Response<Cost> add(@Valid @RequestBody final Request<Cost> request);

    @PutMapping("/{id}")
    @Operation(summary = "Update Cost API", description = "Update an existing shipment.", tags = "costs")
    Response<Cost> update(@Valid @RequestBody final Request<Cost> request, @UUID @PathVariable("id") final String id);


    @PostMapping("/shipments/search")
    @Operation(summary = "Find All Cost Shipment API", description = "Returns a list of cost shipments based on the input of shipment tracking and order IDs.", tags = "costs")
    Response<CostShipmentFilterResult> findAllCostShipmentByFilter(@Valid @RequestBody final Request<ShipmentFilter> shipmentFilter);

    @PostMapping("/list")
    @Operation(summary = "Find All Costs API", description = "Return a list of cost based on filter.", tags = "costs")
    Response<CostFilterResult> findAllByFilter(@Valid @RequestBody final Request<CostFilter> costFilter);

}
