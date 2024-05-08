package com.quincus.shipment.impl.repository.specification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public abstract class BaseSpecification<T> implements Specification<T> {
    public static final String ADD_ORGANIZATION_PREDICATE = "addOrganizationPredicate";
    @Getter
    protected final Map<String, Predicate> predicates = new HashMap<>();

    protected Predicate buildPredicate(CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Predicate predicate = criteriaBuilder.conjunction();
        for (Predicate predicateByCriteria : predicates.values()) {
            predicate = criteriaBuilder.and(predicate, predicateByCriteria);
        }
        query.distinct(true);
        return predicate;
    }

    protected void addEqualsPredicate(Root<T> root, String key, CriteriaBuilder criteriaBuilder, Object value, String fieldName) {
        if (value != null) {
            addPredicate(key, criteriaBuilder.equal(root.get(fieldName), value));
        }
    }

    protected void addNotEqualsPredicate(Root<T> root, String key, CriteriaBuilder criteriaBuilder, Object value, String fieldName) {
        if (value != null) {
            addPredicate(key, criteriaBuilder.notEqual(root.get(fieldName), value));
        }
    }

    protected void addInPredicate(Root<T> root, String key, List<?> values, String fieldName) {
        if (CollectionUtils.isNotEmpty(values)) {
            addPredicate(key, root.get(fieldName).in(values));
        }
    }

    protected void addPredicate(String key, Predicate predicate) {
        predicates.putIfAbsent(key, predicate);
    }
}
