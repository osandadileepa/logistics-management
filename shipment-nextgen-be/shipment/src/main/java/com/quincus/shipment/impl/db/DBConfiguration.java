package com.quincus.shipment.impl.db;


import com.quincus.ext.YamlPropertySourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
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
@EnableJpaRepositories(
        basePackages = "com.quincus.shipment.impl.repository",
        entityManagerFactoryRef = "db.config.LocalContainerEntityManagerFactoryBean",
        transactionManagerRef = "db.config.PlatformTransactionManager"
)
@AllArgsConstructor
public class DBConfiguration {

    private final DataSourceProperties dataSourceProperties;
    private final JpaProperties jpaProperties;

    @Bean("db.config.LocalContainerEntityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean userEntityManager() {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan(jpaProperties.getModelPackages());

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
        vendorAdapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

        entityManagerFactoryBean.setJpaDialect(new HibernateJpaDialect());

        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", jpaProperties.getDatabasePlatform());
        properties.setProperty("hibernate.ddl-auto", "create-drop");
        properties.setProperty("hibernate.types.jackson.object.mapper", HibernateObjectMapperSupplier.class.getCanonicalName());
        properties.setProperty("hibernate.show_sql", String.valueOf(jpaProperties.isShowSql()));
        properties.setProperty("hibernate.format_sql", String.valueOf(jpaProperties.isShowSql()));
        properties.setProperty("hibernate.jdbc.batch_size", jpaProperties.getBatchSize());
        properties.setProperty("hibernate.order_inserts", String.valueOf(jpaProperties.isBatchEnable()));
        properties.setProperty("hibernate.order_updates", String.valueOf(jpaProperties.isBatchEnable()));

        entityManagerFactoryBean.setJpaProperties(properties);
        return entityManagerFactoryBean;
    }

    @Bean("db.config.DataSource")
    public DataSource dataSource() {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());

        final HikariPoolConfig hikari = dataSourceProperties.getHikari();
        dataSource.setMinimumIdle(hikari.getMinimumIdle());
        dataSource.setMaximumPoolSize(hikari.getMaximumPoolSize());
        dataSource.setIdleTimeout(hikari.getIdleTimeout());
        dataSource.setConnectionTimeout(hikari.getConnectionTimeout());
        dataSource.setMaxLifetime(hikari.getMaxLifetime());
        return dataSource;
    }

    @Bean("db.config.PlatformTransactionManager")
    public PlatformTransactionManager userTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(userEntityManager().getObject());
        return transactionManager;
    }

}
