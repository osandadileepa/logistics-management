package com.quincus.networkmanagement.impl.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties(prefix = "mme")
@PropertySource(value = {"classpath:config/training-config-${spring.profiles.active}.yml"}, factory = YamlPropertySourceFactory.class)
public class MmeTrainingProperties {
    private long trainingDelay;
    private String debounceKeyPrefix;
    @Value("${spring.profiles.active}")
    private String profile;
}
