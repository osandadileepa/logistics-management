package com.quincus.db.config;


import com.quincus.ext.YamlPropertySourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties({
        DataSourceProperties.class,
        JpaProperties.class
})
@PropertySource(value = {"classpath:config/db.yml", "classpath:config/db-${spring.profiles.active}.yml"}, factory = YamlPropertySourceFactory.class, ignoreResourceNotFound = true)
public class DBConfiguration {


    private final DataSourceProperties dataSourceProperties;
    private final JpaProperties jpaProperties;

    public DBConfiguration(DataSourceProperties dataSourceProperties, JpaProperties jpaProperties) {
        this.dataSourceProperties = dataSourceProperties;
        this.jpaProperties = jpaProperties;
    }

    @Bean("db.config.LocalContainerEntityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean userEntityManager() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(jpaProperties.getModelPackages());

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
        vendorAdapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        em.setJpaVendorAdapter(vendorAdapter);

        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", jpaProperties.getDatabasePlatform());
        properties.setProperty("hibernate.ddl-auto", "create-drop");
        em.setJpaProperties(properties);

        return em;
    }

    @Bean("db.config.DataSource")
    public DataSource dataSource() {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        return dataSource;
    }

    @Bean("db.config.PlatformTransactionManager")
    public PlatformTransactionManager userTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(userEntityManager().getObject());
        return transactionManager;
    }

}
