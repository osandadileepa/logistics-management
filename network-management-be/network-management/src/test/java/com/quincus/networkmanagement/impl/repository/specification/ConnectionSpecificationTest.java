package com.quincus.networkmanagement.impl.repository.specification;

import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class ConnectionSpecificationTest {

    @Test
    void toPredicateNoFilterReturnsEmptyPredicate() {
        ConnectionSearchFilter filter = new ConnectionSearchFilter();
        ConnectionSpecification specification = new ConnectionSpecification(filter);

        Root<ConnectionEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);
        assertThat(predicate).isNull();
    }
}

