package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.serializer.PackageJourneySegmentListSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ShipmentJourney {

    public static final String STATUS_PLANNED = "PLANNED";

    @UUID(required = false)
    private String journeyId;
    @UUID(required = false)
    private String shipmentId;
    @UUID(required = false)
    private String orderId;
    private JourneyStatus status;
    @Valid
    @JsonSerialize(using = PackageJourneySegmentListSerializer.class)
    private List<PackageJourneySegment> packageJourneySegments;
    @Valid
    @Size(max = 20)
    private List<Alert> alerts;

    public List<PackageJourneySegment> getPackageJourneySegments() {
        if (CollectionUtils.isEmpty(packageJourneySegments)) {
            return Collections.emptyList();
        }
        return packageJourneySegments.stream()
                .sorted(Comparator.comparing(PackageJourneySegment::getSequence))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @JsonIgnore
    public List<PackageJourneySegment> getUnsortedPackageJourneySegments() {
        if (CollectionUtils.isEmpty(packageJourneySegments)) {
            return Collections.emptyList();
        }
        return packageJourneySegments;
    }

    public void addPackageJourneySegment(PackageJourneySegment segment) {
        if (CollectionUtils.isEmpty(packageJourneySegments)) {
            packageJourneySegments = new ArrayList<>();
        }
        packageJourneySegments.add(segment);
    }

    public void markAllSegmentsAsDeleted() {
        Optional.ofNullable(getPackageJourneySegments())
                .filter(segments -> !segments.isEmpty())
                .ifPresent(segments -> segments.forEach(segment -> segment.setDeleted(true)));
    }

}
