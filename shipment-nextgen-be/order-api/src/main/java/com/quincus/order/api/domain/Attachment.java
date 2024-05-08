package com.quincus.order.api.domain;

import lombok.Data;

import java.time.Instant;

@Data
public class Attachment {
    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    private String fileName;
    private Long fileSize;
    private String fileUrl;
}
