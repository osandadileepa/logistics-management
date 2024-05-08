package com.quincus.shipment;

import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
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

@RequestMapping("/network-lane")
@Tag(name = "network-lane", description = "This endpoint manage network lane.")
@Validated
public interface NetworkLaneController {

    @PostMapping("/list")
    @Operation(summary = "Find NetworkLane API", description = "Return a list of network-lane based on filter.", tags = "network-lane")
    Response<NetworkLaneFilterResult> findAll(@Valid @RequestBody final Request<NetworkLaneFilter> requestFilter);

    @PutMapping
    @Operation(summary = "Update NetworkLane API", description = "Update an existing networkLane.", tags = "network-lane")
    Response<NetworkLane> update(@Valid @RequestBody final Request<NetworkLane> request);

    @GetMapping("/{id}")
    @Operation(summary = "Find NetworkLane By ID API ", description = "Find an existing NetworkLane using the ID.", tags = "network-lane")
    Response<NetworkLane> findById(@PathVariable("id") final String id);

}
