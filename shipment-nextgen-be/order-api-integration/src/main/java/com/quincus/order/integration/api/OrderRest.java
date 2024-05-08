package com.quincus.order.integration.api;

import com.quincus.order.integration.config.OrderProperties;
import com.quincus.order.integration.model.OrderResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;


@Service
@Slf4j
@NoArgsConstructor
public class OrderRest {

    private static final String ORDER_ID_EMPTY = "OrderId is empty, skipping rollback...";
    private static final String ROLLBACK_SUCCESS = "Rollback was successful: OrderId: {} ";
    private static final String SHIPMENT_MODULE_ERROR = "ShipmentV2ModuleError";
    private static final String PARAM_ORDER_ID = "order_id";
    private static final String PARAM_ERROR_MODULE = "error_module";
    private static final String PARAM_ERROR_MESSAGE = "error_message";
    @Autowired
    private OrderProperties orderProperties;
    private RestTemplate restTemplate;
    private HttpHeaders headers;

    public ResponseEntity<OrderResponse> rollback(final String orderId, final String errorMsg) {
        if (StringUtils.isEmpty(orderId)) {
            log.error(ORDER_ID_EMPTY);
        }
        String uriStr = createRollbackPayload(orderId, errorMsg);
        ResponseEntity<OrderResponse> response = restTemplate.exchange(uriStr, HttpMethod.POST, createHttpRequest(), OrderResponse.class);
        OrderResponse body = response.getBody();
        if (body != null && body.getError() != null) {
            log.error(body.getError());
        } else {
            log.info(ROLLBACK_SUCCESS, orderId);
        }
        return response;
    }


    @PostConstruct
    private void init() {
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
    }

    private HttpEntity<String> createHttpRequest() {
        if (headers.get(HttpHeaders.AUTHORIZATION) == null) {
            headers.add(HttpHeaders.AUTHORIZATION, orderProperties.getS2sToken());
        }
        return new HttpEntity<>(headers);
    }

    private String createRollbackPayload(final String orderId, final String errorMsg) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(PARAM_ORDER_ID, orderId);
        params.add(PARAM_ERROR_MODULE, SHIPMENT_MODULE_ERROR);
        params.add(PARAM_ERROR_MESSAGE, errorMsg);

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(orderProperties.getScheme())
                .host(orderProperties.getHost())
                .path(orderProperties.getRollbackApi())
                .queryParams(params)
                .build();

        return uriComponents.toUriString();
    }
}
