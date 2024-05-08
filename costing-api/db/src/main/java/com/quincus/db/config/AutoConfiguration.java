package com.quincus.db.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"com.quincus.db"})
@EnableConfigurationProperties(DataSourceProperties.class)
@EnableJpaRepositories(
        basePackages = "com.quincus.db.repository",
        entityManagerFactoryRef = "db.config.LocalContainerEntityManagerFactoryBean",
        transactionManagerRef = "db.config.PlatformTransactionManager"

)
public class AutoConfiguration {

}
