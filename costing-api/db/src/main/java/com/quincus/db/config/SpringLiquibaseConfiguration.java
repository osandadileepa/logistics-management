package com.quincus.db.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringLiquibaseConfiguration {

    private final DBConfiguration dbConfiguration;

    public SpringLiquibaseConfiguration(DBConfiguration dbConfiguration) {
        this.dbConfiguration = dbConfiguration;
    }


    @Bean
    public SpringLiquibase createCustomSpringLiquibase() {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dbConfiguration.dataSource());
        springLiquibase.setChangeLog("classpath:/db/db.changelog-master.yaml");
        springLiquibase.setShouldRun(true);
        return springLiquibase;

    }

}
