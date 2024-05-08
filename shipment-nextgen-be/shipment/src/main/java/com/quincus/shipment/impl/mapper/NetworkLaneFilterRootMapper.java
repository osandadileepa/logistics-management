package com.quincus.shipment.impl.mapper;

import com.quincus.order.api.domain.Location;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneLocationFilter;
import com.quincus.shipment.api.filter.ServiceTypeFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NetworkLaneFilterRootMapper {
    public NetworkLaneFilter mapOrderMessageToNetworkLaneFilter(Root omMessage) {
        if (!isAllCriteriaPresentInOmPayload(omMessage)) {
            return null;
        }
        NetworkLaneFilter filter = new NetworkLaneFilter();
        filter.setServiceTypes(List.of(generateServiceTypeFromOmMessage(omMessage)));
        filter.setOriginLocations(generateNetworkLocationFilter(omMessage.getOrigin()));
        filter.setDestinationLocations(generateNetworkLocationFilter(omMessage.getDestination()));
        filter.setPageNumber(1);
        filter.setSize(3);
        return filter;
    }

    private ServiceTypeFilter generateServiceTypeFromOmMessage(Root omMessage) {
        ServiceTypeFilter serviceTypeFilter = new ServiceTypeFilter();
        serviceTypeFilter.setName(omMessage.getServiceType());
        return serviceTypeFilter;
    }

    private NetworkLaneLocationFilter generateNetworkLocationFilter(Location location) {
        NetworkLaneLocationFilter networkLaneLocationFilter = new NetworkLaneLocationFilter();
        networkLaneLocationFilter.setCountryExtIds(List.of(location.getCountryId()));
        networkLaneLocationFilter.setStateExtIds(List.of(location.getStateId()));
        networkLaneLocationFilter.setCityExtIds(List.of(location.getCityId()));
        return networkLaneLocationFilter;
    }

    private boolean isAllCriteriaPresentInOmPayload(Root omMessage) {
        return omMessage != null && StringUtils.isNotBlank(omMessage.getServiceType())
                && omMessage.getOrigin() != null && omMessage.getDestination() != null
                && StringUtils.isNotBlank(omMessage.getOrigin().getCountryId())
                && StringUtils.isNotBlank(omMessage.getOrigin().getStateId())
                && StringUtils.isNotBlank(omMessage.getOrigin().getCityId())
                && StringUtils.isNotBlank(omMessage.getDestination().getCountryId())
                && StringUtils.isNotBlank(omMessage.getDestination().getStateId())
                && StringUtils.isNotBlank(omMessage.getDestination().getCityId());
    }
}
