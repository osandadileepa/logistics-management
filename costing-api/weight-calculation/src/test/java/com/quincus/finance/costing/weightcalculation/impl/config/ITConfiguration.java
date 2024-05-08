package com.quincus.finance.costing.weightcalculation.impl.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"com.quincus.finance.costing", "com.quincus.db"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.quincus.db.config.*"))
@EntityScan(basePackages = {"com.quincus.finance.costing", "com.quincus.db.model"})
@EnableJpaRepositories(
        basePackages = {
                "com.quincus.finance.costing.weightcalculation.db.repository",
                "com.quincus.db.repository"}

)
public class ITConfiguration {
}
