package com.quincus.shipment.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MilestoneUpdateTimeRequest {
    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private String milestoneTime;
    private String id;
}
