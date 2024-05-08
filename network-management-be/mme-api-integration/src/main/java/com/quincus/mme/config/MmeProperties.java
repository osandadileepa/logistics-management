package com.quincus.mme.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties(prefix = "mme")
@PropertySource(value = {"classpath:config/mme-config-${spring.profiles.active}.yml"}, factory = YamlPropertySourceFactory.class)
public class MmeProperties {
    private String authValue;
    private String baseUrl;
    private String trainModel;
    private String checkTrainModel;
    private String predict;
    private String checkPrediction;
    @Value("${spring.profiles.active}")
    private String profile;
}
