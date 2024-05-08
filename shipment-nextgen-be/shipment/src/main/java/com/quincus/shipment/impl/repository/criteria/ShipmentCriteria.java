package com.quincus.shipment.impl.repository.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.filter.AirlineFilter;
import com.quincus.shipment.api.filter.ShipmentLocationFilter;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ShipmentCriteria extends AbstractPageableCriteria<ShipmentSpecification> implements LocationCoverageCriteria, UserPartnersCriteria {
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, BaseEntity_.CREATE_TIME);
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 10, DEFAULT_SORT);

    private Organization organization;
    private String partnerId;
    private EtaStatus[] etaStatus;
    private JourneyStatus journeyStatus;
    private Customer[] customer;
    private String[] origin;
    private String[] destination;
    private String[] keys;
    private String[] excludeKeys;
    private String[] costKeys;
    private AirlineFilter[] airlineKeys;
    private ServiceType[] serviceType;
    private Order order;
    private String[] facilities;
    private ShipmentLocationFilter facilityLocations;
    private ShipmentLocationFilter originLocations;
    private ShipmentLocationFilter destinationLocations;
    private Date bookingDateFrom;
    private Date bookingDateTo;
    private Map<LocationType, List<String>> userLocationCoverageIdsByType;
    private List<String> userPartners;
    private ObjectMapper objectMapper;
    private List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;
    private String[] alert;

    public ShipmentCriteria() {
        super(DEFAULT_PAGE_REQUEST, DEFAULT_SORT);
    }

    @Override
    public ShipmentSpecification buildSpecification() {
        return new ShipmentSpecification(
                this,
                objectMapper,
                shipmentLocationCoveragePredicates
        );
    }
}
