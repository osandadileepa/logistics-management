package com.quincus.networkmanagement.impl.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties(prefix = "async.thread")
@PropertySource(value = {"classpath:config/async-config-${spring.profiles.active}.yml"}
        , factory = YamlPropertySourceFactory.class)
@ConstructorBinding
public class NetworkManagementAsyncProperties {
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private String threadNamePrefix;
}
