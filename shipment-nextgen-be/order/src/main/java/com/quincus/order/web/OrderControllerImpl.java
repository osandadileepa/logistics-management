package com.quincus.order.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.OrderController;
import com.quincus.order.api.OrderApi;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.OrderShipmentResponse;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class OrderControllerImpl implements OrderController {
    private static final String ERROR_PARSING_ORDER_PAYLOAD = "Error parsing Order payload.";
    private static final String ERROR_ORDER_NO_SHIPMENT = "Order %s does not contain any shipment.";
    private final OrderApi orderApi;
    private final MessageApi messageApi;
    private final ObjectMapper objectMapper;

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN','S2S')")
    @LogExecutionTime
    public Response<List<Shipment>> createFromOrder(final Request<Object> orderRequest) {
        try {
            String orderMessagePayload = objectMapper.writeValueAsString(orderRequest.getData());
            Order order = orderApi.createOrderFromPayload(orderMessagePayload);
            List<Shipment> shipments = orderApi.createOrUpdateShipments(order, orderMessagePayload, UUID.randomUUID().toString());
            if (CollectionUtils.isEmpty(shipments)) {
                throw new QuincusValidationException(String.format(ERROR_ORDER_NO_SHIPMENT, order.getId()));
            }
            SegmentDispatchType dispatchType;
            if (shipments.stream().anyMatch(Shipment::isSegmentsUpdatedFromSource)) {
                dispatchType = SegmentDispatchType.JOURNEY_UPDATED;
            } else if (shipments.stream().anyMatch(Shipment::isUpdated)) {
                dispatchType = SegmentDispatchType.SHIPMENT_UPDATED;
            } else {
                dispatchType = SegmentDispatchType.SHIPMENT_CREATED;
            }
            ShipmentJourney refJourney = shipments.get(0).getShipmentJourney();
            messageApi.sendShipmentWithJourneyToQShip(shipments.get(0), refJourney);
            if (orderApi.isOrderNotCancelled(order)) {
                messageApi.sendSegmentDispatch(shipments, refJourney, dispatchType, DspSegmentMsgUpdateSource.CLIENT);
            }
            return new Response<>(shipments);
        } catch (JsonProcessingException e) {
            throw new QuincusValidationException(ERROR_PARSING_ORDER_PAYLOAD);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN','S2S')")
    @LogExecutionTime
    public Response<OrderShipmentResponse> asyncCreateFromOrder(Request<Object> orderRequest) {
        try {
            String orderMessagePayload = objectMapper.writeValueAsString(orderRequest.getData());
            Order order = orderApi.createOrderFromPayload(orderMessagePayload);
            return new Response<>(orderApi.asyncCreateOrUpdateShipments(order, orderMessagePayload, UUID.randomUUID().toString()));
        } catch (JsonProcessingException e) {
            throw new QuincusValidationException(ERROR_PARSING_ORDER_PAYLOAD);
        }
    }

}
