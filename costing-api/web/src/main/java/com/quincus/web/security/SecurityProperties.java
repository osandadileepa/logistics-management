package com.quincus.web.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.path")
public class SecurityProperties {
    private String[] allowed;
    private String[] origins;
    private String[] methods;
    private String[] headers;
}
