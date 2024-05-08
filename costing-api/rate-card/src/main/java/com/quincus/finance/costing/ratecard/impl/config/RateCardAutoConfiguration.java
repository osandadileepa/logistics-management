package com.quincus.finance.costing.ratecard.impl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "rate-card.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan(basePackages = {"com.quincus.finance.costing.ratecard.impl"})
public class RateCardAutoConfiguration {

}
