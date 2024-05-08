package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class Instruction {
    private String id;
    private String label;
    private String source;
    private String value;
    private String applyTo;
    private String createdAt;
    private String updatedAt;
}