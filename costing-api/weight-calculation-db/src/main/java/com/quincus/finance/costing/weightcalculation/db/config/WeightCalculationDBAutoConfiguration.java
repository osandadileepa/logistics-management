package com.quincus.finance.costing.weightcalculation.db.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConditionalOnProperty(name = "weight-calculation.db.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan(basePackages = {"com.quincus.finance.costing.weightcalculation.db"})
@EnableConfigurationProperties(DataSourceProperties.class)
@EnableJpaRepositories(
        basePackages = "com.quincus.finance.costing.weightcalculation.db.repository",
        entityManagerFactoryRef = "db.config.LocalContainerEntityManagerFactoryBean",
        transactionManagerRef = "db.config.PlatformTransactionManager"

)
public class WeightCalculationDBAutoConfiguration {

}
