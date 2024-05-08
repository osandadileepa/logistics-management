package com.quincus.order.integration.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ConfigurationProperties(prefix = "order")
@Data
@Configuration
@PropertySource(value = {"classpath:config/order-config-${spring.profiles.active}.yml"}
        , factory = YamlPropertySourceFactory.class)
public class OrderProperties {

    private String s2sToken;
    private String host;
    private String rollbackApi;
    private String scheme;
    @Value("${spring.profiles.active}")
    private String profile;
}
