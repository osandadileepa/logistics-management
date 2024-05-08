package com.quincus.db.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "db.jpa")
public class JpaProperties {

    private String databasePlatform;
    private boolean generateDdl;
    private boolean showSql;
    private String [] modelPackages;

}
