package com.quincus.shipment.impl.validator;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.exception.PackageJourneySegmentException;
import com.quincus.shipment.api.exception.SegmentException;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@AllArgsConstructor
@Slf4j
@Component
public class PackageJourneySegmentValidator {
    public static final String FACILITY_ID_SET_ERR_MSG = "ID value must be set to `external_id` instead of `id`.";
    public static final String FACILITY_DROP_PICKUP_NOT_MATCH_ERR_MSG = "Not matching Segment's DropOff and Pickup found.";
    public static final String PICKUP_DROP_OFF_SAME_ERR_MSG = "PickUp and DropOff cannot be the same.";
    public static final String DUPLICATE_SEGMENT_FOUND_ERR_MSG = "Duplicate Segment found.";
    public static final String EMPTY_PICKUP_DROP_OFF_TIME = "PickUp/DropOff time not found.";
    public static final String EMPTY_DEPARTURE_ARRIVAL_TIME = "Departure time not found.";
    public static final String EMPTY_FACILITY_ID = "Start/End Facility not found.";
    public static final String EMPTY_PARTNER_ID = "Partner ID not found.";
    private final UserDetailsProvider userDetailsProvider;

    public void validatePackageJourneySegments(ShipmentJourney shipmentJourney) {
        if (userDetailsProvider.isFromKafka()) {
            log.info("Skipping validation for Kafka source with order id: `{}` and shipment id: `{}`", shipmentJourney.getOrderId(), shipmentJourney.getShipmentId());
            return;
        }
        if (CollectionUtils.isEmpty(shipmentJourney.getPackageJourneySegments())) {
            log.debug("Skipping validation as segments are empty with order id: `{}` and shipment id: `{}`", shipmentJourney.getOrderId(), shipmentJourney.getShipmentId());
            return;
        }
        validatePackageJourneySegments(shipmentJourney.getPackageJourneySegments());
    }

    private void validatePackageJourneySegments(final List<PackageJourneySegment> packageJourneySegments) {
        PackageJourneySegmentException packageJourneySegmentException = new PackageJourneySegmentException();
        Set<PackageJourneySegment> uniquePackageJourneySegments = new HashSet<>();
        IntStream.range(0, packageJourneySegments.size()).forEach(index -> {
            PackageJourneySegment packageJourneySegment = packageJourneySegments.get(index);
            validateOrderAndS2SRestrictions(packageJourneySegments, packageJourneySegment, index, packageJourneySegmentException);
            if (isFromUI()) {
                validateUIRestrictions(packageJourneySegment, index, packageJourneySegmentException);
            } else {
                enrichPackageJourneySegment(packageJourneySegment, index); //Enrich only when source is not from UI.
            }
            uniquePackageJourneySegments.add(packageJourneySegment);
        });
        checkForDuplicateSegments(uniquePackageJourneySegments, packageJourneySegments);
        checkAndThrowExceptionIfErrorsPresent(packageJourneySegmentException);
    }

    private void checkAndThrowExceptionIfErrorsPresent(PackageJourneySegmentException packageJourneySegmentException) {
        if (CollectionUtils.isNotEmpty(packageJourneySegmentException.getErrors())) {
            throw packageJourneySegmentException;
        }
    }

    private boolean isFromUI() {
        return !userDetailsProvider.isFromAllowedSource();
    }

    private void enrichPackageJourneySegment(PackageJourneySegment packageJourneySegment, int index) {
        if (StringUtils.isBlank(packageJourneySegment.getRefId())) {
            packageJourneySegment.setRefId(String.valueOf(index));
        }
        if (StringUtils.isBlank(packageJourneySegment.getSequence())) {
            packageJourneySegment.setSequence(String.valueOf(index));
        }
    }

    private void validateOrderAndS2SRestrictions(List<PackageJourneySegment> packageJourneySegments,
                                                 PackageJourneySegment packageJourneySegment,
                                                 int index,
                                                 PackageJourneySegmentException packageJourneySegmentException) {

        if (isFacilityIdSet(packageJourneySegment.getStartFacility())) {
            packageJourneySegmentException.addError(index, FACILITY_ID_SET_ERR_MSG, "start_facility");
        }
        if (isFacilityIdSet(packageJourneySegment.getEndFacility())) {
            packageJourneySegmentException.addError(index, FACILITY_ID_SET_ERR_MSG, "end_facility");
        }
        if (index != 0 && !isValidPickUpDropOffLocations(packageJourneySegments.get(index - 1), packageJourneySegment)) {
            packageJourneySegmentException.addError(index, FACILITY_DROP_PICKUP_NOT_MATCH_ERR_MSG, "start_facility.external_id and end_facility.external_id");
        }
    }

    private void validateUIRestrictions(PackageJourneySegment packageJourneySegment,
                                        int index,
                                        PackageJourneySegmentException packageJourneySegmentException) {
        if (isFacilityEmpty(packageJourneySegment.getStartFacility())) {
            packageJourneySegmentException.addError(index, EMPTY_FACILITY_ID, "start_facility");
        }
        if (isFacilityEmpty(packageJourneySegment.getEndFacility())) {
            packageJourneySegmentException.addError(index, EMPTY_FACILITY_ID, "end_facility");
        }
        if (isEmptyPickUpDropOffTime(packageJourneySegment)) {
            packageJourneySegmentException.addError(index, EMPTY_PICKUP_DROP_OFF_TIME, "pick_up_time and drop_off_time");
        }
        if (isFacilityIdsAreEqual(packageJourneySegment)) {
            packageJourneySegmentException.addError(index, PICKUP_DROP_OFF_SAME_ERR_MSG, "start_facility.external_id and end_facility.external_id");
        }
        if (isEmptyDepartureTime(packageJourneySegment)) {
            packageJourneySegmentException.addError(index, EMPTY_DEPARTURE_ARRIVAL_TIME, "departure_time");
        }
        if (isEmptyPartnerId(packageJourneySegment)) {
            packageJourneySegmentException.addError(index, EMPTY_PARTNER_ID, "partner_id");
        }
    }

    private void checkForDuplicateSegments(Set<PackageJourneySegment> uniqueSegments,
                                           List<PackageJourneySegment> packageJourneySegments) {
        if (uniqueSegments.size() != packageJourneySegments.size() && isFromUI()) {
            throw new SegmentException(DUPLICATE_SEGMENT_FOUND_ERR_MSG);
        }
    }

    private boolean isEmptyDepartureTime(PackageJourneySegment packageJourneySegment) {
        if (!TransportType.AIR.equals(packageJourneySegment.getTransportType())) return false;
        return StringUtils.isBlank(packageJourneySegment.getDepartureTime());
    }

    private boolean isFacilityIdsAreEqual(PackageJourneySegment packageJourneySegment) {
        if (ObjectUtils.allNotNull(packageJourneySegment.getStartFacility(),
                packageJourneySegment.getEndFacility())) {
            return StringUtils.equals(packageJourneySegment.getStartFacility().getExternalId(),
                    packageJourneySegment.getEndFacility().getExternalId());
        }
        return false;
    }

    private boolean isFacilityIdSet(Facility facility) {
        return facility != null && StringUtils.isNotBlank(facility.getId());
    }

    private boolean isFacilityEmpty(Facility facility) {
        return facility == null || StringUtils.isBlank(facility.getExternalId());
    }

    private boolean isEmptyPickUpDropOffTime(PackageJourneySegment packageJourneySegment) {
        if (!TransportType.GROUND.equals(packageJourneySegment.getTransportType())) return false;
        ZonedDateTime pickup = DateTimeUtil.parseZonedDateTime(packageJourneySegment.getPickUpTime());
        ZonedDateTime dropOff = DateTimeUtil.parseZonedDateTime(packageJourneySegment.getDropOffTime());
        return pickup == null || dropOff == null;
    }

    private boolean isEmptyPartnerId(PackageJourneySegment packageJourneySegment) {
        return packageJourneySegment.getPartner() == null
                || StringUtils.isBlank(packageJourneySegment.getPartner().getId());
    }

    private boolean isValidPickUpDropOffLocations(PackageJourneySegment previousSegment, PackageJourneySegment segment) {
        String previousEndFacilityId = previousSegment.getEndFacility().getExternalId();
        String segmentStartFacilityId = segment.getStartFacility().getExternalId();
        if (StringUtils.isBlank(previousEndFacilityId) || StringUtils.isBlank(segmentStartFacilityId)) return true;
        return StringUtils.equals(previousEndFacilityId, segmentStartFacilityId);
    }
    
}
