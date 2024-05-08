package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.FileName;
import com.quincus.ext.annotation.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class ProofOfCost {
    @UUID(required = false)
    private String id;
    @NotBlank
    private String url;
    @FileName
    private String fileName;
    @NotNull
    @Min(value = 0, message = "File size should not be negative.")
    @Max(value = 24 * 1024 * 1024, message = "File size should not exceed 24MB.")
    private Long fileSize;
}
