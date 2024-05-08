package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.ISODateTime;
import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class NetworkLaneSegment {

    @UUID(required = false)
    private String id;
    @Size(max = 48)
    private String sequence;
    private TransportType transportType = TransportType.GROUND;
    @Valid
    private Partner partner;
    @Size(max = 50)
    private String vehicleInfo;
    @Size(max = 50)
    private String flightNumber;
    @Size(max = 65)
    private String airline;
    @Size(max = 50)
    private String airlineCode;
    @Valid
    private Facility startFacility;
    @Valid
    private Facility endFacility;
    @Size(max = 50)
    private String masterWaybill;
    @Size(max = 4000, message = "Must be maximum of 4000 characters.")
    private String pickupInstruction;
    @Size(max = 4000, message = "Must be maximum of 4000 characters.")
    private String deliveryInstruction;
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 15, fraction = 4)
    private BigDecimal duration;
    private UnitOfMeasure durationUnit = UnitOfMeasure.MINUTE;
    @Size(max = 50)
    @ISODateTime
    private String pickUpTime;
    @Size(max = 255)
    private String pickUpTimezone;
    @Size(max = 50)
    @ISODateTime
    private String dropOffTime;
    @Size(max = 255)
    private String dropOffTimezone;
    @Size(max = 50)
    @ISODateTime
    private String lockOutTime;
    @Size(max = 255)
    private String lockOutTimezone;
    @Size(max = 50)
    @ISODateTime
    private String departureTime;
    @Size(max = 255)
    private String departureTimezone;
    @Size(max = 50)
    @ISODateTime
    private String arrivalTime;
    @Size(max = 255)
    private String arrivalTimezone;
    @Size(max = 50)
    @ISODateTime
    private String recoveryTime;
    @Size(max = 255)
    private String recoveryTimezone;
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 15, fraction = 4)
    private BigDecimal calculatedMileage;
    private UnitOfMeasure calculatedMileageUnit = UnitOfMeasure.MILE;
    @Size(max = 42)
    private String organizationId;
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 15, fraction = 4)
    private BigDecimal calculatedDuration;
    private SegmentType type;

    public String getCalculatedMileageUnitLabel() {
        return calculatedMileageUnit.getLabel();
    }

    public String getDurationUnitLabel() {
        return durationUnit.getLabel();
    }

}
