package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class Instruction {
    public static final String SOURCE_ORDER = "order";
    public static final String SOURCE_SEGMENT = "segment";
    @UUID(required = false)
    private String id;
    @UUID(required = false)
    private String externalId;
    @Size(max = 256)
    private String label;
    @Size(max = 256)
    private String source;
    @Size(max = 4000, message = "Must be maximum of 4000 characters.")
    private String value;
    private InstructionApplyToType applyTo;
    @Size(max = 128)
    private String createdAt;
    @Size(max = 128)
    private String updatedAt;
    @UUID(required = false)
    private String orderId;
    @UUID(required = false)
    private String packageJourneySegmentId;
    @UUID(required = false)
    private String organizationId;
}
