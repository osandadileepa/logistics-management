package com.quincus.web.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private String[] allowed;
    private String[] origins;
    private String[] methods;
    private String[] headers;
    private TokenProperties token;

    @Data
    public static class TokenProperties {
        private String value;
    }
}
