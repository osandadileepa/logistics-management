package com.quincus.order.integration.api;


import com.quincus.order.integration.model.OrderResponse;
import org.springframework.http.ResponseEntity;

public interface OrderApiIntegration {

    ResponseEntity<OrderResponse> rollback(String orderId, String errorMessage);

}
