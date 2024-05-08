package com.quincus.finance.costing.weightcalculation.api;

import com.quincus.finance.costing.weightcalculation.api.filter.WeightCalculationRuleFilter;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WeightCalculationRuleApi {

    WeightCalculationRule create(WeightCalculationRule weightCalculationRule);
    
    Optional<WeightCalculationRule> find(String id);

    Optional<WeightCalculationRule> update(WeightCalculationRule weightCalculationRule);

    List<WeightCalculationRule> findActiveByOrganizationIdAndPartnerIds(String organizationId, Set<String> partnerIds);

    List<WeightCalculationRule> findActiveByOrganizationId(String organizationId);

    Page<WeightCalculationRule> search(WeightCalculationRuleFilter filter, Pageable pageable);

    void delete(String id);

}
