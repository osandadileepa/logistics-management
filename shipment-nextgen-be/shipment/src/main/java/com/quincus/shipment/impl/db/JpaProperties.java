package com.quincus.shipment.impl.db;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "db.jpa")
public class JpaProperties {

    private String databasePlatform;
    private boolean generateDdl;
    private boolean showSql;
    private String batchSize;
    private boolean batchEnable;
    private String[] modelPackages;

}
