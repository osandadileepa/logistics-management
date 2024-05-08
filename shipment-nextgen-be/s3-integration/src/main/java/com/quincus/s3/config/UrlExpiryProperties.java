package com.quincus.s3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "s3-integration.url-expiry")
public class UrlExpiryProperties {
    private String upload;
    private String read;
}
