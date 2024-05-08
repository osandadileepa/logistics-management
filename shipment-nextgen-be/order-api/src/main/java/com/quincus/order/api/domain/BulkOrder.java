package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class BulkOrder {
    private String id;
    private String bulkId;
    private String userName;
    private String status;
    private String createdAt;
}
