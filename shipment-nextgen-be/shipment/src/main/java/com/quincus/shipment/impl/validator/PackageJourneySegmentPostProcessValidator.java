package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.web.common.validator.PostProcessValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PackageJourneySegmentPostProcessValidator implements PostProcessValidator<PackageJourneySegment> {

    @Override
    public boolean isValid(PackageJourneySegment segment) {
        if (segment.getTransportType() == null) {
            String errorMessage = "Transport type not provided in segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        if (segment.getPartner() == null) {
            String errorMessage = "Partner not provided in segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        if (segment.getStartFacility() == null) {
            String errorMessage = "Start facility not provided in segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        if (segment.getEndFacility() == null) {
            String errorMessage = "End facility not provided in segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        if (segment.getTransportType() == TransportType.GROUND) {
            return isGroundSegmentValid(segment);
        } else if (segment.getTransportType() == TransportType.AIR) {
            return isAirSegmentValid(segment);
        }
        return true;
    }

    private boolean isGroundSegmentValid(PackageJourneySegment segment) {
        if (segment.getPickUpTime() == null || segment.getDropOffTime() == null) {
            String errorMessage = "Pickup/Drop Off times not provided in ground segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        return true;
    }

    private boolean isAirSegmentValid(PackageJourneySegment segment) {
        if (segment.getDepartureTime() == null) {
            String errorMessage = "Departure DateTime not provided in air segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        if (segment.getAirlineCode() == null) {
            String errorMessage = "Airline Code not provided in air segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        if (segment.getFlightNumber() == null) {
            String errorMessage = "Flight Number not provided in air segment {}. Make sure these are not omitted in the query results.";
            log.warn(errorMessage, segment.getSegmentId());
            return false;
        }
        return true;
    }
}
