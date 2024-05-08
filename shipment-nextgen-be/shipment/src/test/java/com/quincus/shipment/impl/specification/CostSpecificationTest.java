package com.quincus.shipment.impl.specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.filter.CostAmountRange;
import com.quincus.shipment.impl.repository.criteria.CostCriteria;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.repository.entity.CostEntity_;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import com.quincus.shipment.impl.repository.specification.CostSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class CostSpecificationTest {

    @Mock
    private Root<CostEntity> root;
    @Mock
    private Path<Object> path;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<Object> criteriaQuery;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("given all criteria when toPredicate then generate correct predicates for all fields")
    void shouldGeneratePredicates() {
        CostCriteria criteria = createCostCriteria();

        when(root.get(CostEntity_.DRIVER_NAME)).thenReturn(path);
        when(root.get(CostEntity_.PARTNER_NAME)).thenReturn(path);
        when(root.get(MultiTenantEntity_.ORGANIZATION_ID)).thenReturn(path);
        when(root.get(CostEntity_.PARTNER_ID)).thenReturn(path);
        when(root.get(CostEntity_.COST_AMOUNT)).thenReturn(path);
        when(root.get(CostEntity_.PARTNER_ID)).thenReturn(path);


        CostSpecification specification = new CostSpecification(criteria, objectMapper);
        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(root, times(1)).get(CostEntity_.DRIVER_NAME);
        verify(root, times(1)).get(CostEntity_.PARTNER_NAME);
        verify(root, times(1)).get(MultiTenantEntity_.ORGANIZATION_ID);
        verify(root, times(1)).get(CostEntity_.PARTNER_ID);
        verify(root, times(1)).get(CostEntity_.COST_AMOUNT);
    }


    @Test
    @DisplayName("given no criteria when toPredicate then don't generate extra predicates ")
    void shouldNotGenerateExtraPredicates() {
        CostCriteria criteria = new CostCriteria();

        CostSpecification specification = new CostSpecification(criteria, objectMapper);
        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(specification.getPredicates()).hasSize(1);

    }

    private CostCriteria createCostCriteria() {
        CostCriteria criteria = new CostCriteria();

        criteria.setKeys(List.of("key1", "key2"));
        criteria.setVendors(List.of("vendor1", "vendor2"));
        criteria.setDrivers(List.of("by1", "by2"));

        CostAmountRange costAmountRange = new CostAmountRange();
        costAmountRange.setMinCostAmount(new BigDecimal("0.00"));
        costAmountRange.setMaxCostAmount(new BigDecimal("1000.00"));
        criteria.setCostAmountRange(costAmountRange);

        criteria.setOrganizationId("1L");
        criteria.setPartnerId("1L");

        criteria.setUserPartners(List.of("partner1", "partner2"));

        return criteria;
    }

}
