package com.quincus.finance.costing.weightcalculation.impl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "weight-calculation.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan(basePackages = {"com.quincus.finance.costing.weightcalculation.impl"})
public class WeightCalculationAutoConfiguration {

}
