package com.quincus.networkmanagement.impl.db;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringLiquibaseConfiguration {

    @Bean
    @Autowired
    public SpringLiquibase createCustomSpringLiquibase(DBConfiguration dbConfiguration) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dbConfiguration.dataSource());
        springLiquibase.setChangeLog("classpath:/db/db.changelog-master.yaml");
        springLiquibase.setShouldRun(true);
        return springLiquibase;
    }

}
