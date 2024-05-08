package com.quincus.qlogger.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ConfigurationProperties(prefix = "qlogger")
@Data
@Configuration
@PropertySource(value = {"classpath:config/qlogger-config-${spring.profiles.active}.yml"}
        , factory = YamlPropertySourceFactory.class)
public class QLoggerProperties {

    private String s2sToken;
    private String baseUrl;
    private String publishEventAPI;

    @Value("${spring.profiles.active}")
    private String profile;
}
