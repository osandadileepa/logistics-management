package com.quincus.shipment.impl.mapper;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", imports = {DateTimeUtil.class}, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface NetworkLaneJourneySegmentMapper {

    @Mapping(target = "arrivalTime", expression = "java(DateTimeUtil.toIsoDateTimeFormat(networkLaneSegment.getArrivalTime()))")
    @Mapping(target = "lockOutTime", expression = "java(DateTimeUtil.toIsoDateTimeFormat(networkLaneSegment.getLockOutTime()))")
    @Mapping(target = "recoveryTime", expression = "java(DateTimeUtil.toIsoDateTimeFormat(networkLaneSegment.getRecoveryTime()))")
    @Mapping(target = "departureTime", expression = "java(DateTimeUtil.toIsoDateTimeFormat(networkLaneSegment.getDepartureTime()))")
    @Mapping(target = "pickUpTime", ignore = true)
    @Mapping(target = "pickUpCommitTime", ignore = true)
    @Mapping(target = "dropOffTime", ignore = true)
    @Mapping(target = "dropOffCommitTime", ignore = true)
    PackageJourneySegment toPackageJourneySegment(NetworkLaneSegment networkLaneSegment, String refId, String opsType);

    @AfterMapping
    default void addAdditionalPackageJourneyInfo(@MappingTarget PackageJourneySegment packageJourneySegment, NetworkLaneSegment networkLaneSegment) {
        String combinedInstruction = Stream.of(networkLaneSegment.getPickupInstruction(), networkLaneSegment.getDeliveryInstruction())
                .filter(StringUtils::isNotBlank).collect(Collectors.joining(", "));
        packageJourneySegment.setInstruction(combinedInstruction);
        packageJourneySegment.setStatus(SegmentStatus.PLANNED);
    }

    @AfterMapping
    default void removeFacilityLocationDetail(@MappingTarget PackageJourneySegment packageJourneySegment) {
        // removing the location as this will enrich during shipment creation
        if (packageJourneySegment.getStartFacility() != null) {
            packageJourneySegment.getStartFacility().setLocation(null);
        }
        if (packageJourneySegment.getEndFacility() != null) {
            packageJourneySegment.getEndFacility().setLocation(null);
        }
    }
}
