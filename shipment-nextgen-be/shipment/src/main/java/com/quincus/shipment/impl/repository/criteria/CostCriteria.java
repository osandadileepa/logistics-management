package com.quincus.shipment.impl.repository.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.filter.CostAmountRange;
import com.quincus.shipment.api.filter.CostDateRange;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.specification.CostSpecification;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CostCriteria extends AbstractPageableCriteria<CostSpecification> implements UserPartnersCriteria {
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, BaseEntity_.CREATE_TIME);
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 10, DEFAULT_SORT);
    private List<String> keys;
    private List<String> costTypes;
    private List<String> vendors;
    private List<String> drivers;
    private CostDateRange incurredDateRange;
    private CostAmountRange costAmountRange;
    private String organizationId;
    private String partnerId;
    private Set<String> userLocationsCoverage;
    private List<String> userPartners;
    private ObjectMapper objectMapper;

    public CostCriteria() {
        super(DEFAULT_PAGE_REQUEST, DEFAULT_SORT);
    }

    @Override
    public CostSpecification buildSpecification() {
        return new CostSpecification(this, objectMapper);
    }
}
