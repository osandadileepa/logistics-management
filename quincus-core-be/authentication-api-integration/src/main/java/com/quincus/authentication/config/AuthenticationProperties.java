package com.quincus.authentication.config;

import com.quincus.core.impl.util.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ConfigurationProperties(prefix = "authentication")
@Data
@Configuration
@PropertySource(value = {"classpath:config/authentication-config-${spring.profiles.active}.yml"}
        , factory = YamlPropertySourceFactory.class)
public class AuthenticationProperties {
    private String baseUrl;
    private String loginApi;
    private String validateTokenApi;
    @Value("${spring.profiles.active}")
    private String profile;
}
