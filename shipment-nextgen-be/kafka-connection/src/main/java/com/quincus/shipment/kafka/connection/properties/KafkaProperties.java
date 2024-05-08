package com.quincus.shipment.kafka.connection.properties;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Data
public class KafkaProperties {
    static final String PATTERN = "{0}{1}";

    @Value("${kafka.connection.url}")
    private String url;

    @Value("${kafka.module}")
    private String module;

    @Value("${spring.profiles.active}")
    private String profile;

    String getEnvironment() {
        return StringUtils.equalsIgnoreCase(getProfile(), "test") ? "qa" : getProfile();
    }
}
