package com.quincus.shipment.impl.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "shipment-journey")
@PropertySource(value = {"classpath:config/shipment-config-${spring.profiles.active}.yml"}
        , factory = YamlPropertySourceFactory.class)
@ConstructorBinding
public class ShipmentJourneyCreationProperties {

    private List<String> organizationIdsToSkipSegmentCreationFromPayload;
}
