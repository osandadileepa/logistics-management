package com.quincus.order.integration.api;

import com.quincus.order.integration.config.OrderProperties;
import com.quincus.order.integration.model.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRestTest {

    @InjectMocks
    private OrderRest orderRest;
    @Mock
    private OrderProperties orderProperties;

    @Spy
    private HttpHeaders headers = new HttpHeaders();

    @Mock
    private RestTemplate restTemplate;

    @Test
    void testRollback() {
        String orderId = "4fa3c2b4-925c-43fe-bda6-1f807965c6ec";
        String errorMessage = "This is a test error message";
        OrderResponse oResp = new OrderResponse();
        ResponseEntity<OrderResponse> response = new ResponseEntity<>(oResp, new HttpHeaders(), HttpStatus.OK);

        when(orderProperties.getScheme()).thenReturn("https");
        when(orderProperties.getHost()).thenReturn("api.order.test.quincus.com");
        when(orderProperties.getRollbackApi()).thenReturn("/api_s2s/v1/rollback");
        when(orderProperties.getS2sToken()).thenReturn("8ClAROQXtEThb1KSjztLPA");
        when(restTemplate
                .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))
        ).thenReturn(response);
        var resp = orderRest.rollback(orderId, errorMessage);
        assertThat(resp).isNotNull();
    }
}
