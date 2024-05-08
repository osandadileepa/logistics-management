package com.quincus.shipment.kafka.producers.message;

import com.quincus.shipment.api.domain.Cod;
import com.quincus.shipment.api.domain.HostedFile;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MilestoneAdditionalInfoMsgPart {
    private List<HostedFile> images;
    private List<HostedFile> signature;
    private List<HostedFile> attachments;
    private String remarks;
    private Cod cod;
}
