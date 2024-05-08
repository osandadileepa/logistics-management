package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.service.LocationHierarchyAsyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.quincus.ext.DateTimeUtil.getOffset;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@Slf4j
@AllArgsConstructor
public class NetworkLaneDataHelper {

    private final LocationHierarchyAsyncService locationHierarchyAsyncService;

    public void setupDestinationUsingNetworkLaneSegment(
            final NetworkLane domain,
            final NetworkLaneEntity entity) {
        final List<NetworkLaneSegment> segments = domain.getNetworkLaneSegments();
        if (CollectionUtils.isEmpty(segments)) {
            return;
        }
        Optional.of(segments.get(segments.size() - 1))
                .ifPresent(networkLaneSegment -> {
                    final Facility endFacility = networkLaneSegment.getEndFacility();
                    if (isBlank(endFacility.getId()) && endFacility.getLocation() != null) {
                        domain.setDestination(endFacility.getLocation());
                        entity.setDestination(locationHierarchyAsyncService.setUpLocationHierarchies(endFacility.getLocation(), entity.getOrganizationId()));
                    } else {
                        domain.setDestinationFacility(endFacility);
                        entity.setDestination(locationHierarchyAsyncService.setUpLocationHierarchies(endFacility, entity.getOrganizationId()));
                    }
                });
    }

    public void setupOriginUsingNetworkLaneSegment(
            final NetworkLane domain,
            final NetworkLaneEntity entity) {
        if (CollectionUtils.isEmpty(domain.getNetworkLaneSegments())) {
            return;
        }
        Optional.of(domain.getNetworkLaneSegments().get(0))
                .ifPresent(networkLaneSegment -> {
                    final Facility startFacility = networkLaneSegment.getStartFacility();
                    if (isBlank(startFacility.getId()) && startFacility.getLocation() != null) {
                        domain.setOrigin(startFacility.getLocation());
                        entity.setOrigin(locationHierarchyAsyncService.setUpLocationHierarchies(startFacility.getLocation(), entity.getOrganizationId()));
                    } else {
                        domain.setOriginFacility(startFacility);
                        entity.setOrigin(locationHierarchyAsyncService.setUpLocationHierarchies(startFacility, entity.getOrganizationId()));
                    }
                });
    }

    public void setSegmentTypeOnNetworkLaneSegments(final NetworkLaneEntity entity) {
        List<NetworkLaneSegmentEntity> segments = entity.getNetworkLaneSegmentList();

        if (CollectionUtils.isEmpty(segments)) {
            return;
        }

        int lastIndex = segments.size() - 1;

        if (lastIndex == 0) {
            segments.get(0).setType(SegmentType.LAST_MILE);
        } else {
            segments.get(0).setType(SegmentType.FIRST_MILE);
            segments.get(lastIndex).setType(SegmentType.LAST_MILE);
        }

        for (int index = 1; index < lastIndex; index++) {
            segments.get(index).setType(SegmentType.MIDDLE_MILE);
        }
    }

    public void enrichLaneSegmentTimezoneFields(final NetworkLaneEntity entity) {
        List<NetworkLaneSegmentEntity> laneSegments = entity.getNetworkLaneSegmentList();

        if (CollectionUtils.isEmpty(laneSegments)) {
            return;
        }
        laneSegments.forEach(laneSegment -> {
            String startFacilityTimezone = getTimezone(laneSegment.getStartLocationHierarchy());
            String endFacilityTimezone = getTimezone(laneSegment.getEndLocationHierarchy());
            if (TransportType.GROUND == laneSegment.getTransportType()) {
                laneSegment.setPickUpTimezone(getOffset(laneSegment.getPickUpTime(), startFacilityTimezone));
                laneSegment.setDropOffTimezone(getOffset(laneSegment.getDropOffTime(), endFacilityTimezone));
                return;
            }
            laneSegment.setDepartureTimezone(getOffset(laneSegment.getDepartureTime(), startFacilityTimezone));
            laneSegment.setArrivalTimezone(getOffset(laneSegment.getArrivalTime(), endFacilityTimezone));
            laneSegment.setLockOutTimezone(getOffset(laneSegment.getLockOutTime(), startFacilityTimezone));
            laneSegment.setRecoveryTimezone(getOffset(laneSegment.getRecoveryTime(), endFacilityTimezone));

        });
    }

    private String getTimezone(LocationHierarchyEntity locationHierarchyEntity) {
        if (locationHierarchyEntity.getFacility() == null) {
            return locationHierarchyEntity.getCity().getTimezone();
        }
        return locationHierarchyEntity.getFacility().getTimezone();
    }

}
