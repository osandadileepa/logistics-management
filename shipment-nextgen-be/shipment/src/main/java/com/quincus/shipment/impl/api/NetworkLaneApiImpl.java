package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.NetworkLaneApi;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.NetworkLaneService;
import com.quincus.shipment.impl.validator.NetworkLaneValidator;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class NetworkLaneApiImpl implements NetworkLaneApi {

    private final NetworkLaneService networkLaneService;
    private final UserDetailsProvider userDetailsProvider;
    private final NetworkLaneValidator networkLaneValidator;

    @Override
    public NetworkLaneFilterResult findAll(NetworkLaneFilter filter) {
        return networkLaneService.findAll(filter, userDetailsProvider.getCurrentOrganizationId());
    }

    @Override
    public NetworkLane update(NetworkLane networkLane) {
        enrichNetworkLaneSegmentSequence(networkLane);
        networkLaneValidator.validate(networkLane);
        return networkLaneService.update(networkLane);
    }

    private void enrichNetworkLaneSegmentSequence(NetworkLane networkLane) {
        final List<NetworkLaneSegment> networkLaneSegments = networkLane.getUnsortedNetworkLaneSegments();
        if (CollectionUtils.isEmpty(networkLaneSegments)) {
            return;
        }
        IntStream.range(0, networkLaneSegments.size())
                .forEach(index -> {
                    final NetworkLaneSegment networkLaneSegment = networkLaneSegments.get(index);
                    networkLaneSegment.setSequence(String.valueOf(index));
                });
    }

    @Override
    public NetworkLane findById(String id) {
        return networkLaneService.findById(id);
    }
}
