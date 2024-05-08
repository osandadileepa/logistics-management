package com.quincus.order.integration.api;

import com.quincus.order.integration.model.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrderApiIntegrationImpl implements OrderApiIntegration {
    private static final String MDC_UUID = "UUID";
    private static final int MAX_RETRY = 2;
    private final OrderRest orderRest;

    @Override
    public ResponseEntity<OrderResponse> rollback(String orderId, String errorMessage) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                return orderRest.rollback(orderId, errorMessage);
            } catch (Exception e) {
                retryCount++;
                log.error("Error encountered during rollback attempt `{}` for order id: `{}` and MDC id: `{}` . Exception: ", retryCount, orderId, MDC.get(MDC_UUID), e);
                errorMessage = "Error encountered during rollback. Detailed logs available.";
            }
        }
        // Return an OK response regardless of the success of the rollback after retries
        return ResponseEntity.ok(new OrderResponse());
    }
}
