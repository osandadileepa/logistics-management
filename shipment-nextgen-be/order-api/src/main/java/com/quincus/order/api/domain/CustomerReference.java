package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class CustomerReference {
    private String id;
    private String idLabel;
    private String createdAt;
    private String updatedAt;
}
