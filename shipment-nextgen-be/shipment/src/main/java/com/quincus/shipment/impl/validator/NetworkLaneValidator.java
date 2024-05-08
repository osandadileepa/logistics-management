package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.exception.NetworkLaneException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
@Slf4j
public class NetworkLaneValidator {
    private static final String LOCATION_IS_MISSING_ERROR = "Location is missing.";
    private static final String NODES_NOT_CONNECTED_ERROR = "Nodes not connected.";
    private static final String IDENTICAL_FIRST_LAST_NODES_ERROR = "Identical first & last network nodes.";
    private static final String START_FACILITY = "start_facility";
    private static final String END_FACILITY = "end_facility";

    public void validate(NetworkLane networkLane) {
        validateNetworkLane(networkLane);
    }

    private void validateNetworkLane(NetworkLane networkLane) {
        NetworkLaneException networkLaneException = new NetworkLaneException();
        addErrorWhenFacilityLocationMissingFromNetworkLaneSegments(networkLane.getNetworkLaneSegments(), networkLaneException);
        addErrorWhenFirstAndLastNodesAreIdentical(networkLane, networkLaneException);
        addErrorWhenStartToDestinationIsDisconnected(networkLane, networkLaneException);

        checkAndThrowExceptionIfErrorsPresent(networkLaneException);
    }

    private void checkAndThrowExceptionIfErrorsPresent(NetworkLaneException networkLaneException) {
        if (CollectionUtils.isNotEmpty(networkLaneException.getErrors())) {
            throw networkLaneException;
        }
    }

    private void addErrorWhenStartToDestinationIsDisconnected(
            @NotNull final NetworkLane networkLane,
            final NetworkLaneException networkLaneException) {
        List<NetworkLaneSegment> segments = networkLane.getNetworkLaneSegments();
        if (segments == null || segments.isEmpty()) {
            return;
        }

        final String networkLaneSegmentField = "network_lane_segments[{0}].{1}";
        if (isOriginToFirstSegmentFacilityDisconnected(networkLane, segments)) {
            networkLaneException.addError(NODES_NOT_CONNECTED_ERROR, "origin_facility and ".concat(MessageFormat.format(networkLaneSegmentField, 0, START_FACILITY)));
        }
        addErrorWhenSegmentsDisconnected(networkLane, networkLaneException);
        if (isLastSegmentToDestinationFacilityDisconnected(networkLane, segments)) {
            networkLaneException.addError(NODES_NOT_CONNECTED_ERROR, "destination_facility and ".concat(MessageFormat.format(networkLaneSegmentField, segments.size() - 1, END_FACILITY)));
        }

    }

    private void addErrorWhenSegmentsDisconnected(NetworkLane networkLane, NetworkLaneException networkLaneException) {
        List<NetworkLaneSegment> segments = networkLane.getNetworkLaneSegments();
        Facility prevEndFacility = null;
        final String networkLaneSegmentField = "network_lane_segments[{0}].{1} and network_lane_segments[{2}].{3}";
        for (int index = 0; index < segments.size(); index++) {
            final NetworkLaneSegment segment = segments.get(index);
            final int previousIndex = index - 1;
            if (prevEndFacility != null
                    && segment.getStartFacility() != null
                    && !prevEndFacility.equals(segment.getStartFacility())) {
                networkLaneException.addError(NODES_NOT_CONNECTED_ERROR, MessageFormat.format(networkLaneSegmentField, previousIndex, END_FACILITY, index, START_FACILITY));
            }
            prevEndFacility = segment.getEndFacility();
        }
    }

    private boolean isOriginToFirstSegmentFacilityDisconnected(NetworkLane networkLane, List<NetworkLaneSegment> segments) {
        return networkLane.getOriginFacility() != null
                && segments.get(0).getStartFacility() != null
                && !networkLane.getOriginFacility().equals(segments.get(0).getStartFacility());
    }

    private boolean isLastSegmentToDestinationFacilityDisconnected(NetworkLane networkLane, List<NetworkLaneSegment> segments) {
        return networkLane.getDestinationFacility() != null
                && segments.get(segments.size() - 1).getEndFacility() != null
                && !segments.get(segments.size() - 1).getEndFacility().equals(networkLane.getDestinationFacility());
    }

    private void addErrorWhenFacilityLocationMissingFromNetworkLaneSegments(
            final List<NetworkLaneSegment> networkLaneSegments,
            final NetworkLaneException networkLaneException) {
        if (networkLaneSegments == null || networkLaneSegments.isEmpty()) {
            return;
        }

        final String networkLaneSegmentField = "network_lane_segments[{0}].{1}.location";
        for (int index = 0; index < networkLaneSegments.size(); index++) {
            final NetworkLaneSegment networkLaneSegment = networkLaneSegments.get(index);
            if (isFacilityLocationMissing(networkLaneSegment.getStartFacility())) {
                networkLaneException.addError(LOCATION_IS_MISSING_ERROR, MessageFormat.format(networkLaneSegmentField, index, START_FACILITY));
            }
            if (isFacilityLocationMissing(networkLaneSegment.getEndFacility())) {
                networkLaneException.addError(LOCATION_IS_MISSING_ERROR, MessageFormat.format(networkLaneSegmentField, index, END_FACILITY));
            }
        }
    }

    private boolean isFacilityLocationMissing(Facility facility) {
        return facility != null && facility.getLocation() == null;
    }

    private void addErrorWhenFirstAndLastNodesAreIdentical(
            @NotNull final NetworkLane networkLane,
            final NetworkLaneException networkLaneException) {
        final boolean isUsingFacility = isNotBlank(networkLane.getNetworkLaneSegments().get(0).getStartFacility().getId());
        if (isUsingFacility) {
            if (hasIdenticalFacilityNodes(networkLane)) {
                networkLaneException.addError(IDENTICAL_FIRST_LAST_NODES_ERROR, "origin_facility and destination_facility");
            }
        } else {
            if (hasIdenticalAddressNodes(networkLane)) {
                networkLaneException.addError(IDENTICAL_FIRST_LAST_NODES_ERROR, "origin and destination");
            }
        }
    }

    private boolean hasIdenticalAddressNodes(NetworkLane networkLane) {
        final List<NetworkLaneSegment> segments = networkLane.getNetworkLaneSegments();
        final Address originAddress = segments.get(0).getStartFacility().getLocation();
        final Address destinationAddress = segments.get(segments.size() - 1).getEndFacility().getLocation();
        if (originAddress != null && destinationAddress != null) {
            return checkIdenticalAddress(originAddress, destinationAddress);
        }
        return false;
    }

    private boolean hasIdenticalFacilityNodes(NetworkLane networkLane) {
        final List<NetworkLaneSegment> segments = networkLane.getNetworkLaneSegments();
        final Facility originFacility = segments.get(0).getStartFacility();
        final Facility endFacility = segments.get(segments.size() - 1).getEndFacility();
        if (originFacility != null && endFacility != null) {
            return checkIdenticalFacility(originFacility, endFacility);
        }
        return false;
    }

    private boolean checkIdenticalAddress(
            @NotNull final Address origin,
            @NotNull final Address destination) {
        return StringUtils.isNotBlank(origin.getId())
                && StringUtils.isNotBlank(destination.getId())
                && StringUtils.equals(origin.getId(), destination.getId());
    }

    private boolean checkIdenticalFacility(
            @NotNull final Facility originFacility,
            @NotNull final Facility destinationFacility) {
        return StringUtils.isNotBlank(originFacility.getId())
                && StringUtils.isNotBlank(destinationFacility.getId())
                && StringUtils.equals(originFacility.getId(), destinationFacility.getId());
    }
}
