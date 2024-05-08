package com.quincus.shipment.impl.repository.criteria;

import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.filter.NetworkLaneLocationFilter;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.specification.NetworkLaneSpecification;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Getter
@Setter
public class NetworkLaneCriteria extends AbstractPageableCriteria<NetworkLaneSpecification> {
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, BaseEntity_.CREATE_TIME);
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 10, DEFAULT_SORT);

    private String organizationId;
    private List<ServiceType> serviceTypes;
    private NetworkLaneLocationFilter originLocations;
    private NetworkLaneLocationFilter destinationLocations;
    private NetworkLaneLocationFilter facilityLocations;

    public NetworkLaneCriteria() {
        super(DEFAULT_PAGE_REQUEST, DEFAULT_SORT);
    }

    @Override
    public NetworkLaneSpecification buildSpecification() {
        return new NetworkLaneSpecification(this);
    }
}
