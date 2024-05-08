package com.quincus.finance.costing.weightcalculation.db.repository.specification;

import com.quincus.db.model.OrganizationEntity;
import com.quincus.finance.costing.weightcalculation.api.filter.WeightCalculationRuleFilter;
import com.quincus.finance.costing.weightcalculation.db.model.WeightCalculationRuleEntity;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@AllArgsConstructor
public class WeightCalculationRuleSpecification implements Specification<WeightCalculationRuleEntity> {

    private final transient WeightCalculationRuleFilter filter;

    @Override
    public Predicate toPredicate(Root<WeightCalculationRuleEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        Predicate predicate = criteriaBuilder.conjunction();

        String organizationId = filter.getOrganizationId();
        if (StringUtils.isNotBlank(organizationId)) {
            OrganizationEntity organization = new OrganizationEntity(organizationId);
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("organization"), organization));
        }

        String name = filter.getName();
        if (StringUtils.isNotBlank(name)) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + name + "%"));
        }

        String includeDefaultRules = filter.getIncludeDefaultRules();
        if (!BooleanUtils.toBoolean(includeDefaultRules)) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNotEmpty(root.get("partners")));
        }

        return predicate;
    }

}
