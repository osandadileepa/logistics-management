package com.quincus.order;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.OrderShipmentResponse;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "orders", description = "This API endpoint processes orders into shipments.")
public interface OrderController {

    @PostMapping("/orders")
    @Operation(summary = "Create Shipments From Order API", description = "Translates an order to shipments.", tags = "orders")
    Response<List<Shipment>> createFromOrder(@RequestBody final Request<Object> request);

    @PostMapping("/v2/orders")
    @Operation(summary = "Async Create Shipments From Order API", description = "Translates an order to shipments asynchronously.", tags = "orders")
    Response<OrderShipmentResponse> asyncCreateFromOrder(@RequestBody final Request<Object> request);
}
