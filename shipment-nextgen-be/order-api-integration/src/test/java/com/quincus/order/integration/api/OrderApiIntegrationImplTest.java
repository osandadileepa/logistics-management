package com.quincus.order.integration.api;

import com.quincus.order.integration.model.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApiIntegrationImplTest {
    @InjectMocks
    private OrderApiIntegrationImpl orderApiIntegration;

    @Mock
    private OrderRest orderRest;

    @Test
    void testRollbackWithSuccessfulCall() {
        String orderId = "12345";
        String errorMessage = "Some error";

        when(orderRest.rollback(orderId, errorMessage))
                .thenReturn(ResponseEntity.ok(new OrderResponse()));

        ResponseEntity<OrderResponse> response = orderApiIntegration.rollback(orderId, errorMessage);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(orderRest, times(1)).rollback(orderId, errorMessage);
    }
    
    @Test
    void testRollbackWithMaxFailures() {
        String orderId = "12345";
        String errorMessage = "Some error";

        when(orderRest.rollback(orderId, errorMessage))
                .thenThrow(new RuntimeException("Mock exception"));

        orderApiIntegration.rollback(orderId, errorMessage);

        verify(orderRest, times(1)).rollback(orderId, errorMessage);
    }

}
