package com.quincus.shipment.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAttachment {
    @NotBlank
    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    @NotBlank
    private String fileName;
    @NotBlank
    private String fileUrl;
    @NotNull
    @Min(0)
    private Long fileSize;
}
