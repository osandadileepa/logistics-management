package com.quincus.shipment.api.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class MilestoneAdditionalInfo {
    private List<HostedFile> images;
    private List<HostedFile> signature;
    @Size(max = 2000, message = "Must be maximum of 2000 characters.")
    private String remarks;
    private List<HostedFile> attachments;
    private Cod cod;
}
