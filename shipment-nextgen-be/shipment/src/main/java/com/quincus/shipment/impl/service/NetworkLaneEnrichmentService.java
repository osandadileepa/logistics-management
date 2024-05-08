package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.impl.helper.NetworkLaneDurationCalculator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class NetworkLaneEnrichmentService {

    private final NetworkLaneDurationCalculator networkLaneDurationCalculator;

    public void enrichNetworkLanesDurationCalculation(List<NetworkLane> networkLanes) {
        networkLanes.forEach(networkLane -> enrichNetworkLaneSegmentsCalculatedDuration(networkLane.getNetworkLaneSegments()));
    }

    private void enrichNetworkLaneSegmentsCalculatedDuration(List<NetworkLaneSegment> networkLaneSegments) {
        networkLaneSegments.forEach(segment ->
                segment.setCalculatedDuration(networkLaneDurationCalculator
                        .calculateNetworkLaneSegmentDuration(segment)));
    }

}
