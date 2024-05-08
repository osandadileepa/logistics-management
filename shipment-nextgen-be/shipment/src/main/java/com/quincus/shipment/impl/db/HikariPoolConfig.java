package com.quincus.shipment.impl.db;

import lombok.Data;

@Data
public class HikariPoolConfig {
    private int minimumIdle;
    private int maximumPoolSize;
    private long idleTimeout;
    private long connectionTimeout;
    private long maxLifetime;
}
