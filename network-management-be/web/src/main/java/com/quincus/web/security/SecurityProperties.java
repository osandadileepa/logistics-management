package com.quincus.web.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private String[] allowed;
    private String[] origins;
    private String[] methods;
    private String[] headers;
    private List<Token> s2sTokens;

    @Data
    public static class Token {
        private String name;
        private String value;
    }
}
