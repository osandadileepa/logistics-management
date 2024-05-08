package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.ext.annotation.ISODateTime;
import com.quincus.shipment.api.constant.BookingStatus;
import com.quincus.shipment.api.constant.FlightSubscriptionStatus;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.serializer.BigDecimalSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class PackageJourneySegment implements Comparable<PackageJourneySegment> {
    private String segmentId;
    @Size(max = 48)
    private String journeyId;
    @Size(max = 48)
    private String hubId;
    @Valid
    private Facility startFacility;
    @Valid
    private Facility endFacility;
    @Valid
    private Partner partner;
    private SegmentType type;
    @Size(max = 45)
    private String opsType;
    private SegmentStatus status;
    @NotNull
    private TransportType transportType;
    @Size(max = 45)
    private String servicedBy;
    @Size(max = 45)
    private String cost;
    @Size(max = 50)
    private String refId;
    @Size(max = 45)
    private String sequence;
    @Size(max = 65)
    private String airline;
    @Size(max = 50)
    private String airlineCode;
    @Size(max = 50)
    private String currencyId;
    @Size(max = 4000, message = "Must be maximum of 4000 characters.")
    private String instruction;
    @Size(max = 50)
    private String vehicleInfo;
    @Size(max = 50)
    private String flightNumber;
    @Size(max = 50)
    @ISODateTime
    private String arrivalTime;
    @Size(max = 255)
    private String arrivalTimezone;
    @Size(max = 50)
    @ISODateTime
    private String pickUpTime;
    @Size(max = 255)
    private String pickUpTimezone;
    @Size(max = 50)
    @ISODateTime
    private String pickUpCommitTime;
    @Size(max = 255)
    private String pickUpCommitTimezone;
    @Size(max = 50)
    @ISODateTime
    private String pickUpActualTime;
    @Size(max = 255)
    private String pickUpActualTimezone;
    @Size(max = 50)
    @ISODateTime
    private String pickUpOnSiteTime;
    @Size(max = 255)
    private String pickUpOnSiteTimezone;
    @Size(max = 50)
    @ISODateTime
    private String dropOffTime;
    @Size(max = 255)
    private String dropOffTimezone;
    @Size(max = 50)
    @ISODateTime
    private String dropOffCommitTime;
    @Size(max = 255)
    private String dropOffCommitTimezone;
    @Size(max = 50)
    @ISODateTime
    private String dropOffActualTime;
    @Size(max = 255)
    private String dropOffActualTimezone;
    @Size(max = 50)
    @ISODateTime
    private String dropOffOnSiteTime;
    @Size(max = 255)
    private String dropOffOnSiteTimezone;
    @Size(max = 50)
    @ISODateTime
    private String lockOutTime;
    @Size(max = 255)
    private String lockOutTimezone;
    @Size(max = 50)
    @ISODateTime
    private String recoveryTime;
    @Size(max = 255)
    private String recoveryTimezone;
    @Size(max = 50)
    @ISODateTime
    private String departureTime;
    @Size(max = 255)
    private String departureTimezone;
    @Size(max = 50)
    private String masterWaybill;
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal calculatedMileage;
    private UnitOfMeasure calculatedMileageUnit;
    private String calculatedMileageUnitLabel;
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal duration;
    private UnitOfMeasure durationUnit;
    private String durationUnitLabel;
    @Valid
    @Size(max = 20)
    private List<Alert> alerts;
    private String organizationId;
    private String flightOrigin;
    private String flightDestination;
    private FlightSubscriptionStatus flightSubscriptionStatus;
    @Valid
    private Flight flight;
    @Valid
    private Vehicle vehicle;
    @Valid
    private Driver driver;
    @Valid
    @Size(max = 20)
    private List<Instruction> instructions;
    private Instant modifyTime;
    private boolean deleted;
    @JsonIgnore
    private boolean newlyCreated;
    @Size(max = 64)
    private String internalBookingReference;
    @Size(max = 64)
    private String externalBookingReference;
    private BookingStatus bookingStatus;
    @Size(max = 64)
    private String assignmentStatus;
    @Size(max = 255)
    private String rejectionReason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageJourneySegment that)) {
            return false;
        }
        return (segmentId != null)
                && (journeyId != null)
                && Objects.equals(segmentId, that.segmentId)
                && Objects.equals(journeyId, that.journeyId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public int compareTo(PackageJourneySegment p) {
        if (sequence == null || p == null || p.getSequence() == null) return 0;
        return sequence.compareTo(p.getSequence());
    }
}
