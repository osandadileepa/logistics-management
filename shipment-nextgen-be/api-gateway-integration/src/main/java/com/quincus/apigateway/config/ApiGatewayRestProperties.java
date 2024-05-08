package com.quincus.apigateway.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ConfigurationProperties(prefix = "api-gateway")
@Data
@Configuration
@PropertySource(value = {"classpath:config/api-gateway-config-${spring.profiles.active}.yml"}
        , factory = YamlPropertySourceFactory.class)
public class ApiGatewayRestProperties {

    private String baseUrl;
    private String flightPath;
    private String flightSchedulePath;
    private String assignVendorDetailsWebhook;
    private String updateOrderProgressWebhook;
    private String updateOrderAdditionalChargesWebhook;
    private String s2sToken;
    private String webhookBaseUrl;
    private String checkInWebhook;

    @Value("${spring.profiles.active}")
    private String profile;
}
