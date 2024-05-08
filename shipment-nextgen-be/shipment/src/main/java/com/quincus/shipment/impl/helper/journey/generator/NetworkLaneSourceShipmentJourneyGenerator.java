package com.quincus.shipment.impl.helper.journey.generator;

import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentPickUpDropOffTimeCalculator;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentTypeAssigner;
import com.quincus.shipment.impl.mapper.NetworkLaneFilterRootMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneJourneySegmentMapper;
import com.quincus.shipment.impl.mapper.OMLocationMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.NetworkLaneService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Order(2)
@Component
@AllArgsConstructor
@Slf4j
public class NetworkLaneSourceShipmentJourneyGenerator implements ShipmentJourneyGenerator {

    private final NetworkLaneService networkLaneService;
    private final UserDetailsProvider userDetailsProvider;
    private final NetworkLaneJourneySegmentMapper networkLaneJourneySegmentMapper;
    private final NetworkLaneFilterRootMapper networkLaneFilterRootMapper;
    private final PackageJourneySegmentTypeAssigner packageJourneySegmentTypeAssigner;
    private final PackageJourneySegmentPickUpDropOffTimeCalculator packageJourneySegmentPickupDropOffTimeCalculator;

    @Override
    public ShipmentJourney generateShipmentJourney(Root omMessage) {
        List<PackageJourneySegment> packageJourneySegmentList = generatePackageJourneyFromNetwork(omMessage);
        if (CollectionUtils.isEmpty(packageJourneySegmentList)) {
            return null;
        }
        resetSegmentFacilityIdsToNull(packageJourneySegmentList);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setStatus(JourneyStatus.PLANNED);
        shipmentJourney.setPackageJourneySegments(packageJourneySegmentList);
        return shipmentJourney;
    }

    private void resetSegmentFacilityIdsToNull(List<PackageJourneySegment> packageJourneySegmentList) {
        // there is a validation for this to null and only have external id
        packageJourneySegmentList.forEach(segment -> {
            segment.getStartFacility().setId(null);
            segment.getEndFacility().setId(null);
        });
    }

    private List<PackageJourneySegment> generatePackageJourneyFromNetwork(Root omMessage) {
        NetworkLane networkLane = findEligibleNetworkByOrder(omMessage);
        if (networkLane == null || CollectionUtils.isEmpty(networkLane.getNetworkLaneSegments())) {
            return Collections.emptyList();
        }
        log.debug("NetworkLane with id:{} was used for OrderMessageId: {} ", networkLane.getId(), omMessage.getId());
        List<PackageJourneySegment> mappedSegments = IntStream.range(0, networkLane.getNetworkLaneSegments().size())
                .mapToObj(i -> networkLaneJourneySegmentMapper.toPackageJourneySegment(networkLane.getNetworkLaneSegments().get(i),
                        String.valueOf(i), Optional.ofNullable(omMessage.getOpsType()).orElse("")))
                .collect(Collectors.toCollection(ArrayList::new));
        assignFirstAndLastSegmentStartAndEndFacilityFromOrderWhenNull(mappedSegments, omMessage);
        packageJourneySegmentTypeAssigner.assignSegmentTypes(mappedSegments);
        packageJourneySegmentPickupDropOffTimeCalculator.computeAndAssignPickUpAndDropOffTime(mappedSegments, omMessage);

        return mappedSegments;
    }

    private void assignFirstAndLastSegmentStartAndEndFacilityFromOrderWhenNull(List<PackageJourneySegment> segments, Root omMessage) {
        if (CollectionUtils.isEmpty(segments)) {
            return;
        }
        PackageJourneySegment firstSegment = segments.get(0);
        if (firstSegment.getStartFacility() == null || firstSegment.getStartFacility().getExternalId() == null) {
            firstSegment.setStartFacility(OMLocationMapper.mapLocationToFacility(omMessage.getOrigin().getId(), omMessage.getOrigin(), Shipment.ORIGIN_PROPERTY_NAME));
        }
        PackageJourneySegment lastSegment = segments.get(segments.size() - 1);
        if (lastSegment.getEndFacility() == null || lastSegment.getEndFacility().getExternalId() == null) {
            lastSegment.setEndFacility(OMLocationMapper.mapLocationToFacility(omMessage.getDestination().getId(), omMessage.getDestination(), Shipment.DESTINATION_PROPERTY_NAME));
        }
    }

    private NetworkLane findEligibleNetworkByOrder(Root omMessage) {
        NetworkLaneFilter filter = networkLaneFilterRootMapper.mapOrderMessageToNetworkLaneFilter(omMessage);
        if (filter == null) {
            return null;
        }
        NetworkLaneFilterResult result = networkLaneService.findAll(filter, userDetailsProvider.getCurrentOrganizationId());
        return result.getResult().stream().findFirst().orElse(null);
    }
}
